package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
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
	
	public void paintOverUnits(Graphics g)
	{
		drawSelectedUnitBox(g, dozer);
		
		if (isCursorOnGrid())
		{
			if (isDragging()) panel.draw(g, RED, getDragRegion());
			             else panel.draw(g, RED, getCursorPosition());
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		dozer.assignNow(new BulldozeRegionTask(Arrays.asList(pos)));
		complete();
	}
	
	public void onAreaDragged()
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
