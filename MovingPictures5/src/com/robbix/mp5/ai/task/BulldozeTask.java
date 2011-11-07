package com.robbix.mp5.ai.task;

import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Filter;

public class BulldozeTask extends Task
{
	private int duration;
	
	public BulldozeTask(int duration)
	{
		super(true, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit.getType().getName().contains("Dozer");
			}
		});
		
		this.duration = duration;
	}
	
	public void step(Unit unit)
	{
		if (unit.getActivity() != BULLDOZE)
		{
			unit.setActivity(BULLDOZE);
			unit.resetAnimationFrame();
			return;
		}
		
		unit.incrementAnimationFrame();
		
		if (unit.getAnimationFrame() == duration)
		{
			unit.getMap().bulldoze(unit.getPosition());
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
	}
}
