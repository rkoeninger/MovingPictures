package com.robbix.mp5.basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A two-dimensional grid containing elements of a generic type. All positions
 * in the grid can be assigned a value of null.
 * 
 * Elements in the grid are iterated left-to-right, then top-to-bottom. So the
 * iterator returned by iterator() will first return the element at (0, 0),
 * then (1, 0), (2, 0), (3, 0)... later followed by (0, 1), (1, 1), (2, 1)...
 * and lastly (width - 1, height - 1).
 * 
 * Find methods search for elements in the same order described above.
 * 
 * Grids of area 0 are invalid and cannot be instantiated.
 * 
 * IndexOutOfBoundsExceptions can be thrown by any method that takes a Position
 * as an argument.
 * 
 * @author bort
 */
public class Grid<T> implements Iterable<T>
{
	/**
	 * One-dimensional array that contains the elements of this Grid.
	 * 
	 * To get an index in the array from coordinates: i = x + (y * w)
	 * 
	 * To get coordinates from an index: x = i % w
	 *                                   y = i / w 
	 */
	private T[] cells;
	
	/**
	 * The width of this Grid.
	 */
	public final int w;
	
	/**
	 * The height of this Grid.
	 */
	public final int h;
	
	/**
	 * The bounding Region that encompasses all positions in this Grid.
	 */
	private final Region bounds;
	
	// Extra functions provided for consitency with other naming conventions.
	public int getWidth()    { return w;      }
	public int getHeight()   { return h;      }
	public Region getBounds(){ return bounds; }
	
	/**
	 * Creates a Grid of dimensions w by h with all values initially null.
	 * 
	 * @throws IllegalArgumentException if either w or h are less than 1.
	 */
	@SuppressWarnings("unchecked")
	public Grid(int w, int h)
	{
		if (w < 1 || h < 1)
			throw new IllegalArgumentException(ZERO_DIM);
		
		this.w = w;
		this.h = h;
		this.bounds = new Region(0, 0, w, h);
		cells = (T[]) new Object[bounds.a];
	}
	
	/**
	 * Creates a Grid of dimensions w by h with all values set to initValue.
	 * 
	 * @throws IllegalArgumentException If either w or h are less than 1.
	 */
	@SuppressWarnings("unchecked")
	public Grid(int w, int h, T initValue)
	{
		if (w < 1 || h < 1)
			throw new IllegalArgumentException(ZERO_DIM);
		
		this.w = w;
		this.h = h;
		this.bounds = new Region(0, 0, w, h);
		cells = (T[]) new Object[bounds.a];
		
		Arrays.fill(cells, initValue);
	}
	
	/**
	 * Creates a Grid with dimensions of region.
	 */
	public Grid(Region region)
	{
		this(region.w, region.h);
	}
	
	/**
	 * Creates a Grid with dimensions of region
	 * with all values set to initValue.
	 */
	public Grid(Region region, T initValue)
	{
		this(region.w, region.h, initValue);
	}
	
	/**
	 * Creates a new Grid of the same dimensions and containing the same
	 * elements in the same Positions as the given Grid.
	 * 
	 * The new Grid is independent of the given one. Modifying the new Grid
	 * will not affect this Grid and vice-versa. Do note, however that this
	 * method does not perform a deep copy of the elements in this Grid.
	 */
	public Grid(Grid<T> that)
	{
		this.w = that.w;
		this.h = that.h;
		this.bounds = that.bounds;
		this.cells = Arrays.copyOf(that.cells, that.cells.length);
	}
	
	/**
	 * Gets the element at the given coordinates.
	 * 
	 * @throws IndexOutOfBoundsException If either coordinate is out of bounds.
	 */
	public T get(int x, int y)
	{
		if (x < 0 || y < 0 || x >= w || y >= h)
			throw new IndexOutOfBoundsException("(" + x + ", " + y + ")");
		
		return cells[x + y * w];
	}
	
	/**
	 * Gets the element at the given Position.
	 * 
	 * @throws IndexOutOfBoundsException If either coordinate is out of bounds.
	 */
	public T get(Position pos)
	{
		if (pos.x < 0 || pos.y < 0 || pos.x >= w || pos.y >= h)
			throw new IndexOutOfBoundsException(pos.toString());
		
		return cells[pos.x + pos.y * w];
	}
	
	/**
	 * Sets the element at the given coordinates.
	 * 
	 * @throws IndexOutOfBoundsException If either coordinate is out of bounds.
	 */
	public void set(int x, int y, T value)
	{
		if (x < 0 || y < 0 || x >= w || y >= h)
			throw new IndexOutOfBoundsException("(" + x + ", " + y + ")");
		
		cells[x + y * w] = value;
	}

