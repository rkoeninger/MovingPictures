package com.robbix.mp5.mapper;

import java.awt.Point;

public class ResourceMarker
{
	public static enum Type{COMMON, RARE};
	
	public Point pos;
	public Type type;
	
	public ResourceMarker(Type t, Point p)
	{
		type = t;
		pos = p;
	}
	
	public int hashCode()
	{
		return pos.hashCode();
	}
}
