package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.BorderRegion;
import com.robbix.mp5.basics.LShapedRegion;
import com.robbix.mp5.basics.LinearRegion;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.RIterable;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit crane;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.crane = earthworker;
	}
	
	public void paintOverUnits(Graphics g)
	{
		drawSelectedUnitBox(g, crane);
		
		if (isCursorOnGrid())
		{
			if (isDragging())
			{
				if (isDragRegionLinear())
				{
					LinearRegion region = getLinearDragRegion();
					panel.draw(g, canPlaceTube(region) ? GREEN : RED, region);
				}
				else if (isControlOptionSet())
				{
					BorderRegion region = getBorderDragRegion();
					panel.draw(g, canPlaceTube(region) ? GREEN : RED, region);
				}
				else
				{
					LShapedRegion region = getLShapedDragRegion();
					panel.draw(g, canPlaceTube(region) ? GREEN : RED, region);
				}
			}
			else
			{
				Position pos = getCursorPosition();
				panel.draw(g, canPlaceTube(pos) ? GREEN : RED, pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		Mediator.doEarthworkerBuild(crane, getCursorPosition(), Fixture.TUBE);
		complete();
	}
	
	public void onAreaDragged()
	{
		List<Position> tubeRow = new ArrayList<Position>();
		
		if (isDragRegionLinear())
		{
			for (Position rowPos : getLinearDragRegion())
				tubeRow.add(rowPos);
			
			reverseForCloserEnd(tubeRow, crane.getPosition());
		}
		else if (isControlOptionSet())
		{
			for (Position rowPos : getBorderDragRegion())
				tubeRow.add(rowPos);
			
			rotateForCloserEnd(tubeRow, crane.getPosition());
		}
		else
		{
			for (Position rowPos : getLShapedDragRegion())
				tubeRow.add(rowPos);
			
			reverseForCloserEnd(tubeRow, crane.getPosition());
		}
		
		Mediator.doEarthworkerBuildRow(crane, tubeRow, Fixture.TUBE);
		complete();
	}
	
	private void reverseForCloserEnd(List<Position> tubeRow, Position pos)
	{
		double distance1 = pos.getDistance(tubeRow.get(0));
		double distance2 = pos.getDistance(tubeRow.get(tubeRow.size() - 1));
		
		if (distance2 < distance1)
			Collections.reverse(tubeRow);
	}
	
	private void rotateForCloserEnd(List<Position> tubeLoop, Position pos)
	{
		double dist = Double.POSITIVE_INFINITY;
		int loopPosIndex = 0;
		int index = -1;
		
		for (Position loopPos : tubeLoop)
		{
			double currentDist = loopPos.getDistance(pos);
			
			if (currentDist < dist)
			{
				dist = currentDist;
				index = loopPosIndex;
			}
			
			loopPosIndex++;
		}
		
		Collections.rotate(tubeLoop, tubeLoop.size() - index);
	}
	
	private boolean canPlaceTube(RIterable<Position> itr)
	{
		for (Position pos : itr)
			if (!panel.getMap().canPlaceFixture(Fixture.TUBE, pos))
				return false;
		
		return true;
	}
}
