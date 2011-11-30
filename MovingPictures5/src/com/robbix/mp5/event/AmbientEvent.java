package com.robbix.mp5.event;

import java.awt.geom.Point2D;

public class AmbientEvent implements TransientEvent, LocalEvent
{
	private EventType type;
	private String arg;
	private int time;
	private Point2D point;
	
	public AmbientEvent(EventType type, String arg, int time, Point2D point)
	{
		this.type = type;
		this.arg = arg;
		this.time = time;
		this.point = point;
	}
	
	public EventType getType()
	{
		return type;
	}
	
	public String getArg()
	{
		return arg;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public Point2D getPosition()
	{
		return point;
	}
	
	public void step()
	{
		
	}
}
