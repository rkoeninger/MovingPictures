package com.robbix.mp5.basics;

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
	public final Direction dir;
	public final int length;
	
	public LinearRegion(Position begin, Position end)
	{
		super(begin, end);
		
		if (! begin.isColinear(end))
			throw new IllegalArgumentException("Not colinear");
		
		this.begin = begin;
		this.end = end;
		
		dir = Direction.getDirection(begin, end);
		length = max(abs(begin.x - end.x), abs(begin.y - end.y)) + 1;
	}
	
	public RIterator<Position> iterator()
	{
		return new RIterator<Position>()
		{
			private Position current = begin;
			private int index;
			
			public boolean hasNext()
			{
				return index < length;
			}
			
			public Position next()
			{
				checkHasNext();
				
				index++;
				
				try     { return current;               }
				finally { current = dir.apply(current); }
			}
		};
	}
}
