package com.robbix.mp5.basics;

/**
 * Maintains a cache of Position objects in an attempt to reduce instantiations
 * of Position.
 */
public class PositionCache
{
	private Grid<Position> grid;
	
	public PositionCache(int w, int h)
	{
		this.grid = new Grid<Position>(w, h);
	}
	
	public synchronized void prepare()
	{
		for (int x = 0; x < grid.w; ++x)
		for (int y = 0; y < grid.h; ++y)
			if (grid.isNull(x, y))
				grid.set(x, y, new CachedPosition(x, y));
	}
	
	public synchronized Position get(int x, int y)
	{
		if (x < 0 || x >= grid.w || y < 0 || y >= grid.h)
			return new Position(x, y);
		
		Position pos = grid.get(x, y);
		
		if (pos == null)
		{
			pos = new CachedPosition(x, y);
			grid.set(x, y, pos);
		}
		
		return pos;
	}
	
	private class CachedPosition extends Position
	{
		public CachedPosition(int x, int y)
		{
			super(x, y);
		}
		
		public Position shift(int dx, int dy)
		{
			return PositionCache.this.get(this.x + dx, this.y + dy);
		}
	}
}
