package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.ui.DisplayPanel;

public abstract class AmbientAnimation
{
	protected DisplayPanel panel;
	
	public void setDisplay(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplay()
	{
		return panel;
	}
	
    /**
     * Bounding rectangle encloses the area the animation is drawn in
     */
    public abstract Rectangle2D getBounds();

    /**
     * Graphics object is translated to the bounding rectangle
     */
    public abstract void paint(Graphics g);
    
    public abstract void step(AtomicReference<Runnable> ref);
    
    /**
     * Returns true if this animation is done - it is no longer
     * rendering anything and no longer will
     */
    public abstract boolean isDone();
}
