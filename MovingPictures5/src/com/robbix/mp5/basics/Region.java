package com.robbix.mp5.basics;

/**
 * An immutable rectangular region on a grid.
 * Measured in relative grid co-ordinates.
 * 
 * Compare to java.awt.Rectangle, which is measured in absolute pixels
 * and is mutable.
 * 
 * @author bort
 */
public class Region implements RIterable<Position>
{
	/**
	 * The x-coordinate of the upper-left corner of this Region.
	 */
	public final int x;

	/**
	 * The y-coordinate of the upper-left corner of this Region.
	 */
	public final int y;
	
	/**
	 * The width of this Region. Measured in the same units as x and y.
	 */
	public final int w;

	/**
	 * The height of this Region. Measured in the same units as x and y.
	 */
	public final int h;
	
	/**
	 * The amount of area contained by this Region. Equal to (w * h).
	 */
	public final int a;
	
	// Extra functions provided for consitency with other naming conventions.
	public int getX()     { return x; }
	public int getY()     { return y; }
	public int getWidth() { return w; }
	public int getHeight(){ return h; }
	public int getArea()  { return a; }
	public int getMaxX()  { return x + w; }
	public int getMaxY()  { return y + h; }
	
	/**
	 * Creates a Region with it's upper-left corner at (0, 0)
	 * and with dimensions 0 by 0.
	 */
	public Region()
	{
		this.x = 0;
		this.y = 0;
		this.w = 0;
		this.h = 0;
		this.a = 0;
	}

