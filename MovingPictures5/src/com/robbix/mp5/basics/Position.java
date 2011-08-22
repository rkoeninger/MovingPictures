package com.robbix.mp5.basics;

/**
 * An immutable position on a grid. Measured in relative grid co-ordinates.
 * 
 * Compare to java.awt.Point, which is a position in absolute pixels
 * and is mutable.
 * 
 * @author bort
 */
// TODO: cache Position objects - have a static getPosition(x, y) method
public class Position
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

	/**
	 * Readdresses this Position's coordinates using the given Position
	 * as the relative origin.
	 */
	public Position shift(Position pos)
	{
		return new Position(x + pos.x, y + pos.y);
	}
	
	/**
	 * Finds the distance between two Positions as a floating-point value.
	 */
	public double getDistance(Position that)
	{
		return Math.hypot(this.x - that.x, this.y - that.y);
	}

	/**
	 * Finds the distance between this Position and the given
	 * co-ordinate pair as a floating-point value.
	 */
	public double getDistance(int x, int y)
	{
		return Math.hypot(this.x - x, this.y - y);
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
}
