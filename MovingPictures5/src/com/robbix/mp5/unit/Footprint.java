package com.robbix.mp5.unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.utils.Position;
import com.robbix.utils.RIterator;
import com.robbix.utils.Region;
import com.robbix.utils.ShiftingIterator;

/**
 * The origin of relative Positions in the Footprint is
 * the upper-left corner of the inner Region.
 */
public class Footprint implements Iterable<Position>
{
	/**
	 * The Footprint used by vehicles as the engine is currently designed.
	 * 
	 * Both inner and outer Regions are 1x1 at the origin.
	 */
	public static final Footprint VEHICLE =
		new Footprint(1, 1);
	
	public static final Footprint STRUCT_1_BY_1 =
		new Footprint(1, 1).tube(1, 0).tube(0, 1);
	
	public static final Footprint STRUCT_1_BY_1_NO_TUBES =
		new Footprint(1, 1);
	
	public static final Footprint STRUCT_2_BY_1_NO_W_SIDE =
		new Footprint(2, 1).remove(0, 0);
	
	public static final Footprint STRUCT_1_BY_2_NO_S_SIDE =
		new Footprint(1, 2).remove(0, 1).tube(0, 2).tube(1, 1);
	
	public static final Footprint STRUCT_2_BY_2 =
		new Footprint(2, 2).tube(2, 1).tube(1, 2);
	
	public static final Footprint STRUCT_3_BY_2 =
		new Footprint(3, 2).tube(3, 1).tube(1, 2);
	
	public static final Footprint STRUCT_3_BY_3 =
		new Footprint(3, 3).tube(3, 1).tube(1, 3);
	
	public static final Footprint STRUCT_4_BY_3 =
		new Footprint(4, 3).tube(4, 1).tube(2, 3);
	
	public static final Footprint STRUCT_4_BY_3_NO_SW_CORNER =
		new Footprint(4, 3).remove(0, 2).tube(4, 1).tube(2, 3);
	
	public static final Footprint STRUCT_4_BY_3_NO_SE_CORNER =
		new Footprint(4, 3).remove(3, 2).tube(4, 1).tube(2, 3);
	
	public static final Footprint STRUCT_5_BY_4_NO_SE_CORNER =
		new Footprint(5, 4).remove(4, 3).tube(5, 2).tube(2, 4);
	
	public static final Footprint STRUCT_5_BY_4_NO_SW_CORNER =
		new Footprint(5, 4).remove(0, 3).tube(5, 2).tube(2, 4);
	
	/**
	 * The inner Region of the Footprint. The unit totally occupies
	 * Positions in the inner Region - other units cannot travel there.
	 */
	private Region inner;
	
	/**
	 * The set of Positions this Footprint exclusively occupies.
	 */
	private Set<Position> occupiedSet;
	
	/**
	 * The set of Positions in this Footprint where tubes are placed.
	 */
	private Set<Position> tubePositions;
	
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
	private Footprint(Region inner)
	{
		this.inner = inner;
		this.center = inner.getCenter();
		this.tubePositions = new HashSet<Position>();
		this.occupiedSet = new HashSet<Position>();
		
		for (Position occupied : inner)
			occupiedSet.add(occupied);
	}
	
	private Footprint(int w, int h)
	{
		this(new Region(0, 0, w, h));
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
	
	private Footprint tube(int x, int y)
	{
		Position pos = new Position(x, y);
		
		if (occupiedSet.contains(pos))
			throw new IllegalStateException("pos occupied");
		
		tubePositions.add(pos);
		return this;
	}

	/**
	 * Returns a new Footprint with the given collection of Positions
	 * considered unoccupied.
	 */
	private Footprint remove(int x, int y)
	{
		return remove(new Position(x, y));
	}
	
	/**
	 * Returns a new Footprint with the given collection of Positions
	 * considered unoccupied.
	 */
	private Footprint remove(Position... unoccupiedSet)
	{
		Footprint result = new Footprint(inner);
		
		for (Position unoccupied : unoccupiedSet)
			result.occupiedSet.remove(unoccupied);
		
		return result;
	}
	
	/**
	 * Returns a set of positions occupied by tubes in this footprint.
	 */
	public Set<Position> getTubePositions()
	{
		return tubePositions;
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
	public RIterator<Position> iterator()
	{
		return RIterator.iterate(occupiedSet);
	}
	
	/**
	 * Shifts and iterates all Positions occupied by this Footprint
	 * that cannot be occupied by any other Unit.
	 * 
	 * The Positions returned by this Iterator should be the same Positions
	 * in the UnitLayer that the Unit with this Footprint occupies.
	 */
	public RIterator<Position> iterator(int dx, int dy)
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
	public RIterator<Position> iterator(Position origin)
	{
		return new ShiftingIterator(
			occupiedSet.iterator(),
			origin.x,
			origin.y
		);
	}
	
	public List<Position> getFactoryExits(Position origin)
	{
		if (inner.w == 4 && inner.h == 3)
		{
			List<Position> posList = new ArrayList<Position>();
			
			posList.add(origin.shift(4, 1));
			posList.add(origin.shift(4, 0));
			posList.add(origin.shift(4, -1));
			posList.add(origin.shift(3, -1));
			posList.add(origin.shift(2, -1));
			posList.add(origin.shift(1, -1));
			posList.add(origin.shift(0, -1));
			posList.add(origin.shift(-1, -1));
			posList.add(origin.shift(-1, 0));
			posList.add(origin.shift(-1, 1));
			posList.add(origin.shift(-1, 2));
			posList.add(origin.shift(-1, 3));
			posList.add(origin.shift(0, 3));
			posList.add(origin.shift(1, 3));
			posList.add(origin.shift(2, 3));
			posList.add(origin.shift(3, 3));
			posList.add(origin.shift(4, 3));
			posList.add(origin.shift(4, 2));
			
			return posList;
		}
		
		throw new UnsupportedOperationException("not a factory footprint");
	}
}
