package com.robbix.mp5.map;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Footprint;
import com.robbix.mp5.basics.Grid;
import com.robbix.mp5.basics.IterableIterator;
import com.robbix.mp5.basics.Neighbors;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;

public class LayeredMap
{
	public static LayeredMap load(File rootDir, String mapName, TileSet tileSet) throws IOException
	{
		LayeredMap map = new LayeredMap();
		
		map.costMap = Utils.loadCostMap(new File(rootDir, mapName + ".bmp"));
		
		int w = map.costMap.w;
		int h = map.costMap.h;
		
		map.tileSet = tileSet;
		map.grid = new Grid<Spot>(w, h);
		map.units = new HashSet<Unit>();
		map.deposits = new HashSet<ResourceDeposit>();
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			map.grid.set(x, y, new Spot());
		
		BufferedReader reader = new BufferedReader(new FileReader(
			new File(rootDir, mapName + ".txt")
		));
		
		String line = null;
		
		for (int y = 0; (line = reader.readLine()) != null; ++y)
			for (int x = 0; x < line.length(); ++x)
				switch (line.charAt(x))
				{
				case 'w':
					map.putWall(new Position(x, y));
					break;
				case 't':
					map.putTube(new Position(x, y));
					break;
				case 'p':
					map.grid.get(x, y).tileCode = tileSet.getPlainTile();
					break;
				default:
					throw new IOException("invalid terrain");
				}
		
		reader.close();
		
		return map;
	}
	
	public static enum Fixture
	{
		WALL, TUBE, MINE_PLATFORM
	}
	
	private static final int WALL_MAX_HP = 500;
	private static final int TUBE_MAX_HP = 2000;
	
	private static class Spot
	{
		Unit occupant;
		Unit reservant;
		Fixture fixture;
		int fixtureHP;
		String tileCode;
		ResourceDeposit deposit;
	}
	
	private Grid<Spot> grid;
	private Set<Unit> units;
	private Set<ResourceDeposit> deposits;
	private CostMap costMap;
	private TileSet tileSet;
	
	private DisplayPanel panel;
	
	private LayeredMap()
	{
	}
	
	public void setDisplayPanel(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return panel;
	}
	
	public int getWidth()
	{
		return grid.w;
	}
	
	public int getHeight()
	{
		return grid.h;
	}
	
	public Region getBounds()
	{
		return grid.getBounds();
	}
	
	public int getTileSize()
	{
		return 32;
	}
	
	public String getTileCode(Position pos)
	{
		return getTileCode(pos.x, pos.y);
	}
	
	public String getTileCode(int x, int y)
	{
		return grid.get(x, y).tileCode;
	}
	
	public boolean canPlaceFixture(Position pos)
	{
		return canPlaceUnit(pos) && grid.get(pos).fixture == null;
	}
	
	public Set<ResourceDeposit> getResourceDeposits()
	{
		return deposits;
	}
	
	public void putResourceDeposit(ResourceDeposit deposit, Position pos)
	{
		if (grid.get(pos).deposit != null)
			throw new IllegalStateException("deposit already set there");
		
		grid.get(pos).deposit = deposit;
		deposit.setPosition(pos);
		deposits.add(deposit);
	}
	
	public void removeResourceDeposit(Position pos)
	{
		ResourceDeposit deposit = grid.get(pos).deposit;
		
		deposits.remove(deposit);
		deposit.setPosition(null);
		grid.get(pos).deposit = null;
	}
	
	public ResourceDeposit getResourceDeposit(Position pos)
	{
		return grid.get(pos).deposit;
	}
	
	public boolean hasResourceDeposit(Position pos)
	{
		return grid.get(pos).deposit != null;
	}
	
	public boolean canPlaceResourceDeposit(Position pos)
	{
		return grid.get(pos).deposit == null;
	}
	
