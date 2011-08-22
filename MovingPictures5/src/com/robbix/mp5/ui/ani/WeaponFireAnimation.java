package com.robbix.mp5.ui.ani;

import com.robbix.mp5.unit.Unit;

public abstract class WeaponFireAnimation extends AmbientAnimation
{
	private Unit attacker;
	private Unit target;
	
	public WeaponFireAnimation(Unit attacker, Unit target)
	{
		this.attacker = attacker;
		this.target = target;
	}
	
	public Unit getAttacker()
	{
		return attacker;
	}
	
	public Unit getTarget()
	{
		return target;
	}
	
	/**
	 * Returns true if the animation is at the frame where damage would be done.
	 */
	public abstract boolean atHotPoint();
}
