package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;

public class MineRouteTask extends Task
{
	private Position mineDock;
	private Position smelterDock;
	
	private Position resPosition;
	
	private Unit mine;
	private Unit smelter;
	
	public MineRouteTask(Unit mine, Unit smelter)
	{
		super(true, Task.TRUCK_ONLY);
		
		String mineType = mine.getType().getName();
		String smelterType = smelter.getType().getName();
		
		if (!mineType.contains("Mine"))
			throw new IllegalArgumentException("1st arg not a mine");
		if (!smelterType.contains("Smelter"))
			throw new IllegalArgumentException("2nd arg not a smelter");
		
		boolean mineIsCommon = mineType.contains("Common");
		boolean smelterIsCommon = smelterType.contains("Common");
		
		if (mineIsCommon != smelterIsCommon)
			throw new IllegalArgumentException(
				"Mine and smelter not same type");
		
		mineDock = mine.getPosition();
		resPosition = mineDock.shift(1, 0);
		smelterDock = smelter.getPosition().shift(3, 2);
		
		this.mine = mine;
		this.smelter = smelter;
	}
	
	public void step(Unit unit)
	{
		if (!unit.isTruck())
			throw new IllegalArgumentException();
		
		if (unit.isCargoEmpty())
		{
			if (unit.isAt(mineDock))
			{
				if (mine.isDead())
				{
					unit.completeTask(this);
					return;
				}
				
				if (!mine.isDisabled())
				{
					LayeredMap map = unit.getContainer();
					ResourceDeposit deposit = map.getResourceDeposit(resPosition);
					unit.assignNext(new MineTask(deposit.getLoad()));
				}
			}
			else
			{
				Mediator.doMove(unit, mineDock, false);
			}
		}
		else
		{
			if (unit.isAt(smelterDock))
			{
				if (smelter.isDead())
				{
					unit.completeTask(this);
					return;
				}
				
				if (!smelter.isDisabled())
				{
					unit.assignNext(new DockTask(smelter, Cargo.EMPTY));
				}
			}
			else
			{
				Mediator.doMove(unit, smelterDock, false);
			}
		}
	}
}
