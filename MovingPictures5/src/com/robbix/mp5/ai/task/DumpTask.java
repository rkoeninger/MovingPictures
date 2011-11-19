package com.robbix.mp5.ai.task;

import static com.robbix.mp5.unit.Activity.DUMP;
import static com.robbix.mp5.unit.Activity.MOVE;

import com.robbix.mp5.Game;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;

public class DumpTask extends Task
{
	private int frameCount;
	private boolean incrementThisTime = false;
	
	public DumpTask()
	{
		super(false, TRUCK_ONLY);
	}

	public void step(Unit unit)
	{
		if (unit.getActivity() != DUMP)
		{
			unit.setActivity(DUMP);
			unit.resetAnimationFrame();
			frameCount = Game.game.getSpriteLibrary().getDumpGroupLength(unit);
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
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
	}
}
