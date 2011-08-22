package com.robbix.mp5.ai.task;

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
		if (!unit.getActivity().equals("build"))
		{
			unit.setActivity("build");
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
					unit.setActivity("turret");
				}
				else
				{
					unit.setActivity("still");
				}
				
				unit.completeTask(this);
			}
		}
	}
}
