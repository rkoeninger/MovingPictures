package com.robbix.mp5.ai.task;

import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.unit.Unit;

public class BulldozeRegionTask extends Task
{
	private List<Position> spots;
	private int currentSpot = 0;
	
	public BulldozeRegionTask(List<Position> spots)
	{
		super(true, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit.getType().getName().contains("Dozer");
			}
		});
		
		this.spots = spots;
	}
	
	public void step(Unit unit)
	{
		if (currentSpot >= spots.size())
		{
			unit.setActivity("move");
			unit.completeTask(this);
			return;
		}
		
		Position toDoze = spots.get(currentSpot);
		
		if (unit.isAt(toDoze))
		{
			unit.assignNext(new BulldozeTask(19 * 4));
			currentSpot++;
		}
		else
		{
			Mediator.doMove(unit, toDoze, false);
		}
	}
}