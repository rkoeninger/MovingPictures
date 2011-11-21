package com.robbix.utils;

import static java.lang.Math.hypot;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * An immutable position on a grid. Measured in relative grid co-ordinates.
 * 
 * Compare to java.awt.Point, which is a position in absolute pixels
 * and is mutable.
 * 
 * @author bort
 */
public class Position implements RIterable<Position>
{
	/**
	 * The x-coordinate of this Position.
	 */
	public final int x;
	
	/**
	 * The y-coordinate of this Position.
	 */
	public final int y;
	
	// Extra functions provided for consitency with other naming conventions.
	public int getX(){ return x; }
	public int getY(){ return y; }
	
	/**
	 * Creates a Position representing the point (x, y).
	 */
	public Position(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns a new Position whose coordinates are shifted by &lt;dx, dy&gt;.
	 */
	public Position shift(int dx, int dy)
	{
		return new Position(x + dx, y + dy);
	}
	
	public Position add(Position pos)
	{
		return new Position(x + pos.x, y + pos.y);
	}
	
	public Position subtract(Position pos)
	{
		return new Position(x - pos.x, y - pos.y);
	}
	
	public Point2D getCenterPoint()
	{
		return new Point2D.Double(x + 0.5, y + 0.5);
	}
	
	/**
	 * Returns a set of this position's four neighbors n, s, e, w.
	 */
	public Set<Position> get4Neighbors()
	{
		Set<Position> neighbors = new HashSet<Position>();
		
		neighbors.add(shift(0, 1));
		neighbors.add(shift(0, -1));
		neighbors.add(shift(1, 0));
		neighbors.add(shift(-1, 0));
		
		return neighbors;
	}

	/**
	 * Returns a set of this position's eight neighbors
	 * n, s, e, w, ne, nw, sw, se.
	 */
	public Set<Position> get8Neighbors()
	{
		Set<Position> neighbors = new HashSet<Position>();
		
		neighbors.add(shift(0, 1));
		neighbors.add(shift(0, -1));
		neighbors.add(shift(1, 0));
		neighbors.add(shift(-1, 0));
		neighbors.add(shift(1, 1));
		neighbors.add(shift(1, -1));
		neighbors.add(shift(-1, 1));
		neighbors.add(shift(-1, -1));
		
		return neighbors;
	}
	
	/**
	 * Returns true if this Position and that Position are aligned
	 * vertically or horizontally.
	 */
	public boolean isColinear(Position that)
	{
		return this.x == that.x || this.y == that.y;
	}
	
	/**
	 * Finds the distance between two Positions as a floating-point value.
	 */
	public double getDistance(Position that)
	{
		return hypot(this.x - that.x, this.y - that.y);
	}

	/**
	 * Finds the distance between this Position and the given
	 * co-ordinate pair as a floating-point value.
	 */
	public double getDistance(int x, int y)
	{
		return hypot(this.x - x, this.y - y);
	}
	
	/**
	 * Returns true if the given object is a Position
	 * and has the same x and y values as this Position.
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (! (obj instanceof Position) || obj == null) return false;
		
		Position that = (Position) obj;
		
		return (this.x == that.x) && (this.y == that.y);		
	}
	
	/**
	 * Returns the hash code for this Position based on x and y members.
	 */
	public int hashCode()
	{
		return (x * 3571) ^ (y * 181081);
	}
	
	/**
	 * Returns a string representation of this Position in the form "(x, y)".
	 */
	public String toString()
	{
		return String.format("(%1$d, %2$d)", x, y);
	}
	
	public RIterator<Position> iterator()
	{
		return new MeIterator();
	}
	
	private class MeIterator extends RIterator<Position>
	{
		private boolean done = false;
		
		public boolean hasNext()
		{
			return !done;
		}
		
		public Position next()
		{
			checkHasNext();
			done = true;
			return Position.this;
		}
	}
}
