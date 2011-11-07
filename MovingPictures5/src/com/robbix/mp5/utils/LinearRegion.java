package com.robbix.mp5.utils;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * A linear sequence of Positions, speicifed by two endpoints.
 * The two endpoints must reside on the same row or column of the grid.
 * 
 * @author bort
 */
public class LinearRegion extends Region
{
	public final Position begin;
	public final Position end;
	public final int length;
	
	public LinearRegion(Position begin, Position end)
	{
		super(begin, end);
		
		if (! begin.isColinear(end))
			throw new IllegalArgumentException("Not colinear");
		
		this.begin = begin;
		this.end = end;
		
		length = max(abs(begin.x - end.x), abs(begin.y - end.y)) + 1;
	}
}
