package com.robbix.utils;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Does not extend Region as it is not truly rectangular.
 * 
 * @author bort
 */
public class LShapedRegion implements RIterable<Position>
{
	private Position begin;
	private Position elbow;
	private Position end;
	private Direction leg1Dir;
	private Direction leg2Dir;
	private int leg1Len;
	private int leg2Len;
	
	public final int length;

	public int getLength() { return length; }
	
	public Position getFirstEnd() { return begin; }
	public Position getElbow()    { return elbow; }
	public Position getSecondEnd(){ return end;   }

	public Direction getFirstLegDirection() { return leg1Dir; }
	public Direction getSecondLegDirection(){ return leg2Dir; }

	public int getFirstLegLength() { return leg1Len; }
	public int getSecondLegLength(){ return leg2Len; }
	
	public LShapedRegion(Position begin, Position elbow, Position end)
	{
		if (! begin.isColinear(elbow))
			throw new IllegalArgumentException("must be colinear");
		
		if (! end.isColinear(elbow))
			throw new IllegalArgumentException("must be colinear");
		
		leg1Dir = Direction.getDirection(begin, elbow);
		leg2Dir = Direction.getDirection(end, elbow);
		
		if (leg1Dir == null)
			leg1Dir = Direction.E;
		
		if (leg2Dir == null)
			leg2Dir = Direction.E;
		
		leg1Len = max(abs(begin.x - elbow.x), abs(begin.y - elbow.y)) - 1;
		leg2Len = max(abs(elbow.x - end.x), abs(elbow.y - end.y)) - 1;
		
		this.begin = begin;
		this.elbow = elbow;
		this.end = end;
		
		length = leg1Len + leg2Len - 1;
	}
	
	public LShapedRegion(Position begin, Position end, boolean verticalFirst)
	{
		this(
			begin,
			verticalFirst ? new Position(begin.x, end.y) : new Position(end.x, begin.y),
			end
		);
	}
	
	public boolean contains(Position pos)
	{
		if (pos.x == begin.x)
		{
			return isBetween(pos.y, elbow.y, begin.y);
		}
		else if (pos.y == begin.y)
		{
			return isBetween(pos.x, elbow.x, begin.x);
		}
		else if (pos.x == end.x)
		{
			return isBetween(pos.y, elbow.y, end.y);
		}
		else if (pos.y == end.y)
		{
			return isBetween(pos.x, elbow.x, end.x);
		}
		
		return false;
	}
	
	private static boolean isBetween(int val, int end1, int end2)
	{
		return end1 < end2
			? val >= end1 && val <= end2
			: val >= end2 && val <= end1;
	}
	
	public boolean contains(Region region)
	{
		if (region.a == 0)
			return true;
		
		return contains(region.getUpperLeftCorner())
			&& contains(region.getUpperRightCorner())
			&& contains(region.getLowerLeftCorner())
			&& contains(region.getLowerRightCorner());
	}
	
	public RIterator<Position> iterator()
	{
		return new LShapeIterator();
	}
	
	private class LShapeIterator extends RIterator<Position>
	{
		private int leg = 0; // side of L-shape being iterated, 0-1, 2 marks end
		private Position current = begin;
		
		public boolean hasNext()
		{
			return leg != 2;
		}
		
		public Position next()
		{
			checkHasNext();
			Position result = current;
			
			switch (leg)
			{
			case 0:
				if (current.equals(elbow))
				{
					leg = current.equals(end) ? 2 : 1;
					current = leg2Dir.reverse().apply(current);
				}
				else
				{
					current = leg1Dir.apply(current);
				}
				
				return result;
			case 1:
				if (current.equals(end))
				{
					leg = 2;
				}
				else
				{
					current = leg2Dir.reverse().apply(current);
				}
				
				return result;
			default:
				throw new Error();
			}
		}
	}
}