	/**
	 * Sets the element at the given Position.
	 * 
	 * @throws IndexOutOfBoundsException If either coordinate is out of bounds.
	 */
	public void set(Position pos, T value)
	{
		if (pos.x < 0 || pos.y < 0 || pos.x >= w || pos.y >= h)
			throw new IndexOutOfBoundsException(pos.toString());
		
		cells[pos.x + pos.y * w] = value;
	}

	/**
	 * Sets all elements within the given Region to value.
	 * 
	 * @throws IndexOutOfBoundsException If reg is not contained by this Grid.
	 */
	public void fill(Region reg, T value)
	{
		if (! bounds.contains(reg))
			throw new IndexOutOfBoundsException(reg.toString());
		
		for (Position pos : reg)
			cells[pos.x + pos.y * w] = value;
	}
	
	/**
	 * Sets all elements in this Grid to the given value.
	 */
	public void fill(T value)
	{
		Arrays.fill(cells, value);
	}
	
	/**
	 * Returns true if this Grid contains at least one instance of value.
	 */
	public boolean contains(T value)
	{
		return find(value) != null;
	}
	
	/**
	 * Returns the Position of the first occurance of the given value or null
	 * if this Grid does not contain the given value.
	 */
	public Position find(T value)
	{
		if (value != null)
		{
			for (int i = 0; i < bounds.a; ++i)
				if (cells[i] != null && cells[i].equals(value))
					return new Position(i % w, i / w);
		}
		
		return null;
	}

	/**
	 * Returns the Position of the last occurance of the given value or null
	 * if this Grid does not contain the given value.
	 */
	public Position findLast(T value)
	{
		if (value != null)
		{
			for (int i = bounds.a - 1; i >= 0; --i)
				if (cells[i] != null && cells[i].equals(value))
					return new Position(i % w, i / w);
		}
		
		return null;
	}
	
	/**
	 * Returns the Positions of all occurances of the given value. Returned
	 * list will be empty if this Grid does not contain the given value.
	 */
	public List<Position> findAll(T value)
	{
		List<Position> indicies = new ArrayList<Position>();
		
		if (value != null)
		{
			for (int i = 0; i < bounds.a; ++i)
				if (cells[i] != null && cells[i].equals(value))
					indicies.add(new Position(i % w, i / w));
		}
		
		return indicies;
	}
	
	/**
	 * Returns the Positions of all occurances of the given value in the
	 * specified Region. Returned list will be empty if this Grid does not
	 * contain the given value within the specified Region.
	 */
	public List<Position> findAll(T value, Region region)
	{
		if (!bounds.contains(region))
			throw new IndexOutOfBoundsException(region.toString());
		
		List<Position> indicies = new ArrayList<Position>();
		
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			T current = cells[y * w + x];
			
			if (current != null && current.equals(value))
				indicies.add(new Position(x, y));
		}
		
