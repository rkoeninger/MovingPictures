package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.BorderRegion;
import com.robbix.mp5.utils.LShapedRegion;
import com.robbix.mp5.utils.LinearRegion;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.RIterable;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit crane;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.crane = earthworker;
		this.shiftOptionCount = 3;
		this.showTubeConnectivity = true;
	}
	
	public void paintOverUnits(Graphics g)
	{
		drawSelectedUnitBox(g, crane);
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			boolean available = false;
			
			if (isDragging())
			{
				if (isDragRegionLinear())
				{
					LinearRegion region = getLinearDragRegion();
					available = canPlaceTube(region);
					panel.draw(g, available ? GREEN : RED, region);
				}
				else if (getShiftOption() == 2)
				{
					BorderRegion region = getBorderDragRegion();
					available = canPlaceTube(region);
					panel.draw(g, available ? GREEN : RED, region);
				}
				else
				{
					LShapedRegion region = getLShapedDragRegion(getShiftOption() == 0);
					available = canPlaceTube(region);
					panel.draw(g, available ? GREEN : RED, region);
				}
			}
			else
			{
				available = canPlaceTube(pos);
				panel.draw(g, available ? GREEN : RED, pos);
			}
			
			if (!available)
			{
				g.setColor(Color.WHITE);
				panel.draw(g, "Occupied", pos);
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
		else if (getShiftOption() == 2)
		{
			for (Position rowPos : getBorderDragRegion())
				tubeRow.add(rowPos);
			
			rotateForCloserEnd(tubeRow, crane.getPosition());
		}
		else
		{
			for (Position rowPos : getLShapedDragRegion(getShiftOption() == 0))
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
