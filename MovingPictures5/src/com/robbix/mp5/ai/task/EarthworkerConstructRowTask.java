package com.robbix.mp5.ai.task;

import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap.Fixture;
import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Unit;

public class EarthworkerConstructRowTask extends Task
{
	private Fixture fixture;
	private List<Position> targets;
	private int currentTarget;
	
	public EarthworkerConstructRowTask(List<Position> targets, Fixture fixture)
	{
		super(true, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit.getType().getName().contains("Earthworker");
			}
		});
		
		if ((fixture != Fixture.WALL) && (fixture != Fixture.TUBE))
			throw new IllegalArgumentException("must be wall or tube");
		
		this.targets = targets;
		this.fixture = fixture;
		this.currentTarget = 0;
	}
	
	public void step(Unit unit)
	{
		if (currentTarget >= targets.size())
		{
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
		else
		{
			Mediator.doEarthworkerBuild(unit, targets.get(currentTarget), fixture);
			currentTarget++;
		}
	}
}
