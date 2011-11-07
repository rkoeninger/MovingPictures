package com.robbix.mp5.ai.task;

import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Direction;

/**
 * This task runs with the presumption that the Unit is permitted to enter
 * the destination position and will not be pre-empted by another Unit.
 */
public class MoveTask extends Task
{
	private int spotSize;
	
	private double absX;
	private double absY;
	private double initAbsX;
	private double initAbsY;
	
	public MoveTask()
	{
		super(false, Task.VEHICLE_ONLY);
		absX = absY = initAbsX = initAbsY = Double.NaN;
	}
	
	public MoveTask(Unit unit)
	{
		super(false, Task.VEHICLE_ONLY);
		spotSize = unit.getMap().getSpotSize();
		initAbsX = unit.getX() * spotSize + unit.getXOffset();
		initAbsY = unit.getY() * spotSize + unit.getYOffset();
		absX = initAbsX;
		absY = initAbsY;
	}
	
	public void step(Unit unit)
	{
		if (Double.isNaN(initAbsX))
		{
			spotSize = unit.getMap().getSpotSize();
			initAbsX = unit.getX() * spotSize + unit.getXOffset();
			initAbsY = unit.getY() * spotSize + unit.getYOffset();
			absX = initAbsX;
			absY = initAbsY;
		}
		
		final Direction dir = unit.getDirection();
		
		absX += unit.getSpeed() * dir.cos();
		absY += unit.getSpeed() * dir.sin();
		
		final int xOff = ((int) Math.round(absX)) - unit.getX() * spotSize;
		final int yOff = ((int) Math.round(absY)) - unit.getY() * spotSize;
		
		final int frameIncrement =
			Math.max(Math.abs(xOff - unit.getXOffset()),
					 Math.abs(yOff - unit.getYOffset()));
		
		if (frameIncrement > 0)
			unit.incrementAnimationFrame(); // TODO: how many frames to inc?
		
		unit.setXOffset(xOff);
		unit.setYOffset(yOff);
		
		int xGridShift = 0;
		int yGridShift = 0;
		
		/*
		 * If unit has crossed the halfway point between
		 * current cell and next cell (on the x-axis),
		 * set marker to shift position on the grid.
		 */
		if (dir.getDX() > 0 && unit.getXOffset() > (spotSize / 2))
		{
			unit.shiftXOffset(-spotSize);
			xGridShift = 1;
		}
		else if (dir.getDX() < 0 && unit.getXOffset() < -(spotSize / 2))
		{
			unit.shiftXOffset(spotSize);
			xGridShift = -1;
		}

		/*
		 * If unit has crossed the halfway point between
		 * current cell and next cell (on the y-axis),
		 * set marker to shift position on the grid.
		 */
		if (dir.getDY() > 0 && unit.getYOffset() > (spotSize / 2))
		{
			unit.shiftYOffset(-spotSize);
			yGridShift = 1;
		}
		else if (dir.getDY() < 0 && unit.getYOffset() < -(spotSize / 2))
		{
			unit.shiftYOffset(spotSize);
			yGridShift = -1;
		}
		
		/*
		 * If a shift position marker was set...
		 * - Unreserve current position.
		 * - Move to new position.
		 * - Unreserve new position.
		 */
		if (xGridShift != 0 || yGridShift != 0)
		{
			unit.getMap().unreserve(unit.getPosition());
			unit.getMap().shift(unit, xGridShift, yGridShift);
			unit.getMap().unreserve(unit.getPosition());
		}
		
		/*
		 * If unit has reached (or surpassed) the center of the next cell,
		 * recenter unit and end task.
		 */
		if ((dir.getDX() > 0 && absX - initAbsX >=  spotSize)
		||  (dir.getDX() < 0 && absX - initAbsX <= -spotSize)
		||	(dir.getDY() > 0 && absY - initAbsY >=  spotSize)
		||  (dir.getDY() < 0 && absY - initAbsY <= -spotSize))
		{
			unit.setXOffset(0);
			unit.setYOffset(0);
			unit.completeTask(this);
			unit.step();
		}
	}
}
