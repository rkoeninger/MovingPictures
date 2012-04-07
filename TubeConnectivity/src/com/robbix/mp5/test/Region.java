package com.robbix.mp5.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class Region implements Iterable<Position>
{
	public final int x;
	public final int y;
	public final int w;
	public final int h;
	public final int area;
	
	public int getX()     { return x; }
	public int getY()     { return y; }
	public int getWidth() { return w; }
	public int getHeight(){ return h; }
	
	public Region()
	{
		this.x = 0;
		this.y = 0;
		this.w = 0;
		this.h = 0;
		this.area = w * h;
	}
	
	public Region(int x, int y, int w, int h)
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
		this.area = w * h;
	}
	
	public Region(Position a, Position b)
	{
		this.x = Math.min(a.x, b.x);
		this.y = Math.min(a.y, b.y);
		this.w = Math.abs(a.x - b.x) + 1; // + 1 needed so b is included
		this.h = Math.abs(a.y - b.y) + 1; // + 1 needed so b is included
		this.area = w * h;
	}
	
	public Region shift(Position pos)
	{
		return new Region(x + pos.x, y + pos.y, w, h);
	}
	
	public boolean contains(Position pos)
	{
		return (pos.x >= x) && (pos.x < x + w)
		    && (pos.y >= y) && (pos.y < y + h);
	}
	
	public boolean contains(Region reg)
	{
		return (reg.x >= x) && (reg.x + reg.w <= x + w)
	    	&& (reg.y >= y) && (reg.y + reg.h <= y + h);
	}
	
	public boolean isEmpty()
	{
		return (area == 0);
	}
	
	public boolean intersects(Region that)
	{
		return getIntersection(that).area != 0;
	}
	
	public Region getIntersection(Region that)
	{
		final int x1 = Math.max(this.x, that.x);
		final int y1 = Math.max(this.y, that.y);
		final int x2 = Math.min(this.x + this.w, that.x + that.w);
		final int y2 = Math.min(this.y + this.h, that.y + that.h);
		
		return new Region(x1, y1, x2 - x1, y2 - y1);
	}
	
	public Region getUnion(Region that)
	{
		final int x1 = Math.min(this.x, that.x);
		final int y1 = Math.min(this.y, that.y);
		final int x2 = Math.max(this.x + this.w, that.x + that.w);
		final int y2 = Math.max(this.y + this.h, that.y + that.h);
		
		return new Region(x1, y1, x2 - x1, y2 - y1);
	}
	
	public boolean equals(Object obj)
	{
		if (! (obj instanceof Region)) return false;

		Region that = (Region) obj;
		
		return (this.x == that.x)
			&& (this.y == that.y)
			&& (this.w == that.w)
			&& (this.h == that.h);
	}
	
	public int hashCode()
	{
		return x ^ y ^ w ^ h;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ") " + w + " by " + h;
	}
	
	public Set<Position> getOutline()
	{
		Set<Position> outline = new HashSet<Position>(w * 2 + h * 2);
		
		for (int horz = x; horz < x + w; ++horz)
		{
			outline.add(new Position(horz, y - 1));
			outline.add(new Position(horz, y + h));
		}
		
		for (int vert = y; vert < y + h; ++vert)
		{
			outline.add(new Position(x - 1, vert));
			outline.add(new Position(x + w, vert));
		}
		
		return outline;
	}
	
	public Iterator<Position> iterator()
	{
		return new PositionIterator();
	}

	public Iterator<Position> iterator(Position origin)
	{
		return new ShiftingIterator(new PositionIterator(), origin.x, origin.y);
	}
	
	private class PositionIterator implements Iterator<Position>
	{
		private int index = 0;
		
		public boolean hasNext()
		{
			return index < area;
		}

		public Position next()
		{
			if (index >= area) throw new NoSuchElementException();
			
			try{return new Position(index % w, index / w);}
			finally{index++;}
		}

		public void remove(){throw new UnsupportedOperationException();}
	}
}