		return indicies;
	}
	
	/**
	 * Returns the Position of an element whose distance from the given
	 * Position is minimum.
	 * 
	 * Returns null if the given value is not in this Grid or if value is null.
	 */
	public Position findClosest(Position pos, T value)
	{
		if (value == null)
			return null;
		
		return findClosest(
			pos,
			Filter.getEqualityFilter(value),
			0,
			Double.POSITIVE_INFINITY
		);
	}

	/**
	 * Returns the Position of an element whose distance from the given
	 * Position is minimum and within the specified distance range.
	 * 
	 * The interval [minDistance, maxDistance] is mutally inclusive.
	 * 
	 * Returns null if the given value is not in this Grid or if value is null.
	 */
	public Position findClosest(
		Position pos,
		T value,
		double minDistance,
		double maxDistance)
	{
		if (value == null)
			return null;
		
		return findClosest(
				pos,
				Filter.getEqualityFilter(value),
				minDistance,
				maxDistance
			);
	}

	/**
	 * Returns the Position of the first occurance of an element that is
	 * acceptable by the given Filter, or null if this Grid does not contain
	 * any such element.
	 */
	public Position find(Filter<T> cond)
	{
		if (cond == null)
			throw new IllegalArgumentException("Filter can't be null");
		
		for (int i = 0; i < bounds.a; ++i)
			if (cells[i] != null && cond.accept(cells[i]))
				return new Position(i % w, i / w);
		
		return null;
	}

	/**
	 * Returns the Position of the last occurance of an element that is
	 * acceptable by the given Filter, or null if this Grid does not contain
	 * any such element.
	 */
	public Position findLast(Filter<T> cond)
	{
		if (cond == null)
			throw new IllegalArgumentException("Filter can't be null");
		
		for (int i = bounds.a - 1; i >= 0; --i)
			if (cells[i] != null && cond.accept(cells[i]))
				return new Position(i % w, i / w);
		
		return null;
	}
	
	/**
	 * Returns the Positions of all occurances of elements that are acceptable
	 * by the given Filter. Returned list will be empty if this Grid does not
	 * contain any such elements.
	 */
	public List<Position> findAll(Filter<T> cond)
	{
		if (cond == null)
			throw new IllegalArgumentException("Filter can't be null");
		
		List<Position> indicies = new ArrayList<Position>();
		
		for (int i = 0; i < bounds.a; ++i)
			if (cells[i] != null && cond.accept(cells[i]))
				indicies.add(new Position(i % w, i / w));
		
		return indicies;
	}
	
	/**
	 * Returns the Positions of all occurances of elements that are acceptable
	 * by the given Filter and within the given Region. Returned list will
	 * be empty if the Region does not contain any such elements.
	 */
	public List<Position> findAll(Filter<T> cond, Region region)
	{
		if (!bounds.contains(region))
			throw new IndexOutOfBoundsException(region.toString());
		
		List<Position> indicies = new ArrayList<Position>();
		
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			T current = cells[y * w + x];
			
			if (current != null && cond.accept(current))
				indicies.add(new Position(x, y));
		}
		
		return indicies;
	}

	/**
	 * Returns the Position of an element with a value that is acceptable by
	 * the given Filter, and whose distance from the given Position is lower
	 * than the distance for any other such element.
	 * 
	 * Returns null if no values in this Grid are acceptable by cond.
	 */
	public Position findClosest(Position pos, Filter<T> cond)
	{
		return findClosest(pos, cond, 0, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Returns the Position of an element with a value that is acceptable by
	 * the given Filter, and whose distance from the given Position is lower
	 * than the distance for any other such element and is within the given
	 * distance range.
	 * 
	 * The interval [minDistance, maxDistance] is mutally inclusive.
	 * 
	 * Returns null if no values in this Grid are acceptable by cond.
	 */
	public Position findClosest(
		Position pos,
		Filter<T> cond,
		double minDistance,
		double maxDistance)
	{
		if (cond == null)
			throw new IllegalArgumentException("Filter may not be null");
		
		int iteration = (int) (minDistance * 1.414);
		int maxIteration =
			Double.isInfinite(maxDistance) || maxDistance > Integer.MAX_VALUE
			? -1
			: (int) maxDistance;
		
		if (iteration == 0)
		{
			if (cond.accept(get(pos)))
				return pos;
			
			iteration = 1;
		}
		
		double closestDistance = Double.POSITIVE_INFINITY;
		Position closestPos = null;
		
		int startX, stopX, startY, stopY;
		boolean nEdge, sEdge, eEdge, wEdge;

		// TODO: comment algorithm
		do
		{
			wEdge = pos.x - iteration >= 0;
			startX = Math.max(pos.x - iteration, 0);

			nEdge = pos.y - iteration >= 0;
			startY = Math.max(pos.y - iteration, 0);
			
			eEdge = pos.x + iteration < w;
			stopX = Math.min(pos.x + iteration, w - 1);

			sEdge = pos.y + iteration < h;
			stopY = Math.min(pos.y + iteration, h - 1);
			
			if (nEdge || sEdge)
				for (int x = startX; x <= stopX; ++x)
				{
					if (nEdge && cond.accept(get(x, startY)))
					{
						double distSq = distanceSq(pos.x, pos.y, x, startY);
						if (distSq < closestDistance)
						{
							closestDistance = distSq;
							closestPos = new Position(x, startY);
						}
					}
					
					if (sEdge && cond.accept(get(x, stopY)))
					{
						double distSq = distanceSq(pos.x, pos.y, x, stopY);
						if (distSq < closestDistance)
						{
							closestDistance = distSq;
							closestPos = new Position(x, stopY);
						}
					}
				}
			
			if (nEdge) startY += 1;
			if (sEdge) stopY -= 1;
			
			if (wEdge || eEdge)
				for (int y = startY; y <= stopY; ++y) 
				{
					if (wEdge && cond.accept(get(startX, y)))
					{
						double distSq = distanceSq(pos.x, pos.y, startX, y);
						if (distSq < closestDistance)
						{
							closestDistance = distSq;
							closestPos = new Position(startX, y);
						}
					}
					
					if (eEdge && cond.accept(get(stopX, y)))
					{
						double distSq = distanceSq(pos.x, pos.y, stopX, y);
						if (distSq < closestDistance)
						{
							closestDistance = distSq;
							closestPos = new Position(stopX, y);
						}
					}
				}

			if (closestPos != null && maxIteration == -1)
			{
				double dy = Math.abs(pos.y - closestPos.y);
				double dx = Math.abs(pos.x - closestPos.x);
				maxIteration = (int) Math.sqrt(dx*dx + dy*dy);
			}
			
			boolean allOutOfBounds = !(nEdge || sEdge || wEdge || eEdge);
			boolean finalIteration = maxIteration != -1
								  && iteration >= maxIteration;
			
			if (finalIteration || allOutOfBounds)
			{
				if (closestPos == null)
					return null;
				
				double distance = closestPos.getDistance(pos);
				return distance <= maxDistance && distance >= minDistance
					? closestPos
					: null;
			}

			iteration++;
		}
		while (nEdge || sEdge || wEdge || eEdge);

		if (closestPos == null)
			return null;
		
		double distance = closestPos.getDistance(pos);
		return distance <= maxDistance && distance >= minDistance
			? closestPos
			: null;
	}
	
	/**
	 * Returns a new Grid of the same dimensions and containing the same
	 * elements in the same Positions as this Grid.
	 * 
	 * The new Grid is independent of this one. Modifying the new Grid will
	 * not affect this Grid and vice-versa. Do note, however that this method
	 * does not perform a deep copy of the elements in this Grid.
	 */
	public Grid<T> copy()
	{
		return new Grid<T>(this);
	}
	
	/**
	 * Returns true if the given object is a Grid
	 * and has the same dimensions and element values as this Grid.
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (! (obj instanceof Grid<?>) || obj == null) return false;
		
		Grid<?> that = (Grid<?>) obj;
		
		return this.w == that.w
			&& this.h == that.h
			&& Arrays.deepEquals(this.cells, that.cells);
	}
	
	/**
	 * Returns the hashCode for this Grid. Hash code is delegated to the
	 * underlying array.
	 */
	public int hashCode()
	{
		return Arrays.hashCode(cells) ^ (w * 888) ^ (h * 28657);
	}

	/**
	 * Returns an iterator that iterates over all elements in this Grid.
	 * 
	 * Its next() method will return a null reference for elements that
	 * are null and its hasNext() method will return true even if all
	 * remaining elements are null.
	 */
	public IterableIterator<T> iterator()
	{
		return new ElementIterator();
	}

	/**
	 * Returns an iterator that iterates over all elements in the given Region.
	 * 
	 * The (x, y, width, height) attributes of the given region are all
	 * relative to the bounding region of this Grid, as given by getRegion().
	 * 
	 * Its next() method will return a null reference for elements that
	 * are null and its hasNext() method will return true even if all
	 * remaining elements are null.
	 * 
	 * @throws IndexOutOfBoundsException If reg is not contained by this Grid's
	 *                                   bounding Region.
	 */
	public IterableIterator<T> iterator(Region reg)
	{
		if (! this.bounds.contains(reg))
			throw new IndexOutOfBoundsException(reg.toString());
		
		return new ElementIterator(reg);
	}
	
	/**
	 * Returns a string representation of this grid.
	 * 
	 * Returned string could be wider than 80 columns, so it might not fit
	 * well in a typical console window.
	 */
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(w);
		buffer.append(" wide by ");
		buffer.append(h);
		buffer.append(" high [\n");
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				buffer.append(cells[x + y * w]);
				buffer.append(", ");
			}
			
			buffer.append('\n');
		}
				
		buffer.append(']');
		
		return buffer.toString();
	}

	/**
	 * Interal class that iterates over the elements contained in a
	 * sub-Region of this Grid.
	 */
	private class ElementIterator extends IterableIterator<T>
	{
		private int index = 0;
		private Region reg;
		
		public ElementIterator()
		{
			this.reg = bounds;
		}
		
		public ElementIterator(Region reg)
		{
			this.reg = reg;
		}
		
		public boolean hasNext()
		{
			return index < reg.a;
		}
		
		public T next()
		{
			if (index >= reg.a)
				throw new NoSuchElementException();
			
			final int xPrime = (index % reg.w) + reg.x;
			final int yPrime = (index / reg.w) + reg.y;
			index++;
			
			return Grid.this.cells[xPrime + yPrime * Grid.this.w];
		}
	}
	
	private static final String ZERO_DIM =
		"Grid dimensions must both be greater than 0";
	
	/**
	 * Returns the square distance between two points. Used for comparisons
	 * of distances since if {@code sqrt(a) > sqrt(b) then a > b}.
	 */
	private static double distanceSq(int x1, int y1, int x2, int y2)
	{
		return ((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
	}
}
