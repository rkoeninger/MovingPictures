package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit crane;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.crane = earthworker;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawSelectedUnitBox(g, crane);
		drawInstructions(g, rect, "Build Tube", "Cancel");
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (isDragging())
			{
				if      (isDragRegionLinear()) panel.draw(g, RED, getLinearDragRegion());
				else if (isControlOptionSet()) panel.draw(g, RED, getBorderDragRegion());
				                          else panel.draw(g, RED, getLShapedDragRegion());
			}
			else
			{
				panel.draw(g, RED, pos);
			}
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doEarthworkerBuild(crane, getCursorPosition(), Fixture.TUBE);
		complete();
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
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
}
