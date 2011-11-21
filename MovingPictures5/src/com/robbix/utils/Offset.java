package com.robbix.utils;

import java.awt.Point;

/**
 * The class Offset represents a pair of x- and y-axis co-ordinate offsets.
 * 
 * The integer units of this class are understood to represent pixel-offsets,
 * as opposed to grid-position-offsets and should not be used for the latter.
 * 
 * It can be used in conjuction with java.awt.Point,
 * but not com.robbix.mp5.basics.Position
 */
public class Offset
{
	public final int dx;
	public final int dy;
	
	public int getDX(){ return dx; }
	public int getDY(){ return dy; }
	
	public int getDX(int scale)
	{
		return scale < 0 ? dx >> -scale : dx << scale;
	}
	
	public int getDY(int scale)
	{
		return scale < 0 ? dy >> -scale : dy << scale;
	}
	
	public Offset(int dx, int dy)
	{
		this.dx = dx;
		this.dy = dy;
	}
	
	public Offset()
	{
		this.dx = 0;
		this.dy = 0;
	}
	
	public Offset scale(int scale)
	{
		return new Offset(
			scale < 0 ? dx >> -scale : dx << scale,
			scale < 0 ? dy >> -scale : dy << scale
		);
	}
	
	public Offset add(Offset that)
	{
		return new Offset(dx + that.dx, dy + that.dy);
	}
	
	public Offset subtract(Offset that)
	{
		return new Offset(dx - that.dx, dy - that.dy);
	}
	
	public Point apply(Point p)
	{
		return new Point(p.x + dx, p.y + dy);
	}
	
	public int hashCode()
	{
		return (dx * 43721) ^ (dy * 14197);
	}
	
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Offset)
		{
			Offset that = (Offset) obj;
			return dx == that.dx && dy == that.dy;
		}
		
		return false;
	}
	
	public String toString()
	{
		return "[" + dx + ", " + dy + "]";
	}
}
