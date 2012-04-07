package com.robbix.mp5.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Footprint
{
	public static Footprint newStructureFootprint(int w, int h)
	{
		return new Footprint(
			new Region(0, 0, w, h),
			new Region(-1, -1, w + 2, h + 2)
		);
	}
	
	public static final Footprint STRUCT_1_BY_1;
	public static final Footprint STRUCT_1_BY_2;
	public static final Footprint STRUCT_2_BY_2;
	public static final Footprint STRUCT_3_BY_2;
	public static final Footprint STRUCT_3_BY_3;
	public static final Footprint STRUCT_4_BY_3;
	public static final Footprint STRUCT_5_BY_4;
	
	static
	{
		serialTable = new HashMap<Integer, Footprint>();
		
		STRUCT_1_BY_1 = newStructureFootprint(1, 1).putTube(0, 1).putTube(1, 0).setSerial(2).register();
		STRUCT_1_BY_2 = newStructureFootprint(1, 2).putTube(0, 2).putTube(1, 1).setSerial(4).register();
		STRUCT_2_BY_2 = newStructureFootprint(2, 2).putTube(1, 2).putTube(2, 1).setSerial(6).register();
		STRUCT_3_BY_2 = newStructureFootprint(3, 2).putTube(1, 2).putTube(3, 1).setSerial(8).register();
		STRUCT_3_BY_3 = newStructureFootprint(3, 3).putTube(1, 3).putTube(3, 1).setSerial(10).register();
		STRUCT_4_BY_3 = newStructureFootprint(4, 3).putTube(2, 3).putTube(4, 1).setSerial(12).register();
		STRUCT_5_BY_4 = newStructureFootprint(5, 4).putTube(2, 4).putTube(5, 2).setSerial(14).register();
	}
	
	public static final Map<Integer, Footprint> serialTable;
	
	public final Region inner;
	public final Region outer;
	
	private int serial;
	
	private SortedSet<Position> occupiedSet;
	
	private Set<Position> tubePositions = new HashSet<Position>();
	
	public Footprint(Region reg)
	{
		this.inner = reg;
		this.outer = reg;
	}
	
	public Footprint(Region inner, Region outer)
	{
		this.inner = inner;
		this.outer = outer;
	}
	
	public Footprint remove(Position... unoccupiedSpots)
	{
		Footprint result = new Footprint(this.inner, this.outer);
		
		result.occupiedSet = new TreeSet<Position>(Utils.Z_ORDER_POS);
		
		for (Position pos : inner)
			result.occupiedSet.add(pos);
		
		for (Position unoccupied : unoccupiedSpots)
			result.occupiedSet.remove(unoccupied);
		
		return result;
	}
	
	public int getSerial()
	{
		return serial;
	}
	
	public Footprint register()
	{
		serialTable.put(serial, this);
		return this;
	}
	
	public Footprint setSerial(int serial)
	{
		this.serial = serial;
		return this;
	}
	
	public Footprint putTube(int x, int y)
	{
		return putTube(new Position(x, y));
	}
	
	public Footprint putTube(Position pos)
	{
		if (occupiedSet != null && occupiedSet.contains(pos))
			throw new IllegalStateException("can't put tube in occupied spot");
		
		tubePositions.add(pos);
		
		return this;
	}
	
	public boolean conflicts(Footprint that)
	{
		return getInnerRegion().intersects(that.getOuterRegion());
	}
	
	public boolean hasHoles()
	{
		return occupiedSet != null;
	}
	
	public Region getInnerRegion()
	{
		return inner;
	}
	
	public Region getOuterRegion()
	{
		return outer;
	}
	
	public Footprint shift(Position pos)
	{
		return new Footprint(inner.shift(pos), outer.shift(pos));
	}
	
	public Set<Position> getTubePositions()
	{
		return tubePositions;
	}
	
	/**
	 * Iterates positions in the UnitLayer occupied by this Footprint.
	 * 
	 * Positions over which part of the building is drawn, but are still
	 * passable by vehicles (i.e. the dock) are not to be returned by
	 * this Iterator.
	 */
	public Iterator<Position> iterator()
	{
		return occupiedSet == null
			? inner.iterator()
			: occupiedSet.iterator();
	}
	
	public Iterator<Position> iterator(int dx, int dy)
	{
		return new ShiftingIterator(
			occupiedSet == null
				? inner.iterator()
				: occupiedSet.iterator(),
			dx,
			dy
		);
	}
}
