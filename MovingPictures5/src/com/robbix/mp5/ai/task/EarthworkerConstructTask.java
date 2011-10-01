package com.robbix.mp5.ai.task;

import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Position;
import static com.robbix.mp5.map.LayeredMap.*;
import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Unit;

public class EarthworkerConstructTask extends Task
{
	private Position target;
	private Fixture fixture;
	private int buildTime;
	private int buildProgress;
	
	public EarthworkerConstructTask(Position target, Fixture fixture, int buildTime)
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
		
		this.target = target;
		this.fixture = fixture;
		this.buildTime = buildTime;
		this.buildProgress = 0;
	}
	
	public void step(Unit unit)
	{
		if (unit.getActivity() != BULLDOZE)
		{
			Position pos = unit.getPosition();
			
			unit.setActivity(BULLDOZE);
			unit.resetAnimationFrame();
			unit.setDirection(Direction.getMoveDirection(pos, target));
		}
		
		if (buildProgress >= buildTime)
		{
			if (fixture == Fixture.WALL) unit.getMap().putWall(target);
			if (fixture == Fixture.TUBE) unit.getMap().putTube(target);
			
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
		else
		{
			unit.incrementAnimationFrame();
		}
		
		buildProgress++;
	}
}
