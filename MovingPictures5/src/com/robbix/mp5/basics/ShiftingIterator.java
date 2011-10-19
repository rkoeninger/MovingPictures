package com.robbix.mp5.basics;

import java.util.Iterator;

public class ShiftingIterator extends RIterator<Position>
{
	private Iterator<Position> itr;
	private int dx, dy;
	
	public ShiftingIterator(Iterator<Position> itr, int dx, int dy)
	{
		if (itr == null)
			throw new IllegalArgumentException("Null Iterator");
		
		this.itr = itr;
		this.dx = dx;
		this.dy = dy;
	}
	
	public boolean hasNext()
	{
		return itr.hasNext();
	}
	
	public Position next()
	{
		return itr.next().shift(dx, dy);
	}
}
