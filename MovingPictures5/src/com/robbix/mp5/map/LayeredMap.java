package com.robbix.mp5.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.CostMap;
import com.robbix.utils.Direction;
import com.robbix.utils.Filter;
import com.robbix.utils.Grid;
import com.robbix.utils.Neighbors;
import com.robbix.utils.Position;
import com.robbix.utils.RIterator;
import com.robbix.utils.Region;

// TODO: Reassess defensive bounds checking (add checked exception?)
public class LayeredMap
{
	public static LayeredMap load(File rootDir, String mapName, TileSet tileSet) throws IOException
	{
		LayeredMap map = new LayeredMap();
		
		map.costMap = CostMap.loadBitmap(new File(rootDir, mapName + ".bmp"));
		
		int w = map.costMap.w;
		int h = map.costMap.h;
		
		map.tileSet = tileSet;
		map.grid = new Grid<Spot>(w, h);
		map.sources = new HashSet<Position>();
		map.units = new HashSet<Unit>();
		map.deposits = new HashSet<ResourceDeposit>();
		map.bounds = new Region(0, 0, w, h);
		
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
		WALL(false), TUBE(true), MINE_PLATFORM(true), GEYSER(false);
		
		private final boolean passable;
		
		private Fixture(boolean passable)
		{
			this.passable = passable;
		}
	}
	
	private static final int WALL_MAX_HP = 500;
	private static final int TUBE_MAX_HP = 2000;
	
	private static class Spot
	{
		public Unit occupant;
		public Unit reservant;
		public Fixture fixture;
		public int fixtureHP;
		public String tileCode;
		public ResourceDeposit deposit;
		public boolean alive;
		
		public boolean isTube()
		{
			return fixture == Fixture.TUBE || (occupant != null &&
			(occupant.isStructure() || occupant.getType().isGuardPostType()));
		}
		
		public boolean isConnectionSource()
		{
			return occupant != null
				&& occupant.isConnectionSource();
		}
	}
	
	private Grid<Spot> grid;
	private Set<Position> sources;
	private Set<Unit> units;
	private Set<ResourceDeposit> deposits;
	private CostMap costMap;
	private TileSet tileSet;
	private Region bounds;
	
	private List<DisplayPanel> panels;
	
	private LayeredMap()
	{
		panels = new LinkedList<DisplayPanel>();
	}
	
	public void addDisplayPanel(DisplayPanel panel)
	{
		synchronized (panels)
		{
			panels.add(panel);
		}
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return getDisplayPanel(0);
	}
	
	public DisplayPanel getDisplayPanel(int index)
	{
		synchronized (panels)
		{
			return panels.get(index);
		}
	}
	
