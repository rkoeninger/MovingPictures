package com.robbix.mp5.ai.task;

import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.unit.Unit;

public class RotateTask extends Task
{
	private Direction destination;
	
	private int rotationProgress = -1;
	private int rotationDisplacement;
	private int rotationSpeed;
	
	private boolean sixteenthTurn;
	
	public RotateTask(Direction destination)
	{
		super(false, Task.VEHICLE_ONLY);
		this.destination = destination;
	}
	
	public void step(Unit unit)
	{
		if (unit.getDirection() == destination)
		{
			unit.completeTask(this);
			return;
		}
		
		if (rotationProgress == -1)
		{
			rotationProgress = 0;
			rotationSpeed = unit.getType().getRotationSpeed();
			rotationDisplacement = unit.getDirection().getDisplacement(destination);
			sixteenthTurn = unit.getType().hasSixteenthTurn();
		}
		
		if (rotationDisplacement > 8 || rotationDisplacement < 0)
		{
			if (rotationProgress % rotationSpeed == 0)
			{
				unit.rotate(sixteenthTurn ? -1 : -2);
			}
		}
		else
		{
			if (rotationProgress % rotationSpeed == 0)
			{
				unit.rotate(sixteenthTurn ? +1 : +2);
			}
		}
		
		rotationProgress++;
		
		if (unit.getDirection() == destination)
		{
			unit.completeTask(this);
		}
	}
}
