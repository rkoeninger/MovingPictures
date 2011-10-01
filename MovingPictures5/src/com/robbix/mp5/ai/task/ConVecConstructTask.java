package com.robbix.mp5.ai.task;

import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Filter;
import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Unit;

public class ConVecConstructTask extends Task
{
	private Unit target;
	
	public ConVecConstructTask(Unit target)
	{
		super(true, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit.getType().getName().contains("ConVec");
			}
		});
		
		if (!target.isStructure() && !target.getType().isGuardPostType())
			throw new IllegalArgumentException("construct target not struct");
		
		this.target = target;
	}
	
	public void step(Unit unit)
	{
		if (unit.getActivity() != CONSTRUCT)
		{
			unit.setActivity(CONSTRUCT);
			unit.resetAnimationFrame();
			unit.setDirection(Direction.SW);
		}
		else if (target.getActivity() != BUILD)
		{
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
		else
		{
			unit.incrementAnimationFrame();
		}
	}
}
