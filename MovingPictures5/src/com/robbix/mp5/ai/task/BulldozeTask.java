package com.robbix.mp5.ai.task;

import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.unit.Unit;

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
		if (!unit.getActivity().equals("bulldoze"))
		{
			unit.setActivity("bulldoze");
			unit.resetAnimationFrame();
			return;
		}
		
		unit.incrementAnimationFrame();
		
		if (unit.getAnimationFrame() == duration)
		{
			unit.getMap().bulldoze(unit.getPosition());
			unit.setActivity("move");
			unit.completeTask(this);
		}
	}
}
