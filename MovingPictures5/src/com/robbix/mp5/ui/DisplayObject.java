package com.robbix.mp5.ui;

import java.awt.geom.Rectangle2D;


public class DisplayObject
{
	protected DisplayPanel panel;
	
	public void setDisplayPanel(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return panel;
	}
	
	public boolean isAlive()
	{
		return false;
	}
	
	public void paint(DisplayGraphics g)
	{
		
	}
	
	/**
	 * Bounds should wrap as closely around the phenomena as possible
	 * without excluding any of it. This way, phenomena won't pop on and off
	 * the screen around the edges.
	 */
	public Rectangle2D getBounds()
	{
		return null;
	}
}
