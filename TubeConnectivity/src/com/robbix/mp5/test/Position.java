package com.robbix.mp5.test;

import java.util.HashSet;
import java.util.Set;

public class Position
{
	public final int x;
	public final int y;
	
	public int getX(){ return x; }
	public int getY(){ return y; }
	
	public Position()
	{
		this.x = 0;
		this.y = 0;
	}
	
	public Position(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Set<Position> getNeighbors()
	{
		Set<Position> neighbors = new HashSet<Position>();
		
		neighbors.add(new Position(x - 1, y));
		neighbors.add(new Position(x + 1, y));
		neighbors.add(new Position(x, y - 1));
		neighbors.add(new Position(x, y + 1));
		
		return neighbors;
	}
	
	public boolean isInLine(Position pos)
	{
		return x == pos.x || y == pos.y;
	}
	
	public Position shift(int dx, int dy)
	{
		return new Position(x + dx, y + dy);
	}
	
	public double getDistance(Position that)
	{
		return Math.sqrt(Math.pow(this.x - that.x, 2) + Math.pow(this.y - that.y, 2));
	}
	
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		
		if (obj == null || ! (obj instanceof Position))
			return false;
		
		Position that = (Position) obj;
		
		return (this.x == that.x) && (this.y == that.y);		
	}
	
	public int hashCode()
	{
		return x ^ y;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	public Position shift(Position origin)
	{
		return shift(origin.x, origin.y);
	}
}
