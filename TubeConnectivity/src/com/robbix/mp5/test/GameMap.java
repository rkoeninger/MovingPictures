package com.robbix.mp5.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GameMap
{
	private static class Spot
	{
		public Unit occupier;
		
		public boolean source;
		public boolean tube;
		public boolean alive;
	}
	
	private Spot[][] grid;
	public final int w;
	public final int h;
	public final Region reg;
	
	private Set<Position> sources;
	private Set<Unit> units;
	
	public GameMap(int w, int h)
	{
		grid = new Spot[w][h];
		this.w = w;
		this.h = h;
		this.reg = new Region(0, 0, w, h);
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			grid[x][y] = new Spot();
		
		sources = new HashSet<Position>();
		units = new HashSet<Unit>();
	}
	
	public int getWidth()
	{
		return w;
	}
	
	public int getHeight()
	{
		return h;
	}
	
	public Region getRegion()
	{
		return reg;
	}
	
	public boolean contains(Position pos)
	{
		return pos.x >= 0 && pos.y >= 0 && pos.x < w && pos.y < h;
	}
	
	public boolean isOccupied(Position pos)
	{
		return grid[pos.x][pos.y].occupier != null;
	}
	
	public boolean isTube(Position pos)
	{
		return grid[pos.x][pos.y].tube;
	}
	
	public boolean isSource(Position pos)
	{
		return grid[pos.x][pos.y].source;
	}
	
	public boolean isAlive(Position pos)
	{
		return grid[pos.x][pos.y].alive;
	}
	
	public void putTube(Position pos)
	{
		if (grid[pos.x][pos.y].occupier != null)
		{
			remove(grid[pos.x][pos.y].occupier);
		}
		
		grid[pos.x][pos.y].source = false;
		grid[pos.x][pos.y].tube   = true;
		grid[pos.x][pos.y].alive  = false;
		
		sources.remove(pos);
		
		for (Position neighbor : pos.getNeighbors())
			if (contains(neighbor) && grid[neighbor.x][neighbor.y].alive)
				grid[pos.x][pos.y].alive  = true;
		
		if (grid[pos.x][pos.y].alive)
			branchConnections(pos);
	}
	
	public void putSource(Position pos)
	{
		if (grid[pos.x][pos.y].occupier != null)
		{
			remove(grid[pos.x][pos.y].occupier);
		}
		
		grid[pos.x][pos.y].source = true;
		grid[pos.x][pos.y].tube   = false;
		grid[pos.x][pos.y].alive  = true;
		
		sources.add(pos);
		
		branchConnections(pos);
	}
	
	public void remove(Position pos)
	{
		if (grid[pos.x][pos.y].occupier != null)
		{
			remove(grid[pos.x][pos.y].occupier);
		}
		else
		{
			grid[pos.x][pos.y].source = false;
			grid[pos.x][pos.y].tube   = false;
			grid[pos.x][pos.y].alive  = false;
			
			sources.remove(pos);
		}
		
		assessConnections();
	}
	
	public void put(Unit unit, Position pos)
	{
		if (!reg.contains(unit.getFootprint().getInnerRegion()))
			throw new IndexOutOfBoundsException();
		
		unit.setPosition(pos);
		Iterator<Position> fpItr = unit.iterateOccupied();
		
		while (fpItr.hasNext())
		{
			Position fpPos = fpItr.next();
			
			grid[fpPos.x][fpPos.y].occupier = unit;
			grid[fpPos.x][fpPos.y].source   = unit.isConnectionSource();
			grid[fpPos.x][fpPos.y].tube     = true;
			grid[fpPos.x][fpPos.y].alive    = false;
			
			if (unit.isConnectionSource())
				sources.add(fpPos);
		}
		
		for (Position tubePos : unit.getFootprint().getTubePositions())
			putTube(tubePos.shift(unit.getPosition()));
		
		units.add(unit);
		unit.setMap(this);
		
		assessConnections();
	}
	
	public Unit getUnit(Position pos)
	{
		return grid[pos.x][pos.y].occupier;
	}
	
	public void remove(Unit unit)
	{
		Iterator<Position> fpItr = unit.iterateOccupied();
		
		while (fpItr.hasNext())
		{
			Position fpPos = fpItr.next();
			
			grid[fpPos.x][fpPos.y].occupier = null;
			grid[fpPos.x][fpPos.y].source   = false;
			grid[fpPos.x][fpPos.y].tube     = false;
			grid[fpPos.x][fpPos.y].alive    = false;
			
			sources.remove(fpPos);
		}
		
		units.remove(unit);
		unit.setMap(null);
		
		assessConnections();
	}
	
	private void assessConnections()
	{
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			grid[x][y].alive = grid[x][y].source;
		
		for (Position sourcePos : sources)
			branchConnections(sourcePos);
	}
	
	private void branchConnections(Position pos)
	{
		Set<Position> openSet = new HashSet<Position>();
		openSet.add(pos);
		
		while (!openSet.isEmpty())
		{
			Iterator<Position> itr = openSet.iterator();
			Position current = itr.next();
			itr.remove();
			
			for (Position neighbor : current.getNeighbors())
			{
				if (!contains(neighbor))
					continue;
				
				Spot neighborSpot = grid[neighbor.x][neighbor.y];
				
				if (neighborSpot.tube && !neighborSpot.alive)
				{
					neighborSpot.alive = true;
					openSet.add(neighbor);
				}
			}
		}
	}
	
	public static GameMap load(File file) throws IOException
	{
		DataInputStream in = new DataInputStream(
							 new BufferedInputStream(
							 new FileInputStream(file)));
		
		int w = in.readShort();
		int h = in.readShort();
		
		GameMap map = new GameMap(w, h);
		
		for (int type = -1; (type = in.read()) >= 0;)
		{
			int x = in.readShort();
			int y = in.readShort();
			Position pos = new Position(x, y);
			
			switch (type)
			{
			case TUBE:
				map.putTube(pos);
				break;
				
			case SOURCE:
				map.putSource(pos);
				break;
				
			case UNIT:
				boolean source = in.readBoolean();
				Footprint fp = Footprint.serialTable.get(in.readInt());
				map.put(new Unit(fp, source), pos);
				break;
			}
		}
		
		in.close();
		
		return map;
	}
	
	public void save(File file) throws IOException
	{
		DataOutputStream out = new DataOutputStream(
							   new BufferedOutputStream(
							   new FileOutputStream(file), w * h * 8));
		
		out.writeShort(w);
		out.writeShort(h);
		
		for (Unit unit : units)
		{
			out.write(UNIT);
			out.writeShort(unit.getPosition().getX());
			out.writeShort(unit.getPosition().getY());
			out.writeBoolean(unit.isConnectionSource());
			out.writeInt(unit.getFootprint().getSerial());
		}
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
		{
			Position pos = new Position(x, y);
			
			if (isTube(pos) && !isOccupied(pos))
			{
				out.write(TUBE);
				out.writeShort(x);
				out.writeShort(y);
			}
			else if (isSource(pos) && !isOccupied(pos))
			{
				out.write(SOURCE);
				out.writeShort(x);
				out.writeShort(y);
			}
		}
		
		out.flush();
		out.close();
	}
	
	private static final int TUBE = 1, SOURCE = 2, UNIT = 3;
}
