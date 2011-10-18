package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.Unit;

public class BuildMineTask extends Task
{
	private Unit mine;
	
	public BuildMineTask(Unit mine)
	{
		super(false, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit != null && unit.getType().getName().endsWith("Miner");
			}
		});
		
		this.mine = mine;
	}
	
	public void step(Unit unit)
	{
		Position pos = unit.getPosition();
		LayeredMap map = unit.getMap();

		int buildFrames = Mediator.game.getSpriteLibrary()
									   .getUnitSpriteSet(mine.getType())
									   .get(Activity.BUILD)
									   .getFrameCount();
		
		map.remove(unit);
		map.putUnit(mine, pos.shift(-1, 0));
		mine.assignNow(new BuildTask(buildFrames, 50));
		unit.completeTask(this);
	}
}
