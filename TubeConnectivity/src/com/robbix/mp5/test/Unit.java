package com.robbix.mp5.test;

import java.util.Iterator;

public class Unit
{
	private Position pos;
	private Footprint fp;
	
	private boolean cc;
	
	private GameMap map;
	
	public Unit(Footprint fp, boolean cc)
	{
		this.fp = fp;
		this.cc = cc;
	}
	
	public Unit(Footprint fp)
	{
		this(fp, false);
	}
	
	public Position getPosition()
	{
		return pos;
	}
	
	public void setPosition(Position pos)
	{
		this.pos = pos;
	}
	
	public GameMap getMap()
	{
		return map;
	}
	
	public void setMap(GameMap map)
	{
		this.map = map;
	}
	
	public Footprint getFootprint()
	{
		return fp;
	}
	
	public boolean isStructure()
	{
		return true;
	}
	
	public boolean isConnectionSource()
	{
		return cc;
	}
	
	public boolean isConnected()
	{
		return map.isAlive(pos);
	}
	
	public Iterator<Position> iterateOccupied()
	{
		return getFootprint().getInnerRegion().iterator(getPosition());
	}
}
