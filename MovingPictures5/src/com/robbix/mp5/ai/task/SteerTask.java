package com.robbix.mp5.ai.task;

import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.unit.Unit;

public class SteerTask extends Task
{
	private Position destination;
	
	public SteerTask(Position destination)
	{
		super(true, Task.VEHICLE_ONLY);
		this.destination = destination;
	}
	
	/**
	 * Unit should be centered in a square when this is called.
	 */
	public void step(Unit unit)
	{
		if (unit.getPosition().equals(destination))
		{
			unit.resetAnimationFrame();
			unit.completeTask(this);
			return;
		}
		
		Position pos = unit.getPosition();
		Direction dir = Direction.getMoveDirection(pos, destination);
		Position next = dir.apply(pos);
		
		if (unit.getMap().canMoveUnit(pos, dir))
		{
			if (unit.getDirection() != dir)
			{
				unit.assignNext(new RotateTask(dir));
				unit.step();
				return;
			}
			
			unit.getMap().reserve(next, unit);
			unit.assignNext(new MoveTask());
			unit.step();
		}
		else
		{
			if (!next.equals(destination))
			{
				for (Direction alt : Direction.getAlternatives(dir))
				{
					next = alt.apply(pos);
					Position prev = unit.getPreviousPosition();
					
					if (unit.getMap().canMoveUnit(pos, alt) && !next.equals(prev))
					{
						unit.assignNext(new SteerTask(next));
						unit.step();
						return;
					}
				}
			}
			
			if (unit.getDirection() != dir)
			{
				unit.assignNext(new RotateTask(dir));
				unit.step();
				return;
			}
			
			// All paths fail, spin wheels
			unit.incrementAnimationFrame();
		}
	}
}