	public void putWall(Position pos)
	{
		Spot spot = grid.get(pos);
		
		if (spot.fixture != null)
			throw new IllegalStateException("fixture present");
		
		spot.fixture = Fixture.WALL;
		spot.fixtureHP = WALL_MAX_HP;
		spot.tileCode = tileSet.getWallTile(getWallNeighbors(pos));
		costMap.setInfinite(pos);
		
		Position n = pos.shift(+0, -1);
		Position s = pos.shift(+0, +1);
		Position w = pos.shift(-1, +0);
		Position e = pos.shift(+1, +0);
		
		if (grid.getBounds().contains(n) && hasWall(n))
			grid.get(n).tileCode = tileSet.getWallTile(getWallNeighbors(n));
		
		if (grid.getBounds().contains(s) && hasWall(s))
			grid.get(s).tileCode = tileSet.getWallTile(getWallNeighbors(s));
		
		if (grid.getBounds().contains(w) && hasWall(w))
			grid.get(w).tileCode = tileSet.getWallTile(getWallNeighbors(w));
		
		if (grid.getBounds().contains(e) && hasWall(e))
			grid.get(e).tileCode = tileSet.getWallTile(getWallNeighbors(e));
		
		panel.refresh();
	}
	
	public void putTube(Position pos)
	{
		Spot spot = grid.get(pos);
		
		if (spot.fixture != null)
			throw new IllegalStateException("fixture present");
		
		spot.fixture = Fixture.TUBE;
		spot.fixtureHP = TUBE_MAX_HP;
		spot.tileCode = tileSet.getTubeTile(getTubeNeighbors(pos));
		costMap.setZero(pos);
		
		Position n = pos.shift(+0, -1);
		Position s = pos.shift(+0, +1);
		Position w = pos.shift(-1, +0);
		Position e = pos.shift(+1, +0);
		
		if (grid.getBounds().contains(n) && hasTube(n))
			grid.get(n).tileCode = tileSet.getTubeTile(getTubeNeighbors(n));
		
		if (grid.getBounds().contains(s) && hasTube(s))
			grid.get(s).tileCode = tileSet.getTubeTile(getTubeNeighbors(s));
		
		if (grid.getBounds().contains(w) && hasTube(w))
			grid.get(w).tileCode = tileSet.getTubeTile(getTubeNeighbors(w));
		
		if (grid.getBounds().contains(e) && hasTube(e))
			grid.get(e).tileCode = tileSet.getTubeTile(getTubeNeighbors(e));
		
		panel.refresh();
	}
	
	public void bulldoze(Position pos)
	{
		clearFixture(pos);
		costMap.setZero(pos);
		grid.get(pos).tileCode = tileSet.getBulldozedTile();
		panel.refresh();
	}
	
	public void clearFixture(Position pos)
	{
		Spot spot = grid.get(pos);
		spot.fixture = null;
		spot.fixtureHP = 0;
		
		Position adj;
		adj = pos.shift(+0, -1);
		
		if (grid.getBounds().contains(adj))
		{
			spot = grid.get(adj);
			
			if (hasWall(adj))
			{
				spot.fixture = null;
				putWall(adj);
			}
			else if (hasTube(adj))
			{
				spot.fixture = null;
				putTube(adj);
			}
		}

		adj = pos.shift(+0, +1);
		
		if (grid.getBounds().contains(adj))
		{
			spot = grid.get(adj);
			
			if (hasWall(adj))
			{
				spot.fixture = null;
				putWall(adj);
			}
			else if (hasTube(adj))
			{
				spot.fixture = null;
				putTube(adj);
			}
		}

		adj = pos.shift(+1, +0);
		
		if (grid.getBounds().contains(adj))
		{
			spot = grid.get(adj);
			
			if (hasWall(adj))
			{
				spot.fixture = null;
				putWall(adj);
			}
			else if (hasTube(adj))
			{
				spot.fixture = null;
				putTube(adj);
			}
		}

		adj = pos.shift(-1, +0);
		
		if (grid.getBounds().contains(adj))
		{
			spot = grid.get(adj);
			
			if (hasWall(adj))
			{
				spot.fixture = null;
				putWall(adj);
			}
			else if (hasTube(adj))
			{
				spot.fixture = null;
				putTube(adj);
			}
		}
	}
	
	public boolean hasWall(Position pos)
	{
		return grid.get(pos).fixture == Fixture.WALL;
	}
	
	public boolean hasTube(Position pos)
	{
		return grid.get(pos).fixture == Fixture.TUBE;
	}
	
	public boolean hasMinePlatform(Position pos)
	{
		return grid.get(pos).fixture == Fixture.MINE_PLATFORM;
	}
	
