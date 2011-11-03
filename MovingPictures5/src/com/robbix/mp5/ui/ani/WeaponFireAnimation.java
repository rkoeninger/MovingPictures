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
	private Point2D origin;
	private Point2D impact;
	private Rectangle2D bounds;
	
	public WeaponFireAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib);
		
		this.attacker = attacker;
		this.target = target;
		
		origin = add(attacker.getAbsPoint(), lib.getHotspot(attacker));
		
		double w = target.getWidth();
		double h = target.getHeight();
		Point2D impactOffset = new Point2D.Double(
			w / 2 + Utils.randFloat(-w / 4, w / 4),
			h / 2 + Utils.randFloat(-h / 4, h / 4)
		);
		impact = add(target.getAbsCenterPoint(), impactOffset);
		
		bounds = new Rectangle2D.Double(
			min(origin.getX(), impact.getX()),
			min(origin.getY(), impact.getY()),
			abs(origin.getX() - impact.getX()),
			abs(origin.getY() - impact.getY())
		);
		
		System.out.println(getClass() + "------------------------------");
		System.out.println(attacker.getAbsPoint());
		System.out.println(lib.getHotspot(attacker));
		System.out.println(target.getAbsPoint());
		System.out.println(impact);
		System.out.println(bounds);
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
		return origin;
	}
	
	public Point2D getFireImpact()
	{
		return impact;
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
