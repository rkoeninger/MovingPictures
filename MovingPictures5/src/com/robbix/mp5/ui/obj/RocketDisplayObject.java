package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.unit.Rocket;

public class RocketDisplayObject extends DisplayObject
{
	private Rocket rocket;
	
	public DisplayLayer getDisplayLayer()
	{
		return DisplayLayer.OVER_UNIT;
	}
	
	public boolean isAlive()
	{
		return rocket.isAlive();
	}
	
	public void paint(DisplayGraphics g)
	{
		
	}
	
	public Rectangle2D getBounds()
	{
		return null;
	}
}