	public List<DisplayPanel> getDisplayPanels()
	{
		synchronized (panels)
		{
			return new ArrayList<DisplayPanel>(panels);
		}
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
	
	/**
	 * Spot size is the number of atomic increments between positions
	 * on the map grid.
	 */
	public int getSpotSize()
	{
		return 32;
	}
	
	public String getTileCode(Position pos)
	{
		return getTileCode(pos.x, pos.y);
	}
	
	public String getTileCode(int x, int y)
	{
		if (!bounds.contains(x, y))
			return null;
		
		return grid.get(x, y).tileCode;
	}
	
	public boolean canPlaceFixture(Fixture fixture, Position pos)
	{
		return bounds.contains(pos)
			&& (canPlaceUnit(pos) || fixture.passable)
			&& grid.get(pos).fixture == null;
	}
	
	public Set<ResourceDeposit> getResourceDeposits()
	{
		return deposits;
	}
	
	public void putResourceDeposit(ResourceDeposit deposit, Position pos)
	{
		if (! bounds.contains(pos))
			return;
		
		if (grid.get(pos).deposit != null)
			throw new IllegalStateException("deposit already set there");
		
		grid.get(pos).deposit = deposit;
		deposit.setPosition(pos);
		deposits.add(deposit);
	}
	
	public void removeResourceDeposit(Position pos)
	{
		if (! bounds.contains(pos))
			return;
		
		ResourceDeposit deposit = grid.get(pos).deposit;
		
		deposits.remove(deposit);
		deposit.setPosition(null);
		grid.get(pos).deposit = null;
	}
	
	public ResourceDeposit getResourceDeposit(Position pos)
	{
		return bounds.contains(pos) ? grid.get(pos).deposit : null;
	}
	
	public boolean hasResourceDeposit(Position pos)
	{
		return bounds.contains(pos) && grid.get(pos).deposit != null;
	}
	
	public boolean canPlaceResourceDeposit(Position pos)
	{
		return bounds.contains(pos) && grid.get(pos).deposit == null;
	}
	
	public void putFixture(Fixture fixture, Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		switch (fixture)
		{
		case TUBE:
			putTube(pos);
			return;
		case WALL:
			putWall(pos);
			return;
		case GEYSER:
			putGeyser(pos);
			return;
		case MINE_PLATFORM:
			if (!canPlaceFixture(fixture, pos))
				throw new IllegalStateException();
			
			grid.get(pos).fixture = fixture;
			return;
		}
		
		throw new IllegalArgumentException("invalid fixture " + fixture);
	}
	
	public void putWall(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		Spot spot = grid.get(pos);
		
		if (spot.fixture != null)
			throw new IllegalStateException("fixture present " + pos);
		
		spot.fixture = Fixture.WALL;
		spot.fixtureHP = WALL_MAX_HP;
		spot.tileCode = tileSet.getWallTile(getWallNeighbors(pos));
		costMap.setInfinite(pos);
		
		Position n = pos.shift(+0, -1);
		Position s = pos.shift(+0, +1);
		Position w = pos.shift(-1, +0);
		Position e = pos.shift(+1, +0);
		
		if (bounds.contains(n) && hasWall(n))
			grid.get(n).tileCode = tileSet.getWallTile(getWallNeighbors(n));
		
		if (bounds.contains(s) && hasWall(s))
			grid.get(s).tileCode = tileSet.getWallTile(getWallNeighbors(s));
		
		if (bounds.contains(w) && hasWall(w))
			grid.get(w).tileCode = tileSet.getWallTile(getWallNeighbors(w));
		
		if (bounds.contains(e) && hasWall(e))
			grid.get(e).tileCode = tileSet.getWallTile(getWallNeighbors(e));
		
		refreshPanel(new Region(pos).stretch(1));
	}
	
	public void putTube(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		Spot spot = grid.get(pos);
		
		if (spot.fixture != null)
			throw new IllegalStateException("fixture present " + pos);
		
		spot.fixture = Fixture.TUBE;
		spot.fixtureHP = TUBE_MAX_HP;
		spot.tileCode = tileSet.getTubeTile(getTubeNeighbors(pos));
		costMap.setZero(pos);
		
		spot.alive = false;
		sources.remove(pos);
		
		for (Position adj : pos.get4Neighbors())
		{
			if (bounds.contains(adj) && (hasTube(adj) || isOccupied(adj)))
			{
				Unit occupant = getUnit(adj);
				spot.alive |= occupant != null && occupant.isConnected();
				spot.alive |= grid.get(adj).alive;
				
				if (hasTube(adj))
				{
					// Refresh tube tile for new neighbor
					grid.get(adj).tileCode = tileSet.getTubeTile(getTubeNeighbors(adj));
				}
			}
		}
		
		if (spot.alive)
		{
			branchConnections(pos);
		}
		
		refreshPanel(new Region(pos).stretch(1));
	}
	
	public void putGeyser(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		Spot spot = grid.get(pos);
		
		if (spot.fixture != null)
			throw new IllegalStateException("fixture present " + pos);
		
		spot.fixture = Fixture.GEYSER;
		costMap.setInfinite(pos);
		
		refreshPanel(new Region(pos).stretch(1));
	}
	
	public boolean isAlive(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).alive;
	}
	
