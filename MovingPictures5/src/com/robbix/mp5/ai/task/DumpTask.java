package com.robbix.mp5.ai.task;

import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;

public class DumpTask extends Task
{
	private int frameCount = 15; // TODO: get frame count somehow
	
	private boolean incrementThisTime = false;
	
	public DumpTask()
	{
		super(false, TRUCK_ONLY);
	}

	public void step(Unit unit)
	{
		if (!unit.getActivity().equals("dump"))
		{
			unit.setActivity("dump");
			unit.resetAnimationFrame();
		}
		else
		{
			/*
			 * Increment animation frame every other step to slow animation
			 */
			if (incrementThisTime)
				unit.incrementAnimationFrame();
			
			incrementThisTime = !incrementThisTime;
		}
		
		if (unit.getAnimationFrame() >= frameCount - 1)
		{
			unit.setCargo(Cargo.EMPTY);
			unit.setActivity("move");
			unit.completeTask(this);
		}
	}
}
