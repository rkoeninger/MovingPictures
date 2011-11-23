package com.robbix.mp5.ai.task;

import com.robbix.mp5.Game;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Filter;
import com.robbix.utils.Utils;

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
			Game.game.doAttack(unit, target);
		}
		else
		{
			if (Utils.randInt(0, 100) > 0)
				return;
			
			unit.rotate(Utils.randInt(-1, 1));
		}
	}
}
