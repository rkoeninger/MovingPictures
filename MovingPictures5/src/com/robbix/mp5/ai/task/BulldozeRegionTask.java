package com.robbix.mp5.ai.task;

import static com.robbix.mp5.unit.Activity.MOVE;

import java.util.List;

import com.robbix.mp5.Game;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Filter;
import com.robbix.mp5.utils.Position;

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
				return unit.is("Dozer");
			}
		});
		
		this.spots = spots;
	}
	
	public void step(Unit unit)
	{
		if (currentSpot >= spots.size())
		{
			unit.setActivity(MOVE);
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
			Game.game.doMove(unit, toDoze, false);
		}
	}
}