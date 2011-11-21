package com.robbix.mp5.ai.task;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Filter;
import com.robbix.utils.Position;

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
		
		SpriteLibrary lib = Game.game.getSpriteLibrary();
		int buildFrames = lib.getBuildGroupLength(mine.getType());
		
		map.remove(unit);
		map.putUnit(mine, pos.shift(-1, 0));
		mine.setActivity(Activity.BUILD);
		mine.assignNow(new BuildTask(buildFrames, mine.getType().getBuildTime()));
		unit.completeTask(this);
	}
}
