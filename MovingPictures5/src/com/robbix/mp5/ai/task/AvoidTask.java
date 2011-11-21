package com.robbix.mp5.ai.task;

import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Direction;
import com.robbix.utils.Filter;
import com.robbix.utils.Position;

public class AvoidTask extends Task
{
	private Filter<Unit> unitFilter;
	
	public AvoidTask(Filter<Unit> unitFilter)
	{
		super(true, Task.VEHICLE_ONLY);
		this.unitFilter = unitFilter;
	}

	public void step(Unit unit)
	{
		Unit other = unit.getMap().findClosest(unit, unitFilter, 1, 4);
		
		if (other == null)
			return;

		Position myPos = unit.getPosition();
		Position hisPos = other.getPosition();
		
		Direction d = Direction.getMoveDirection(myPos, hisPos).reverse();
		
		while (! unit.getMap().getBounds().contains(d.apply(myPos)))
			d = d.rotate(2);
		
		unit.assignNow(new SteerTask(d.apply(myPos)));
		unit.step();
	}
}