	private void assessConnections()
	{
		for (int x = 0; x < getWidth(); ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			grid.get(x, y).alive = grid.get(x, y).isConnectionSource();
		}
		
		for (Position sourcePos : sources)
		{
			branchConnections(sourcePos);
		}
	}
	
	private void branchConnections(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		Set<Position> openSet = new HashSet<Position>();
		openSet.add(pos);
		
		while (!openSet.isEmpty())
		{
			Iterator<Position> itr = openSet.iterator();
			Position current = itr.next();
			itr.remove();
			
			for (Position adj : current.get4Neighbors())
			{
				if (!bounds.contains(adj))
					continue;
				
				Spot neighborSpot = grid.get(adj);
				
				if (neighborSpot.isTube() && !neighborSpot.alive)
				{
					neighborSpot.alive = true;
					openSet.add(adj);
				}
			}
		}
	}
	
	public boolean isBulldozed(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return costMap.isFree(pos) && tileSet.isBulldozed(grid.get(pos).tileCode);
	}
	
	public void bulldoze(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		clearFixture(pos);
		costMap.setZero(pos);
		grid.get(pos).tileCode = tileSet.getBulldozedTile();
		refreshPanel(new Region(pos).stretch(1));
	}
	
	public void clearFixture(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
		Spot spot = grid.get(pos);
		spot.fixture = null;
		spot.fixtureHP = 0;
		
		Position adj;
		adj = pos.shift(+0, -1);
		
		if (bounds.contains(adj))
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
		
		if (bounds.contains(adj))
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
		
		if (bounds.contains(adj))
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
		
		if (bounds.contains(adj))
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
		
		spot.alive = false;
		assessConnections();
	}
	
