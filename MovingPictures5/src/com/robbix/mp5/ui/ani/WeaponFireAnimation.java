package com.robbix.mp5.ui.ani;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public abstract class WeaponFireAnimation extends AmbientAnimation
{
	private Unit attacker;
	private Unit target;
	private Point2D attackerStart;
	private Point2D targetStart;
	private Point2D origin;
	private Point2D impact;
	private Rectangle2D bounds;
	
	public WeaponFireAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib);
		
		this.attacker = attacker;
		this.target = target;
		
		attackerStart = attacker.getAbsPoint();
		targetStart = target.getAbsPoint();
		
		Direction dir = Direction.getDirection(attacker.getPosition(), target.getPosition());
		origin = add(attacker.getAbsPoint(), lib.getHotspot(attacker.getType(), dir));
		
		double w = target.getWidth();
		double h = target.getHeight();
		Point2D impactOffset = new Point2D.Double(
			w / 2.0 + Utils.randFloat(-w / 8.0, w / 8.0),
			h / 2.0 + Utils.randFloat(-h / 8.0, h / 8.0)
		);
		impact = add(target.getAbsPoint(), impactOffset);
		
		bounds = new Rectangle2D.Double(
			min(origin.getX(), impact.getX()),
			min(origin.getY(), impact.getY()),
			abs(origin.getX() - impact.getX()),
			abs(origin.getY() - impact.getY())
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
		return origin;
	}
	
	public Point2D getFireImpact()
	{
		return impact;
	}
	
	public Point2D getTrackedFireOrigin()
	{
		Point2D attackerCurrent = attacker.getAbsPoint();
		return new Point2D.Double(
			origin.getX() - attackerStart.getX() + attackerCurrent.getX(),
			origin.getY() - attackerStart.getY() + attackerCurrent.getY()
		);
	}
	
	public Point2D getTrackedFireImpact()
	{
		Point2D targetCurrent = target.getAbsPoint();
		return new Point2D.Double(
			impact.getX() - targetStart.getX() + targetCurrent.getX(),
			impact.getY() - targetStart.getY() + targetCurrent.getY()
		);
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
