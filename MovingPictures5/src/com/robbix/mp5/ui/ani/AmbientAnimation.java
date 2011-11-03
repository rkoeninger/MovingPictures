package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SpriteLibrary;

public abstract class AmbientAnimation
{
	protected DisplayPanel panel;
	protected SpriteLibrary lib;
	
	public AmbientAnimation(SpriteLibrary lib)
	{
		this.lib = lib;
	}
	
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
    
    protected Point2D add(Point2D p1, Point2D p2)
    {
    	return new Point2D.Double(p1.getX() + p2.getX(), p1.getY() + p2.getY());
    }
    
    protected void playSoundLater(final String sound, final Position pos)
    {
    	SwingUtilities.invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			Mediator.playSound(sound, pos);
    		}
    	});
    }
    
    protected void doSplashDamageLater(final Position pos, final double amount, final double range)
    {
    	SwingUtilities.invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			Mediator.doSplashDamage(pos, amount, range);
    		}
    	});
    }
}