	public boolean hasFixture(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).fixture != null;
	}
	
	public boolean hasWall(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).fixture == Fixture.WALL;
	}
	
	public boolean hasTube(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).fixture == Fixture.TUBE;
	}
	
	public boolean hasGeyser(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).fixture == Fixture.GEYSER;
	}
	
	public boolean hasMinePlatform(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		return grid.get(pos).fixture == Fixture.MINE_PLATFORM;
	}
	
	public int getFixtureHP(Position pos)
	{
		if (!bounds.contains(pos))
			return 0;
		
		Spot spot = grid.get(pos);
		
		if (!(spot.fixture == Fixture.TUBE || spot.fixture == Fixture.WALL))
			throw new IllegalStateException("fixture doesn't have hp");
		
		return spot.fixtureHP;
	}
	
	public void setFixtureHP(Position pos, int hp)
	{
		if (!bounds.contains(pos))
			return;
		
		Spot spot = grid.get(pos);
		
		if (!(spot.fixture == Fixture.TUBE || spot.fixture == Fixture.WALL))
			throw new IllegalStateException("fixture doesn't have hp");
		
		if (spot.fixture == Fixture.WALL)
		{
			double hpFactor = hp / (double) WALL_MAX_HP;
			String[] tileCodeParts = spot.tileCode.split("/");
			String neihborsString = tileCodeParts[tileCodeParts.length - 1];
			Neighbors neighbors = Neighbors.valueOf(neihborsString);
			HealthBracket health = HealthBracket.getBracket(hpFactor);
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
		
		if (bounds.contains(n) && hasWall(n))
			neighbors = neighbors.add(Neighbors.N);
		
		if (bounds.contains(s) && hasWall(s))
			neighbors = neighbors.add(Neighbors.S);
		
		if (bounds.contains(w) && hasWall(w))
			neighbors = neighbors.add(Neighbors.W);
		
		if (bounds.contains(e) && hasWall(e))
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
		
		if (bounds.contains(n) && (hasTube(n) || structureOccupies(n)))
			neighbors = neighbors.add(Neighbors.N);
		
		if (bounds.contains(s) && (hasTube(s) || structureOccupies(s)))
			neighbors = neighbors.add(Neighbors.S);
		
		if (bounds.contains(w) && (hasTube(w) || structureOccupies(w)))
			neighbors = neighbors.add(Neighbors.W);
		
		if (bounds.contains(e) && (hasTube(e) || structureOccupies(e)))
			neighbors = neighbors.add(Neighbors.E);
		
		return neighbors;
	}
	
	public CostMap getTerrainCostMap()
	{
		return costMap;
	}
	
	public boolean canPlaceUnit(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		Spot spot = grid.get(pos);
		
		return !costMap.isInfinite(pos)
			&& spot.occupant == null
			&& spot.reservant == null;
	}
	
	public boolean canPlaceUnit(Position pos, Footprint fp)
	{
		if (!bounds.contains(fp.getInnerRegion().move(pos)))
			return false;
		
		for (Position occupied : fp.iterator(pos))
			if (!canPlaceUnit(occupied))
				return false;
		
		return true;
	}
	
	public boolean canPlaceMine(Position pos)
	{
		if (!bounds.contains(pos))
			return false;
		
		pos = pos.shift(1, 0);
		
		if (!bounds.contains(pos))
			return false;
		
		ResourceDeposit deposit = getResourceDeposit(pos);
		return deposit != null && deposit.isCommon();
	}
	
	public boolean willConnect(Position pos, Footprint fp)
	{
		if (!bounds.contains(fp.getInnerRegion().move(pos)))
			return false;
		
		for (Position occupied : fp.iterator(pos))
			for (Position neighbor : occupied.get4Neighbors())
				if (bounds.contains(neighbor) && isAlive(neighbor))
					return true;
		
		for (Position relativeTubePos : fp.getTubePositions())
			for (Position neighbor : relativeTubePos.shift(pos.x, pos.y).get4Neighbors())
				if (bounds.contains(neighbor) && isAlive(neighbor))
					return true;
		
		return false;
	}
	
	public void putUnit(Unit unit, Position pos)
	{
		if (!canPlaceUnit(pos, unit.getFootprint()))
			throw new IllegalStateException("can't place unit " + pos);
		
		if (unit.getContainer() != null)
			throw new IllegalStateException("Unit already in a UnitLayer");
		
		unit.setPosition(pos);
		unit.setContainer(this);
		
		for (Position occupied : unit.getFootprint().iterator(pos))
		{
			Spot spot = grid.get(occupied);
			spot.occupant = unit;
			
			if (unit.getFootprint() != Footprint.VEHICLE)
			{
				spot.alive = false;
				
				if (unit.isConnectionSource())
					sources.add(occupied);
			}
		}
		
		units.add(unit);
		
		if (unit.isStructure() || unit.getType().isGuardPostType())
		{
			Region outer = unit.getFootprint()
							   .getInnerRegion()
							   .move(pos)
							   .stretch(1);
			
			for (Position bullPos : outer)
			{
				boolean contained = bounds.contains(bullPos);
				
				if (contained && !hasFixture(bullPos))
					bulldoze(bullPos);
			}
			
			for (Position tubePos0 : unit.getFootprint().getTubePositions())
			{
				Position tubePos = tubePos0.shift(pos.x, pos.y);
				
				if (!bounds.contains(tubePos))
					continue;
				
				boolean occupied = isOccupied(tubePos)
					&& (grid.get(tubePos).occupant.isStructure()
				|| grid.get(tubePos).occupant.getType().isGuardPostType());
				
				if (!occupied && !hasFixture(tubePos))
					putTube(tubePos);
			}
		}
		
		assessConnections();
		
		if (unit.isMine())
		{
			grid.get(pos).fixture = Fixture.MINE_PLATFORM;
			refreshPanel(pos);
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
		
		return unit != null && (unit.isStructure() || unit.getType().isGuardPostType());
	}
	
	public Unit getReservant(Position pos)
	{
		return grid.get(pos).reservant;
	}
	
	public Unit getUnit(Position pos)
	{
		
		
		return grid.get(pos).occupant;
	}
	
	public Set<Unit> getUnits(Region region)
	{
		if (! bounds.contains(region))
			throw new IndexOutOfBoundsException();
		
		Set<Unit> occupants = new HashSet<Unit>();
		
		for (Spot spot : grid.iterator(region))	
			if (spot.occupant != null)
				occupants.add(spot.occupant);
		
		return occupants;
	}
	
	public void remove(Unit unit)
	{
		if (! contains(unit))
			throw new NoSuchElementException();
		
		Position pos = unit.getPosition();
		
		if (unit.isMine())
		{
			clearFixture(pos);
		}
		
		for (Position fpPos : unit.getFootprint().iterator(pos))
		{
			grid.get(fpPos).occupant = null;
			grid.get(fpPos).alive = false;
			sources.remove(fpPos);
		}
		
		for (Position rPos : unit.getReservations())
		{
			grid.get(rPos).reservant = null;
		}
		
//		unit.setPosition(null);
//		unit.setContainer(null);
		
		units.remove(unit);
		assessConnections();
		
		if (unit.isStructure())
		{
			Region outer = unit.getFootprint()
							   .getInnerRegion()
							   .move(pos)
							   .stretch(1);
			refreshPanel(outer);
		}
	}
	
	public void clearAllUnits()
	{
		for (Unit unit : units.toArray(new Unit[0]))
		{
			remove(unit);
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
		
		if ((!bounds.contains(next)) || costMap.isInfinite(next))
			return false;
		
		Position dest = dir.apply(pos);
		
		if (!(bounds.contains(dest)
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
		if (!bounds.contains(pos))
			return;
		
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
		if (!bounds.contains(pos))
			return;
		
		final Unit holder = grid.get(pos).reservant;
		
		if (holder != null && !holder.equals(unit))
			throw new IllegalStateException("pos reserved");
		
		grid.get(pos).reservant = unit;
		unit.getReservations().add(pos);
	}
	
	public void unreserve(Position pos)
	{
		if (!bounds.contains(pos))
			return;
		
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
	
	public RIterator<Unit> getUnitIterator()
	{
		return getUnitIterator(false);
	}
	
	public RIterator<Unit> getUnitIterator(boolean zorder)
	{
		Set<Unit> copy = zorder ? new TreeSet<Unit>(Z_ORDER_UNIT)
								: new HashSet<Unit>();
		
		copy.addAll(units);
		return RIterator.iterate(copy);
	}
	
	// unused - as it should be for optimality's sake
	@SuppressWarnings("unused")
	private void refreshPanel()
	{
		synchronized (panels)
		{
			for (DisplayPanel panel : panels)
				panel.refresh();
		}
	}

	private void refreshPanel(Position pos)
	{
		synchronized (panels)
		{
			for (DisplayPanel panel : panels)
				panel.refresh(pos);
		}
	}

	private void refreshPanel(Region region)
	{
		synchronized (panels)
		{
			for (DisplayPanel panel : panels)
				panel.refresh(region);
		}
	}
	
	/**
	 * Compares two Units for z-order.
	 * 
	 * Sorted just like Z_ORDER_POS but all the structures on a row
	 * come after all the vehicles on the same row.
	 */
	private static final Comparator<Unit> Z_ORDER_UNIT =
	new Comparator<Unit>()
	{
		private final int A =  1;
		private final int B = -1;
		
		public int compare(Unit a, Unit b)
		{
			final Position posA = a.getPosition();
			final Position posB = b.getPosition();
			
			if (posA.y > posB.y) return A;
			if (posA.y < posB.y) return B;
			
			final boolean structA = a.isStructure() && !a.isMine();
			final boolean structB = b.isStructure() && !b.isMine();
			
			if ( structA && !structB) return A;
			if (!structA &&  structB) return B;
			
			if (posA.x > posB.x) return A;
			if (posA.x < posB.x) return B;
			
			if (b.isMine()) return A;
			if (a.isMine()) return B;
			
			return 0; // Should this be possible?
		}
	};
}
