package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.ui.ani.WeaponFireAnimation;
import com.robbix.mp5.unit.Unit;

public class AttackTask extends Task
{
	private Unit target;
	private WeaponFireAnimation animation;
	
	private boolean directionSet = false;
	
	public AttackTask(Unit target, WeaponFireAnimation animation)
	{
		super(true, Task.TURRET_ONLY);
		this.target = target;
		this.animation = animation;
	}
	
	public void step(Unit unit)
	{
		if (!directionSet)
		{
			unit.setDirection(Direction.getDirection(
				unit.getPosition(), target.getPosition()));
			directionSet = true;
		}
		
		if (animation.atHotPoint())
		{
			Mediator.doDamage(unit, target, unit.getType().getDamage());
			unit.completeTask(this);
		}
	}
}
