package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.DisplayPanel;

public abstract class DisplayObject
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
	
	public abstract boolean isAlive();
	
	public abstract void paint(DisplayGraphics g);
	
	/**
	 * Bounds should wrap as closely around the phenomena as possible
	 * without excluding any of it. This way, phenomena won't pop on and off
	 * the screen around the edges.
	 */
	public abstract Rectangle2D getBounds();
}
