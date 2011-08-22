package com.robbix.mp5.basics;

import java.util.SortedSet;
import java.util.TreeSet;

import com.robbix.mp5.Utils;

/**
 * The origin of relative Positions in the Footprint is
 * the upper-left corner of the inner Region.
 */
//TODO: cache Footprint objects - have a static getFootprint(x, y) method
public class Footprint implements Iterable<Position>
{
	/**
	 * The Footprint used by vehicles as the engine is currently designed.
	 * 
	 * Both inner and outer Regions are 1x1 at the origin.
	 */
	public static final Footprint VEHICLE =
		new Footprint(new Region(1, 1));
	
	public static final Footprint STRUCT_1_BY_1 =
		new Footprint(
			new Region(0, 0, 1, 1)
		);
	
	public static final Footprint STRUCT_2_BY_1_NO_W_SIDE =
		new Footprint(
			new Region(0, 0, 2, 1)
		).remove(new Position(0, 0));
	
	public static final Footprint STRUCT_1_BY_2_NO_S_SIDE =
		new Footprint(
			new Region(0, 0, 1, 2)
		).remove(new Position(0, 1));
	
	public static final Footprint STRUCT_2_BY_2 =
		new Footprint(
			new Region(0, 0, 2, 2)
		);

	public static final Footprint STRUCT_3_BY_2 =
		new Footprint(
			new Region(0, 0, 3, 2)
		);

	public static final Footprint STRUCT_3_BY_3 =
		new Footprint(
			new Region(0, 0, 3, 3)
		);

	public static final Footprint STRUCT_4_BY_3 =
		new Footprint(
			new Region(0, 0, 4, 3)
		);

	public static final Footprint STRUCT_4_BY_3_NO_SW_CORNER =
		new Footprint(
			new Region(0, 0, 4, 3)
		).remove(new Position(0, 2));

	public static final Footprint STRUCT_4_BY_3_NO_SE_CORNER =
		new Footprint(
			new Region(0, 0, 4, 3)
		).remove(new Position(3, 2));
	
	/**
	 * The inner Region of the Footprint. The unit totally occupies
	 * Positions in the inner Region - other units cannot travel there.
	 */
	private Region inner;
	
	/**
	 * The set of Positions this Footprint exclusively occupies.
	 */
	private SortedSet<Position> occupiedSet;
	
	/**
	 * The center of the Footprint, relative to the upper-left corner of
	 * the inner Region.
	 * 
	 * When the user places a Footprint on the map, this is the Position
	 * that will be centered around the mouse cursor.
	 */
	private Position center;
	
	/**
	 * Creates a new Footprint with given inner and outer Regions.
	 * 
	 * @throws IllegalArgumentException If outer does not totally contain inner
	 */
	public Footprint(Region inner)
	{
		this.inner = inner;
		this.center = new Position(inner.w / 2, inner.h / 2);
		
		this.occupiedSet = new TreeSet<Position>(Utils.Z_ORDER_POS);
		
		for (Position occupied : inner)
			occupiedSet.add(occupied);
	}
	
	/**
	 * Returns the inner Region of this Footprint.
	 */
	public Region getInnerRegion()
	{
		return inner;
	}
	
	/**
	 * Gets the Position relative to the origin of this Footprint considered
	 * the center.
	 */
	public Position getCenter()
	{
		return center;
	}
	
	public int getWidth()
	{
		return inner.w;
	}
	
	public int getHeight()
	{
		return inner.h;
	}
	
	/**
	 * Returns a new Footprint with the given collection of Positions
	 * considered unoccupied.
	 */
	public Footprint remove(Position... unoccupiedSet)
	{
		Footprint result = new Footprint(inner);
		
		for (Position unoccupied : unoccupiedSet)
			result.occupiedSet.remove(unoccupied);
		
		return result;
	}
	
	/**
	 * Returns true if this Footprint and the given Footprint overlap
	 * in any way that is unacceptable.
	 * 
	 * Typically, two Footprints conflict if one's inner Region intersects
	 * the other's outer Region.
	 */
	public boolean conflicts(Footprint that)
	{
		return this.inner.intersects(that.inner);
	}
	
	/**
	 * Compare two Footprints for equality based on inner and outer Regions.
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (! (obj instanceof Footprint) || obj == null) return false;
		
		Footprint that = (Footprint) obj;
		
		return this.inner.equals(that.inner);	
	}
	
	/**
	 * Gets hash code for this Footprint based on inner and outer Regions.
	 */
	public int hashCode()
	{
		return inner.hashCode() * 101;
	}
	
	/**
	 * Iterates all Positions occupied by this Footprint that cannot
	 * be occupied by any other Unit.
	 * 
	 * The Positions returned by this Iterator should be the same Positions
	 * in the UnitLayer that the Unit with this Footprint occupies.
	 */
	public IterableIterator<Position> iterator()
	{
		return IterableIterator.iterate(occupiedSet);
	}
	
	/**
	 * Shifts and iterates all Positions occupied by this Footprint
	 * that cannot be occupied by any other Unit.
	 * 
	 * The Positions returned by this Iterator should be the same Positions
	 * in the UnitLayer that the Unit with this Footprint occupies.
	 */
	public IterableIterator<Position> iterator(int dx, int dy)
	{
		return new ShiftingIterator(occupiedSet.iterator(), dx, dy);
	}
	
	/**
	 * Shifts and iterates all Positions occupied by this Footprint
	 * that cannot be occupied by any other Unit.
	 * 
	 * The Positions returned by this Iterator should be the same Positions
	 * in the UnitLayer that the Unit with this Footprint occupies.
	 */
	public IterableIterator<Position> iterator(Position origin)
	{
		return new ShiftingIterator(
			occupiedSet.iterator(),
			origin.x,
			origin.y
		);
	}
}
