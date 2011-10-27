package com.robbix.mp5.basics;

import static com.robbix.mp5.basics.Direction.E;
import static com.robbix.mp5.basics.Direction.N;
import static com.robbix.mp5.basics.Direction.S;
import static com.robbix.mp5.basics.Direction.W;

import java.awt.Graphics;
import java.awt.Point;
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
	
	public void draw(Graphics g, ColorScheme colors, Point offset, int edgeSize)
	{
		g.setColor(colors.fill);
		g.fillRect(
			x * edgeSize + offset.x,
			y * edgeSize + offset.y,
			edgeSize,
			edgeSize
		);
		
		g.setColor(colors.edge);
		g.drawRect(
			x * edgeSize + offset.x,
			y * edgeSize + offset.y,
			edgeSize,
			edgeSize
		);
	}
	
	public void drawEdge(Graphics g, ColorScheme colors, Point offset, int edgeSize, Direction dir)
	{
		g.setColor(colors.edge);
		int x0 = x * edgeSize + offset.x;
		int y0 = y * edgeSize + offset.y;
		int x1 = (x + 1) * edgeSize + offset.x;
		int y1 = (y + 1) * edgeSize + offset.y;
		
		switch (dir)
		{
		case N: g.drawLine(x0, y0, x1, y0); break;
		case S: g.drawLine(x0, y1, x1, y1); break;
		case E: g.drawLine(x1, y0, x1, y1); break;
		case W: g.drawLine(x0, y0, x0, y1); break;
		}
	}
	
	public void drawEdges(Graphics g, ColorScheme colors, Point offset, int edgeSize, Neighbors neighbors)
	{
		g.setColor(colors.edge);
		if (neighbors.has(N)) drawEdge(g, colors, offset, edgeSize, N);
		if (neighbors.has(S)) drawEdge(g, colors, offset, edgeSize, S);
		if (neighbors.has(E)) drawEdge(g, colors, offset, edgeSize, E);
		if (neighbors.has(W)) drawEdge(g, colors, offset, edgeSize, W);
	}
}
