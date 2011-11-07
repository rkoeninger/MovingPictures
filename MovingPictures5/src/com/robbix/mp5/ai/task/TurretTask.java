package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Filter;
import com.robbix.mp5.utils.Utils;

public class TurretTask extends Task
{
	private Filter<Unit> targetFilter;
	
	public TurretTask(Filter<Unit> targetFilter)
	{
		super(true, Task.TURRET_ONLY);
		this.targetFilter = targetFilter;
	}
	
	public void step(Unit unit)
	{
		Unit target = unit.getMap().findClosest(unit, targetFilter, 1, unit.getType().getAttackRange());
		
		if (target != null)
		{
			Mediator.doAttack(unit, target);
		}
		else
		{
			if (Utils.RNG.nextInt(100) > 0)
				return;
			
			unit.rotate(Utils.RNG.nextInt(3) - 1);
		}
	}
}