	public int getFixtureHP(Position pos)
	{
		Spot spot = grid.get(pos);
		
		if (!(spot.fixture == Fixture.TUBE || spot.fixture == Fixture.WALL))
			throw new IllegalStateException("fixture doesn't have hp");
		
		return spot.fixtureHP;
	}
	
	public void setFixtureHP(Position pos, int hp)
	{
		Spot spot = grid.get(pos);
		
		if (!(spot.fixture == Fixture.TUBE || spot.fixture == Fixture.WALL))
			throw new IllegalStateException("fixture doesn't have hp");
		
		if (spot.fixture == Fixture.WALL)
		{
			double hpFactor = hp / (double) WALL_MAX_HP;
			String[] tileCodeParts = spot.tileCode.split("/");
			String neihborsString = tileCodeParts[tileCodeParts.length - 1];
			Neighbors neighbors = Neighbors.valueOf(neihborsString);
			HealthBracket health = HealthBracket.getDefault(hpFactor);
			spot.tileCode = tileSet.getWallTile(neighbors, health);
		}
		
		spot.fixtureHP = hp;
	}
	
	public Neighbors getWallNeighbors(Position pos)
	{
		Neighbors neighbors = Neighbors.NONE;
		
		Position n = pos.shift(+0, -1);
		Position s = pos.shift(+0, +1);
		Position w = pos.shift(-1, +0);
		Position e = pos.shift(+1, +0);
		
		if (grid.getBounds().contains(n) && hasWall(n))
			neighbors = neighbors.add(Neighbors.N);
		
		if (grid.getBounds().contains(s) && hasWall(s))
			neighbors = neighbors.add(Neighbors.S);
		
		if (grid.getBounds().contains(w) && hasWall(w))
			neighbors = neighbors.add(Neighbors.W);
		
		if (grid.getBounds().contains(e) && hasWall(e))
			neighbors = neighbors.add(Neighbors.E);
		
		return neighbors;
	}
	
	public Neighbors getTubeNeighbors(Position pos)
	{
		Neighbors neighbors = Neighbors.NONE;
		
		Position n = pos.shift(+0, -1);
		Position s = pos.shift(+0, +1);
		Position w = pos.shift(-1, +0);
		Position e = pos.shift(+1, +0);
		
		if (grid.getBounds().contains(n) && (hasTube(n) || structureOccupies(n)))
			neighbors = neighbors.add(Neighbors.N);
		
		if (grid.getBounds().contains(s) && (hasTube(s) || structureOccupies(s)))
			neighbors = neighbors.add(Neighbors.S);
		
		if (grid.getBounds().contains(w) && (hasTube(w) || structureOccupies(w)))
			neighbors = neighbors.add(Neighbors.W);
		
		if (grid.getBounds().contains(e) && (hasTube(e) || structureOccupies(e)))
			neighbors = neighbors.add(Neighbors.E);
		
		return neighbors;
	}
	
	public CostMap getTerrainCostMap()
	{
		return costMap;
	}
	
	public boolean canPlaceUnit(Position pos)
	{
		Spot spot = grid.get(pos);
		
		return !costMap.isInfinite(pos)
			&& spot.occupant == null
			&& spot.reservant == null;
	}
	
	public boolean canPlaceUnit(Position pos, Footprint fp)
	{
		for (Position occupied : fp.iterator(pos))
			if (!canPlaceUnit(occupied))
				return false;
		
		return true;
	}
	
	public void addUnit(Unit unit, Position pos)
	{
		if (!canPlaceUnit(pos, unit.getFootprint()))
			throw new IllegalStateException("can't place unit");
		
		if (unit.getContainer() != null)
		{
			throw new IllegalStateException("Unit already in a UnitLayer");
		}
		
		unit.setPosition(pos);
		unit.setContainer(this);
		
		for (Position occupied : unit.getFootprint().iterator(pos))
			grid.get(occupied).occupant = unit;
		
		units.add(unit);
		
		if (unit.isStructure() || unit.getType().isGuardPostType())
		{
			Region outer = unit.getFootprint().getInnerRegion();
			outer = new Region(
				outer.x - 1,
				outer.y - 1,
				outer.w + 2,
				outer.h + 2
			);
			
			for (Position bulldozedPos : outer.iterator(unit.getPosition()))
				if (getBounds().contains(bulldozedPos))
					bulldoze(bulldozedPos);
		}
		
		if (unit.isMine())
		{
			grid.get(pos).fixture = Fixture.MINE_PLATFORM;
			panel.refresh();
		}
	}
	