	/**
	 * Creates a Region the encompasses the specified area.
	 */
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
		this.a = w * h;
	}
	
	/**
	 * Creates a Region with the specified width and height and an
	 * upper left corner at (0, 0).
	 */
	public Region(int w, int h)
	{
		this(0, 0, w, h);
	}
	
	/**
	 * Creates the smallest Region that contains both Positions a and b.
	 */
	public Region(Position a, Position b)
	{
		this.x = Math.min(a.x, b.x);
		this.y = Math.min(a.y, b.y);
		this.w = Math.abs(a.x - b.x) + 1; // + 1 needed so b is included
		this.h = Math.abs(a.y - b.y) + 1; // + 1 needed so b is included
		this.a = w * h;
	}
	
	/**
	 * Creates a region that contains only the given Position.
	 */
	public Region(Position pos)
	{
		this.x = pos.x;
		this.y = pos.y;
		this.w = 1;
		this.h = 1;
		this.a = 1;
	}
	
	/**
	 * Gets a position representing the upper- and left-most Position
	 * contained by this Region.
	 * 
	 * If this Region is empty, this method returns null.
	 */
	public Position getUpperLeftCorner()
	{
		return (a == 0) ? null : new Position(x, y);
	}

	/**
	 * Gets a position representing the upper- and right-most Position
	 * contained by this Region.
	 * 
	 * If this Region is empty, this method returns null.
	 */
	public Position getUpperRightCorner()
	{
		return (a == 0) ? null : new Position(x + w - 1, y);
	}

	/**
	 * Gets a position representing the lower- and left-most Position
	 * contained by this Region.
	 * 
	 * If this Region is empty, this method returns null.
	 */
	public Position getLowerLeftCorner()
	{
		return (a == 0) ? null : new Position(x, y + h - 1);
	}

	/**
	 * Gets a position representing the lower- and right-most Position
	 * contained by this Region.
	 * 
	 * If this Region is empty, this method returns null.
	 */
	public Position getLowerRightCorner()
	{
		return (a == 0) ? null : new Position(x + w - 1, y + h - 1);
	}

	/**
	 * Returns a new Region whose upper-left corner
	 * is shifted by &lt;dx, dy&gt;.
	 */
	public Region shift(int dx, int dy)
	{
		return new Region(x + dx, y + dy, w, h);
	}
	
	/**
	 * Streches Region in all four directions by d
	 */
	public Region stretch(int d)
	{
		return new Region(x - d, y - d, w + 2 * d, h + 2 * d);
	}
	
	/**
	 * Shrinks Region in all four directions by d
	 */
	public Region shrink(int d)
	{
		return new Region(
			Math.min(x + d, x + w / 2),
			Math.min(y + d, y + h / 2),
			Math.max(w - 2 * d, 0),
			Math.max(h - 2 * d, 0)
		);
	}
	
	/**
	 * Returns a new Region whose upper-left corner is the given Position.
	 */
	public Region move(Position pos)
	{
		return new Region(pos.x, pos.y, w, h);
	}
	
	/**
	 * Returns true if this Region contains the given Position.
	 */
	public boolean contains(Position pos)
	{
		return (pos.x >= this.x) && (pos.x < this.x + this.w)
		    && (pos.y >= this.y) && (pos.y < this.y + this.h);
	}

	/**
	 * Returns true if this Region contains all positions in the given Region.
	 */
	public boolean contains(Region reg)
	{
		return (reg.x >= this.x)
			&& (reg.y >= this.y)
			&& (reg.x + reg.w <= this.x + this.w)
			&& (reg.y + reg.h <= this.y + this.h);
	}
	
	/**
	 * Returns true if this Region does not contain any Positions.
	 */
    public boolean isEmpty()
    {
    	return (a == 0);
    }

	/**
	 * Returns true if this Region contains any positions that are also
	 * contained by the given Region.
	 */
    public boolean intersects(Region that)
    {
    	return getIntersection(that).a != 0;
    }
	
    /**
     * Gets a Region that contains all points contained by both
     * this Region and the given Region.
     * 
     * If this Region or the given Region are empty,
     * their intersection will be empty.
     */
	public Region getIntersection(Region that)
	{
		final int x1 = Math.max(this.x, that.x);
		final int y1 = Math.max(this.y, that.y);

		if ((this.a == 0) || (that.a == 0))
			return new Region(x1, y1, 0, 0);
		
		final int x2 = Math.min(this.x + this.w, that.x + that.w);
		final int y2 = Math.min(this.y + this.h, that.y + that.h);
		
		return new Region(x1, y1, x2 - x1, y2 - y1);
	}
	
	/**
	 * Gets the smallest Region that contains all points contained by either
	 * this Region or that Region.
	 * 
	 * If this Region or the given Region are empty, their union will be the
	 * unempty Region.
	 */
	public Region getUnion(Region that)
	{
		if (this.a == 0) return that;
		if (that.a == 0) return this;
		
		final int x1 = Math.min(this.x, that.x);
		final int y1 = Math.min(this.y, that.y);
		final int x2 = Math.max(this.x + this.w, that.x + that.w);
		final int y2 = Math.max(this.y + this.h, that.y + that.h);
		
		return new Region(x1, y1, x2 - x1, y2 - y1);
	}

	/**
	 * Returns true if the given object is a Region
	 * and has the same x, y, w and h values as this Region.
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (! (obj instanceof Region) || obj == null) return false;
		
		Region that = (Region) obj;
		
		return (this.x == that.x) && (this.y == that.y)
			&& (this.w == that.w) && (this.h == that.h);
	}

	/**
	 * Returns the hash code for this Region based on x, y, w and h members.
	 */
	public int hashCode()
	{
		return (x * 3571) ^ (y * 181081) ^ (w * 888) ^ (h * 28657);
	}

	/**
	 * Returns a string representation of this Region in the form
	 * "(x, y) w by h".
	 */
	public String toString()
	{
		return String.format("(%1$d, %2$d) %3$d by %4$d", x, y, w, h);
	}
	
	/**
	 * Returns an Iterator that lists off all Positions
	 * contained by this Region.
	 */
	public RIterator<Position> iterator()
	{
		return new PositionIterator();
	}

	/**
	 * Returns an Iterator that lists off all Positions
	 * contained by this Region offset by {@code &lt;dx, dy&gt;}.
	 */
	public RIterator<Position> iterator(int dx, int dy)
	{
		return new PositionIterator(dx, dy);
	}

	/**
	 * Returns an Iterator that lists off all Positions
	 * contained by this Region offset by {@code origin}.
	 */
	public RIterator<Position> iterator(Position origin)
	{
		return new PositionIterator(origin.x, origin.y);
	}
	
	/**
	 * Iterator wrapper that shifts all Position objects returned by
	 * underlying Iterator.
	 */
	private class PositionIterator extends RIterator<Position>
	{
		private int dx, dy;
		private int index = 0;
		
		public PositionIterator()
		{
			this.dx = 0;
			this.dy = 0;
		}
		
		public PositionIterator(int dx, int dy)
		{
			this.dx = dx;
			this.dy = dy;
		}
		
		public boolean hasNext()
		{
			return index < a;
		}
		
		public Position next()
		{
			checkHasNext();
			
			int xPrime = index % w;
			int yPrime = index / w;
			index++;
			
			return new Position(x + xPrime + dx, y + yPrime + dy);
		}
	}
}
