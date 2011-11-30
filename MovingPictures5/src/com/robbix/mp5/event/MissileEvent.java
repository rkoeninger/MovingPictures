package com.robbix.mp5.event;

import java.awt.geom.Point2D;

public class MissileEvent implements TransientEvent, LocalEvent
{
	public static class Smoke
	{
		public final int type;
		public final int startFrame;
		public final Point2D point;
		
		public Smoke(int type, int startFrame, Point2D point)
		{
			this.type = type;
			this.startFrame = startFrame;
			this.point = point;
		}
	}
	
	public EventType getType()
	{
		return null;
	}
	
	public String getArg()
	{
		return null;
	}
	
	public int getTime()
	{
		return 0;
	}
	
	public Point2D getPosition()
	{
		return null;
	}
	
	public void step()
	{
		
	}
}
