package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.BorderRegion;
import com.robbix.utils.LShapedRegion;
import com.robbix.utils.LinearRegion;
import com.robbix.utils.Position;
import com.robbix.utils.RGraphics;
import com.robbix.utils.RIterable;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit crane;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.crane = earthworker;
		this.shiftOptionCount = 3;
		this.showTubeConnectivity = true;
	}
	
	public void paintImpl(RGraphics g)
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
					g.setColor(available ? GREEN.getFill() : RED.getFill());
					g.fillRegion(region);
				}
				else if (getShiftOption() == 2)
				{
					BorderRegion region = getBorderDragRegion();
					available = canPlaceTube(region);
					g.setColor(available ? GREEN.getFill() : RED.getFill());
					g.fillBorderRegion(region);
				}
				else
				{
					LShapedRegion region = getLShapedDragRegion(getShiftOption() == 0);
					available = canPlaceTube(region);
					g.setColor(available ? GREEN.getFill() : RED.getFill());
					g.fillLShapedRegion(region);
				}
			}
			else
			{
				available = canPlaceTube(pos);
				g.setColor(available ? GREEN.getFill() : RED.getFill());
				g.fillPosition(pos);
			}
			
			if (!available)
			{
				g.setColor(Color.WHITE);
				g.drawString("Occupied", pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		Game.game.doEarthworkerBuild(crane, getCursorPosition(), Fixture.TUBE);
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
		
		Game.game.doEarthworkerBuildRow(crane, tubeRow, Fixture.TUBE);
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
