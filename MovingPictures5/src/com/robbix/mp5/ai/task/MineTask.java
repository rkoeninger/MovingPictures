package com.robbix.mp5.ai.task;

import com.robbix.mp5.Game;

import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Direction;
import com.robbix.mp5.utils.Position;

public class MineTask extends Task
{
	private int frameCount = 60;
	private Cargo toLoad;
	
	private int state = 0;
	
	private boolean incrementThisTime = false;

	public MineTask(Cargo toLoad)
	{
		super(false, Task.TRUCK_ONLY);
		this.toLoad = toLoad;
	}

	public void step(Unit unit)
	{
		if (state == 0)
		{
			if (unit.getDirection() != Direction.W)
			{
				unit.assignNext(new RotateTask(Direction.W));
			}
			else
			{
				unit.setActivity(MINELOAD);
				unit.setCargo(toLoad);
				unit.resetAnimationFrame();
				state = 1;
			}
		}
		else if (state == 1 && unit.getAnimationFrame() >= frameCount)
		{
			unit.setActivity(MOVE);
			unit.resetAnimationFrame();
			unit.completeTask(this);
		}
		else
		{
			Position pos = unit.getPosition();
			
			/*
			 * Increment animation frame every other step to slow animation
			 */
			if (incrementThisTime)
			{
				unit.incrementAnimationFrame();
				
				if (unit.getAnimationFrame() == 30)
				{
					Game.game.playSound("dockGrab", pos);
				}
				else if (unit.getAnimationFrame() == 55)
				{
					Game.game.playSound("dockOpen", pos);
				}
			}
			
			incrementThisTime = !incrementThisTime;
		}
	}
}
