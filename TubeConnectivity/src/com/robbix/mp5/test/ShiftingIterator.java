package com.robbix.mp5.test;

import java.util.Iterator;

public class ShiftingIterator implements Iterator<Position>
{
	private Iterator<Position> itr;
	private int dx,dy;
	
	public ShiftingIterator(Iterator<Position> itr, int dx, int dy){
		this.itr=itr;this.dx=dx;this.dy=dy;
	}
	
	public boolean hasNext(){return itr.hasNext();}
	public void remove(){throw new UnsupportedOperationException();}
	public Position next(){return itr.next().shift(dx, dy);}
}
