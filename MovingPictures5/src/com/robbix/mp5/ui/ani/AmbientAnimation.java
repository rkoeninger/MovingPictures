package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AmbientAnimation
{
    /**
     * Bounding rectangle encloses the area the animation is drawn in
     */
    public abstract Rectangle getBounds();

    /**
     * Graphics object is translated to the bounding rectangle
     */
    public abstract void paint(Graphics g);

    public abstract void step();
    
    public void step(AtomicReference<Runnable> ref)
    {
    	throw new UnsupportedOperationException("not impl, call step()");
    }
    
    public boolean hasCallback()
    {
    	return false;
    }
    
    /**
     * Returns true if this animation is done - it is no longer
     * rendering anything and no longer will
     */
    public abstract boolean isDone();
}
