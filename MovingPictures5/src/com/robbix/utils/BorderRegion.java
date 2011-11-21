package com.robbix.utils;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class BorderRegion implements RIterable<Position>
{
	public final int x;
	public final int y;
	public final int w;
	public final int h;
	public final int a;
	
	public BorderRegion(int x, int y, int w, int h)
	{
		if (w < 0)
		{
			x += w;
			w = -w;
		}
		
		if (h < 0)
		{
			y += h;
			h = -h;
		}
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.a = (w * 2) + (max(0, h - 2) * 2);
	}
	
	public BorderRegion(Position a, Position b)
	{
		// + 1 needed so b is included
		this(min(a.x, b.x), min(a.y, b.y), abs(a.x - b.x) + 1, abs(a.y - b.y) + 1);
	}
	
	public BorderRegion(Region region)
	{
		this(region.x, region.y, region.w, region.h);
	}
	
	public boolean contains(Position pos)
	{
		// Aligned vertically
		if (pos.x == x || pos.x == x + w - 1)
		{
			return isBetween(pos.y, y, y + h);
		}
		
		// Aligned horizontally
		if (pos.y == y || pos.y == y + h - 1)
		{
			return isBetween(pos.x, x, x + w);
		}
		
		return false;
	}
	
	private static boolean isBetween(int val, int end1, int end2)
	{
		return end1 < end2
			? val >= end1 && val <= end2
			: val >= end2 && val <= end1;
	}
	
	public RIterator<Position> iterator()
	{
		return iterator(true);
	}
	
	public RIterator<Position> iterator(boolean clockwise)
	{
		return new SpiralIterator(clockwise);
	}
	
	/**
	 * Iterates over elements in this BorderRegion. Always starts at (x, y).
	 * Goes clockwise or counter-clockwise.
	 */
	private class SpiralIterator extends RIterator<Position>
	{
		private int leg = 0; // side of border being iterated, 0-3, 4 marks end
		private int index = 0;
		
		/*
		 * The variables j and k are abstractions of x and y. They are used
		 * in a generalization of two older algorithms, one for clockwise,
		 * one for counter-clockwise. Their next() methods were similar enough
		 * to simplifiy down to this generalization.
		 */
		
		private boolean clockwise;
		private int j0; // origin of j is dimension 1; x when clockwise, y when ccw
		private int k0; // origin of k is dimension 2; y when clockwise, x when ccw
		private int jn; // length in dimension 1; w when clockwise, h when ccw
		private int kn; // length in dimension 2; h when clockwise, w when ccw
		
		public SpiralIterator(boolean clockwise)
		{
			this.clockwise = clockwise;
			j0 = clockwise ? x : y;
			k0 = clockwise ? y : x;
			jn = clockwise ? w : h;
			kn = clockwise ? h : w;
		}
		
		private Position pos(int j, int k)
		{
			return clockwise ? new Position(j, k) : new Position(k, j);
		}
		
		public boolean hasNext()
		{
			if (a == 0)
				return false;
			
			return leg != 4;
		}
		
		public Position next()
		{
			checkHasNext();
			Position result = null;
			
			switch (leg)
			{
			case 0:
				result = pos(j0 + index, k0);
				index++;
				
				if (index >= jn)
				{
					leg   = kn > 2 ? 1 : 2;
					index = kn > 2 ? 1 : 0;
				}
				
				return result;
				
			case 1:
				result = pos(j0 + jn - 1, k0 + index);
				index++;
				
				if (index >= kn - 1)
				{
					leg   = 2;
					index = 0;
				}
				
				return result;
				
			case 2:
				result = pos(j0 + jn - 1 - index, k0 + kn - 1);
				index++;
				
				if (index >= jn)
				{
					leg   = kn > 2 ? 3 : 4;
					index = kn > 2 ? 1 : 0;
				}
				
				return result;
				
			case 3:
				result = pos(j0, k0 + kn - 1 - index);
				index++;
				
				if (index >= kn - 1)
				{
					leg   = 4;
					index = 0;
				}
				
				return result;
				
			default:
				throw new Error();
			}
		}
	}
}
