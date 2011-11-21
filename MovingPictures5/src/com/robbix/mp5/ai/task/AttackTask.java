package com.robbix.mp5.ai.task;

import com.robbix.mp5.ui.ani.WeaponAnimation;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Direction;

public class AttackTask extends Task
{
	private Unit target;
	private WeaponAnimation animation;
	
	private boolean directionSet = false;
	
	public AttackTask(Unit target, WeaponAnimation animation)
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
			unit.completeTask(this);
		}
	}
}
