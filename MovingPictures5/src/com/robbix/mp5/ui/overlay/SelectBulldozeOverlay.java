package com.robbix.mp5.ui.overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.ai.task.BulldozeRegionTask;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.RGraphics;
import com.robbix.mp5.utils.Region;

public class SelectBulldozeOverlay extends InputOverlay
{
	private Unit dozer;
	
	public SelectBulldozeOverlay(Unit dozer)
	{
		this.dozer = dozer;
	}
	
	public void paintImpl(RGraphics g)
	{
		drawSelectedUnitBox(g, dozer);
		
		if (isCursorOnGrid())
		{
			g.setColor(GREEN.getFill());
			
			if (isDragging()) g.fillRegion(getDragRegion());
			             else g.fillPosition(getCursorPosition());
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
