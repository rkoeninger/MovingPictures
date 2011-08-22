package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.unit.Unit;

public class SelfDestructAttackTask extends Task
{
	private Filter<Unit> targetFilter;
	
	public SelfDestructAttackTask(Filter<Unit> targetFilter)
	{
		super(true, Task.TURRET_ONLY);
		this.targetFilter = targetFilter;
	}
	
	public void step(Unit unit)
	{
		Unit target = unit.getMap().findClosest(unit, targetFilter, 1, unit.getType().getAttackRange());
		
		if (target != null)
		{
			Mediator.selfDestruct(unit);
		}
	}
}