	public boolean isOccupied(Position pos)
	{
		return grid.get(pos).occupant != null;
	}
	
	public boolean isReserved(Position pos)
	{
		return grid.get(pos).reservant != null;
	}
	
	private boolean structureOccupies(Position pos)
	{
		Unit unit = grid.get(pos).occupant;
		
		return unit != null && unit.isStructure();
	}
	
	public Unit getReservant(Position pos)
	{
		return grid.get(pos).reservant;
	}
	
	public Unit getUnit(Position pos)
	{
		return grid.get(pos).occupant;
	}
	
	public Unit getUnitAbsolute(int x, int y)
	{
		int tileSize = getTileSize();
		
		boolean quadrantUpper = y % tileSize < 0.5;
		boolean quadrantLeft  = x % tileSize < 0.5;
		
		int gridX = (int)x / tileSize;
		int gridY = (int)y / tileSize;
		
		ArrayList<Position> toCheck = new ArrayList<Position>(4);
		toCheck.add(new Position(gridX, gridY));
		
		if (quadrantUpper && quadrantLeft)
		{
			toCheck.add(new Position(gridX - 1, gridY));
			toCheck.add(new Position(gridX,     gridY - 1));
			toCheck.add(new Position(gridX - 1, gridY - 1));
		}
		else if (!quadrantUpper && quadrantLeft)
		{
			toCheck.add(new Position(gridX - 1, gridY));
			toCheck.add(new Position(gridX,     gridY + 1));
			toCheck.add(new Position(gridX - 1, gridY + 1));
		}
		else if (quadrantUpper && !quadrantLeft)
		{
			toCheck.add(new Position(gridX + 1, gridY));
			toCheck.add(new Position(gridX,     gridY - 1));
			toCheck.add(new Position(gridX + 1, gridY - 1));
		}
		else
		{
			toCheck.add(new Position(gridX + 1, gridY));
			toCheck.add(new Position(gridX,     gridY + 1));
			toCheck.add(new Position(gridX + 1, gridY + 1));
		}
		
		for (Position pos : toCheck)
		{
			if (! getBounds().contains(pos))
				continue;
			
			Unit unit = grid.get(pos).occupant;
			
			if (unit != null)
			{
				int absX = pos.x * tileSize + unit.getXOffset();
				int absY = pos.y * tileSize + unit.getYOffset();
				
				if (x >= absX && x < absX + tileSize
				&&  y >= absY && y < absY + tileSize)
					return unit;
			}
		}
		
		return null;
	}
	
	public Set<Unit> getAllAbsolute(int x, int y, int w, int h)
	{
		Set<Unit> selected = new HashSet<Unit>();
		int tileSize = getTileSize();
		
		Rectangle selectedArea = new Rectangle(x, y, w, h);
		
		for (int gridX = x / tileSize; gridX < (x + w) / tileSize; ++gridX)
		for (int gridY = y / tileSize; gridY < (y + h) / tileSize; ++gridY)
		{
			Position pos = new Position(gridX, gridY);
			
			if (! getBounds().contains(pos))
				continue;
			
			Unit unit = grid.get(pos).occupant;

			if (unit != null)
			{
				Rectangle unitBounds = new Rectangle(
					unit.getAbsX(),
					unit.getAbsY(),
					unit.getWidth()  * tileSize,
					unit.getHeight() * tileSize
				);
				
				if (selectedArea.intersects(unitBounds))
					selected.add(unit);
			}
		}
		
		return selected;
	}
	
	public void remove(Unit unit)
	{
		if (! contains(unit))
			throw new NoSuchElementException();
		
		Position pos = unit.getPosition();
		
		for (Position fpPos : unit.getFootprint().iterator(pos))
		{
			grid.get(fpPos).occupant = null;
		}
		
		for (Position rPos : unit.getReservations())
		{
			grid.get(rPos).reservant = null;
		}
		
//			unit.setPosition(null);
//			unit.setContainer(null);
		
		units.remove(unit);
		
		if (unit.isMine())
		{
			grid.get(pos).fixture = null;
			panel.refresh();
		}
	}
	
