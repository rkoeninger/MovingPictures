package com.robbix.mp5.ai.task;

import com.robbix.mp5.Mediator;

import static com.robbix.mp5.unit.Activity.*;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Direction;
import com.robbix.mp5.utils.Filter;
import com.robbix.mp5.utils.Position;

public class ConVecConstructTask extends Task
{
	private Unit target;
	private Position targetPos;
	
	public ConVecConstructTask(Unit target, Position targetPos)
	{
		super(true, new Filter<Unit>()
		{
			public boolean accept(Unit unit)
			{
				return unit.getType().getName().contains("ConVec");
			}
		});
		
		if (!target.isStructure() && !target.getType().isGuardPostType())
			throw new IllegalArgumentException("construct target not struct");
		
		this.target = target;
		this.targetPos = targetPos;
	}
	
	public void step(Unit unit)
	{
		if (unit.getActivity() != CONSTRUCT)
		{
			unit.setActivity(CONSTRUCT);
			unit.resetAnimationFrame();
			unit.setDirection(Direction.SW);
			unit.setCargo(Cargo.EMPTY);
			unit.getMap().putUnit(target, targetPos);
			
			SpriteLibrary lib = Mediator.game.getSpriteLibrary();
			int buildFrames = lib.getBuildGroupLength(target.getType());
			
			target.setActivity(BUILD);
			target.assignNow(new BuildTask(buildFrames, target.getType().getBuildTime()));
		}
		else if (target.getActivity() != BUILD)
		{
			unit.setActivity(MOVE);
			unit.completeTask(this);
		}
		else
		{
			unit.incrementAnimationFrame();
		}
	}
}
