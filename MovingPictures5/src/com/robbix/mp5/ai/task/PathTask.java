package com.robbix.mp5.ai.task;

import java.util.List;

import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Direction;
import com.robbix.utils.Position;

public class PathTask extends Task
{
	private List<Position> path;
	private int pathIndex;
	private Position destination;
	
	public PathTask(List<Position> path)
	{
		super(true, Task.VEHICLE_ONLY);
		this.path = path;
		this.destination = path.get(path.size() - 1);
		
		this.pathIndex = 1; // Skip the first pos, it is current pos
	}
	
	public void step(Unit unit)
	{
		if (unit.getPosition().equals(destination))
		{
			unit.resetAnimationFrame();
			unit.completeTask(this);
			return;
		}
		
		Position next = path.get(pathIndex++);
		Position current = unit.getPosition();
		Direction dir = Direction.getMoveDirection(current, next);
		
		if (unit.getMap().canMoveUnit(current, dir) || next.equals(destination))
		{
			unit.assignNext(new SteerTask(next));
			unit.step();
		}
		else
		{
			Position prevNext = current;
			
			// Steer around obstacle
			while (!unit.getMap().canMoveUnit(prevNext, dir) && !next.equals(destination))
			{
				prevNext = next;
				next = path.get(pathIndex++);
				dir = Direction.getMoveDirection(prevNext, next);
			}
			
			unit.assignNext(new SteerTask(next));
			unit.step();
		}
	}
}