	public void clearAllUnits()
	{
		Iterator<Unit> unitItr = units.iterator();
		
		while (unitItr.hasNext())
		{
			unitItr.next();
			unitItr.remove();
		}
	}
	
	public boolean contains(Unit unit)
	{
		return unit.getContainer() == this;
	}
	
	public int getUnitCount()
	{
		return units.size();
	}
	
	public boolean canMoveUnit(Position pos, Direction dir)
	{
		Position next = dir.apply(pos);
		
		if ((!grid.getBounds().contains(next)) || costMap.isInfinite(next))
			return false;
		
		Position dest = dir.apply(pos);
		
		if (!(grid.getBounds().contains(dest)
				&& grid.get(dest).occupant == null
				&& grid.get(dest).reservant == null))
			return false;
		
		if (!dir.isDiagonal())
			return true;
		
		Position adj1 = dir.rotate(+2).apply(pos);
		Position adj2 = dir.rotate(-2).apply(pos);
		
		Unit occupier1 = grid.get(adj1).occupant;
		Unit occupier2 = grid.get(adj2).occupant;
		Unit reserver1 = grid.get(adj1).reservant;
		Unit reserver2 = grid.get(adj2).reservant;
		
		if (occupier1 != null && occupier1.equals(reserver2))
			return false;
		
		if (occupier2 != null && occupier2.equals(reserver1))
			return false;
		
		return true;
	}
	
	public void move(Unit unit, Position pos)
	{
		if (grid.get(pos).occupant != null)
			throw new IllegalStateException(pos + " occupied");
		
		final Unit holder = grid.get(pos).reservant;
		
		if (holder != null && !holder.equals(unit))
			throw new IllegalStateException(pos + " reserved");
		
		Footprint fp = unit.getFootprint();
		
		for (Position occupied : fp.iterator(unit.getPosition()))
			grid.get(occupied).occupant = null;
		
		unit.setPosition(pos);
		
		for (Position occupied : fp.iterator(unit.getPosition()))
			grid.get(occupied).occupant = unit;
	}
	
	public void shift(Unit unit, int dx, int dy)
	{
		move(unit, unit.getPosition().shift(dx, dy));
	}
	
	public Unit findClosest(
		Unit unit,
		final Filter<Unit> unitFilter,
		double minDistance,
		double maxDistance)
	{
		if (!contains(unit))
			throw new NoSuchElementException();
		
		Position pos = unit.getPosition();
		
		Filter<Spot> spotFilter = new Filter<Spot>()
		{
			public boolean accept(Spot spot)
			{
				if (spot == null)
					return false;
				
				return unitFilter.accept(spot.occupant);
			}
		};
		
		Position closestPos = grid.findClosest(
			pos,
			spotFilter,
			minDistance,
			maxDistance
		);
		
		if (closestPos == null)
			return null;
		
		return grid.get(closestPos).occupant;
	}
	
	public void reserve(Position pos, Unit unit)
	{
		final Unit holder = grid.get(pos).reservant;
		
		if (holder != null && !holder.equals(unit))
			throw new IllegalStateException("pos reserved");
		
		grid.get(pos).reservant = unit;
		unit.getReservations().add(pos);
	}
	
	public void unreserve(Position pos)
	{
		final Unit holder = grid.get(pos).reservant;
		
		if (holder == null)
			return;
		
		grid.get(pos).reservant = null;
		holder.getReservations().remove(pos);
	}
	
	public Collection<Position> getReservations(Unit unit)
	{
		return unit.getReservations();
	}
	
	public IterableIterator<Unit> getUnitIterator()
	{
		return getUnitIterator(false);
	}
	
	public IterableIterator<Unit> getUnitIterator(boolean zorder)
	{
		Set<Unit> copy = zorder ? new TreeSet<Unit>(Utils.Z_ORDER_UNIT)
								: new HashSet<Unit>();
		
		copy.addAll(units);
		return IterableIterator.iterate(copy);
	}
}
