package com.robbix.mp5.basics;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

/**
 * Does not extend Region as it is not truly rectangular.
 * 
 * @author bort
 */
public class LShapedRegion implements RIterable<Position>
{
	private List<Position> elements;
	private Position begin;
	private Position elbow;
	private Position end;
	private Direction leg1Dir;
	private Direction leg2Dir;
	private int leg1Len;
	private int leg2Len;
	
	public final int length;

	public int getLength() { return length; }
	
	public Position getFirstEnd() { return begin; }
	public Position getElbow()    { return elbow; }
	public Position getSecondEnd(){ return end;   }

	public Direction getFirstLegDirection() { return leg1Dir; }
	public Direction getSecondLegDirection(){ return leg2Dir; }

	public int getFirstLegLength() { return leg1Len; }
	public int getSecondLegLength(){ return leg2Len; }
	
	public LShapedRegion(Position begin, Position elbow, Position end)
	{
		leg1Dir = Direction.getDirection(begin, elbow);
		leg2Dir = Direction.getDirection(end, elbow);
		
		if (leg1Dir == null)
			leg1Dir = Direction.E;
		
		if (leg2Dir == null)
			leg2Dir = Direction.E;
		
		leg1Len = max(abs(begin.x - elbow.x), abs(begin.y - elbow.y)) - 1;
		leg2Len = max(abs(elbow.x - end.x), abs(elbow.y - end.y)) - 1;
		
		elements = new ArrayList<Position>();
		
		for (Position current = begin; ! current.equals(elbow); current = leg1Dir.apply(current))
			elements.add(current);
		
		for (Position current = elbow; ! current.equals(end); current = leg2Dir.reverse().apply(current))
			elements.add(current);
		
		elements.add(end);
		
		this.begin = begin;
		this.elbow = elbow;
		this.end = end;
		
		length = leg1Len + leg2Len - 1;
	}
	
	public LShapedRegion(Position corner1, Position corner2)
	{
		this(corner1, new Position(corner1.x, corner2.y), corner2);
	}
	
	public boolean contains(Position pos)
	{
		return elements.contains(pos);
	}
	
	public boolean contains(Region region)
	{
		return elements.contains(region.getUpperLeftCorner())
			&& elements.contains(region.getUpperRightCorner())
			&& elements.contains(region.getLowerLeftCorner())
			&& elements.contains(region.getLowerRightCorner());
	}
	
	public RIterator<Position> iterator()
	{
		return RIterator.iterate(elements);
	}
}
