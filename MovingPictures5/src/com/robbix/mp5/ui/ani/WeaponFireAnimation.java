package com.robbix.mp5.ui.ani;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Utils;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public abstract class WeaponFireAnimation extends AmbientAnimation
{
	private Unit attacker;
	private Unit target;
	private Point2D start;
	private Point2D end;
	private Rectangle2D bounds;
	
	public WeaponFireAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib);
		
		this.attacker = attacker;
		this.target = target;
		
		start = add(attacker.getAbsPoint(), lib.getHotspot(attacker));
		
		double w = target.getWidth();
		double h = target.getHeight();
		Point2D impactOffset = new Point2D.Double(
			w / 2 + Utils.randFloat(-w / 4, w / 4),
			h / 2 + Utils.randFloat(-h / 4, h / 4)
		);
		end = add(target.getAbsPoint(), impactOffset);
		
		bounds = new Rectangle2D.Double(
			min(start.getX(), end.getX()),
			min(start.getY(), end.getY()),
			abs(start.getX() - end.getX()),
			abs(start.getY() - end.getY())
		);
	}
	
	public Unit getAttacker()
	{
		return attacker;
	}
	
	public Unit getTarget()
	{
		return target;
	}
	
	public Point2D getFireOrigin()
	{
		return start;
	}
	
	public Point2D getFireImpact()
	{
		return end;
	}
	
	public Rectangle2D getBounds()
	{
		return bounds;
	}
	
	/**
	 * Returns true if the animation is at the frame where damage would be done.
	 */
	public abstract boolean atHotPoint();
}
