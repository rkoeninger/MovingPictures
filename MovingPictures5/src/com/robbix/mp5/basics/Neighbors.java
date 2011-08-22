package com.robbix.mp5.basics;

/**
 * An enumeration of flags to identify which of a grid-position's neighbors
 * have some property. For walls on the map, a Neighbors value can refer to
 * which adjacent positions on the map also contain a wall.
 * 
 * Naming priority for values in this enum are North, South, East, West.
 * e.g. A set containing East, North and South will be NSE.
 */
public enum Neighbors
{
	/*
	 * Ordinals reflect a half-byte set of bit-flags:
	 * in binary, 0000abcd      'd' is the lowest-order bit, bit 0, d is bit 3
	 * a - east side
	 * b - north side
	 * c - west side
	 * d - south side
	 * 
	 * *** The order in which the values are declared is relevant! ***
	 */
	NONE, S, W, SW, N, NS, NW, NSW, E, SE, EW, SEW, NE, NSE, NEW, NSEW;

	/**
	 * Returns the set of neighbors held by either this or the given Neighbors.
	 * Also known as a <b>union</b>.
	 */
	public Neighbors add(Neighbors neighbors)
	{
		return Neighbors.values()[ordinal() | neighbors.ordinal()];
	}
	
	/**
	 * Returns the set of neighbors shared by this and the given Neighbors.
	 * Also known as an <b>intersection</b>.
	 */
	public Neighbors shared(Neighbors neighbors)
	{
		return Neighbors.values()[ordinal() & neighbors.ordinal()];
	}
	
	/**
	 * Returns a set of neighbors that contains all those held by this set,
	 * less those held by the given set.
	 */
	public Neighbors remove(Neighbors neighbors)
	{
		return Neighbors.values()[ordinal() & ~neighbors.ordinal()];
	}
	
	/**
	 * Determines if all the given Neighbors are held by this set.
	 */
	public boolean has(Neighbors neighbors)
	{
		return (ordinal() & neighbors.ordinal()) == neighbors.ordinal();
	}
	
	/**
	 * Determines if tis set of Neighbors contains a neighbor in the given
	 * Direction. Only works for North, South, East and West.
	 */
	public boolean has(Direction dir)
	{
		return (dir == Direction.N && has(Neighbors.N))
			|| (dir == Direction.S && has(Neighbors.S))
			|| (dir == Direction.E && has(Neighbors.E))
			|| (dir == Direction.W && has(Neighbors.W));
	}
}
