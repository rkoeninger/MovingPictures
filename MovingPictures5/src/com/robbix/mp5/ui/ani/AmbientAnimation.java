package com.robbix.mp5.ui.ani;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;
import com.robbix.utils.RGraphics;

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
    public abstract void paint(RGraphics g);
    
    public abstract void step();
    
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
    	Game.game.doLater(new Runnable()
    	{
    		public void run()
    		{
    			Game.game.playSound(sound, pos);
    		}
    	});
    }
    
    protected void doSplashDamageLater(final Position pos, final double amount, final double range)
    {
    	Game.game.doLater(new Runnable()
    	{
    		public void run()
    		{
    			Game.game.doSplashDamage(pos, amount, range);
    		}
    	});
    }
    
    protected void doDamageLater(final Unit attacker, final Unit target, final double amount)
    {
    	Game.game.doLater(new Runnable()
    	{
    		public void run()
    		{
    			Game.game.doDamage(attacker, target, amount);
    		}
    	});
    }
}
