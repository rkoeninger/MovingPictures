package com.robbix.mp5.ai.task;

import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Unit;

public class BuildTask extends Task
{
	private int buildFrames;
	private int buildProgress;
	private int buildTime;
	
	public BuildTask(int buildFrames, int buildTime)
	{
		super(false, Task.STRUCTURE_OR_GUARDPOST);
		
		this.buildFrames = buildFrames;
		this.buildProgress = 0;
		this.buildTime = buildTime;
	}
	
	public void step(Unit unit)
	{
		if (unit.getActivity() != BUILD)
		{
			unit.setActivity(BUILD);
			unit.resetAnimationFrame();
		}
		else
		{
			unit.setAnimationFrame(
				(int)(buildFrames * (buildProgress / (float)buildTime))
			);
			
			buildProgress++;
			
			if (buildProgress >= buildTime)
			{
				if (unit.getType().isGuardPostType())
				{
					unit.setActivity(TURRET);
				}
				else
				{
					unit.setActivity(STILL);
				}
				
				unit.completeTask(this);
			}
		}
	}
}
