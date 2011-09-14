package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	public void init()
	{
		panel.setAnimatedCursor(null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		CommandUnitOverlay.paintSelectedUnitBox(g, dozer);
		
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Select Bulldoze Area", rect.width / 2 - 55, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (isDragging())
			{
				Region dragRegion = getDragRegion();
				
				panel.draw(g, dragRegion);
				g.setColor(TRANS_RED);
				panel.fill(g, dragRegion);
			}
			else
			{
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
		panel.completeOverlay(this);
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		Region dragRegion = getDragRegion();
		List<Position> dozeArea = new ArrayList<Position>();
		
		for (Position areaPos : dragRegion)
			if (dozer.getMap().canPlaceUnit(areaPos) || dozer.isAt(areaPos))
				dozeArea.add(areaPos);
		
		dozer.assignNow(new BulldozeRegionTask(dozeArea));
		panel.completeOverlay(this);
	}
}
