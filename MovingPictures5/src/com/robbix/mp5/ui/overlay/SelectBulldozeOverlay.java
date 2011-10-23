package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.ai.task.BulldozeRegionTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.unit.Unit;

public class SelectBulldozeOverlay extends InputOverlay
{
	private Unit dozer;
	
	public SelectBulldozeOverlay(Unit dozer)
	{
		this.dozer = dozer;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawSelectedUnitBox(g, dozer);
		drawInstructions(g, rect, "Select Bulldoze Area", "Cancel");
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (isDragging())
			{
				Region dragRegion = getDragRegion();
				
				g.setColor(Color.RED);
				panel.draw(g, dragRegion);
				g.setColor(TRANS_RED);
				panel.fill(g, dragRegion);
			}
			else
			{
				g.setColor(Color.RED);
				panel.draw(g, pos);
				g.setColor(TRANS_RED);
				panel.fill(g, pos);
			}
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		dozer.assignNow(new BulldozeRegionTask(Arrays.asList(pos)));
		complete();
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		Region dragRegion = getDragRegion();
		List<Position> dozeArea = new ArrayList<Position>();
		
		for (Position areaPos : dragRegion.zigZagIterator())
			if (dozer.getMap().canPlaceUnit(areaPos) || dozer.isAt(areaPos))
				dozeArea.add(areaPos);
		
		reverseForCloserEnd(dozeArea, dozer.getPosition());
		
		dozer.assignNow(new BulldozeRegionTask(dozeArea));
		complete();
	}
	
	private void reverseForCloserEnd(List<Position> tubeRow, Position pos)
	{
		double distance1 = pos.getDistance(tubeRow.get(0));
		double distance2 = pos.getDistance(tubeRow.get(tubeRow.size() - 1));
		
		if (distance2 < distance1)
			Collections.reverse(tubeRow);
	}
}
