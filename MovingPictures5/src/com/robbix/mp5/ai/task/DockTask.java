package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.player.Player;
import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Direction;
import com.robbix.mp5.utils.Position;

public class DockTask extends Task
{
	private int downFrameCount = 120;
	private int upFrameCount = 125;
	private int state = 0;
	private Cargo toLoad;
	private Unit smelter;
	
	private boolean incrementThisTime = false;
	
	public DockTask(Unit smelter, Cargo toLoad)
	{
		super(false, Task.DOCKABLE);
		this.smelter = smelter;
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
				unit.setActivity(DOCKDOWN);
				unit.resetAnimationFrame();
				state = 1;
			}
		}
		else if (state == 1 && unit.getAnimationFrame() >= downFrameCount - 1)
		{
			Cargo cargo = unit.getCargo();
			
			if (toLoad != null && cargo != null && cargo.getType() == Cargo.Type.COMMONORE)
			{
				Player owner = smelter.getOwner();
				ResourceType type = null;
				
				switch (cargo.getType())
				{
				case COMMONORE:
					type = ResourceType.COMMON_ORE;
					break;
				case RAREORE:
					type = ResourceType.RARE_ORE;
					break;
				default:
					throw new Error("not valid cargo type");
				}
				
				owner.addResource(type, cargo.getAmount());
				toLoad = null;
			}
			
			unit.setActivity(DOCKUP);
			unit.setCargo(toLoad);
			unit.resetAnimationFrame();
			state = 2;
		}
		else if (state == 2 && unit.getAnimationFrame() >= upFrameCount - 1)
		{
			unit.setActivity(MOVE);
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
				if (state == 1 && unit.getAnimationFrame() == 0)
				{
					Mediator.playSound("dockOpen", pos);
				}
				else if (state == 1 && unit.getAnimationFrame() == 30)
				{
					Mediator.playSound("dockGrab", pos);
				}
				else if (state == 1 && unit.getAnimationFrame() == 80)
				{
					Mediator.playSound("dockLower", pos);
				}
				else if (state == 2 && upFrameCount - unit.getAnimationFrame() == 5)
				{
					Mediator.playSound("dockOpen", pos);
				}
				else if (state == 2 && upFrameCount - unit.getAnimationFrame() == 45)
				{
					Mediator.playSound("dockGrab", pos);
				}
				else if (state == 2 && upFrameCount - unit.getAnimationFrame() == 125)
				{
					Mediator.playSound("dockLower", pos);
				}
				
				unit.incrementAnimationFrame();
			}
			
			incrementThisTime = !incrementThisTime;
		}
	}
}
