package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.ai.task.EarthworkerConstructRowTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit earthworker;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.earthworker = earthworker;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		InputOverlay.paintSelectedUnitBox(g, earthworker);
		
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Build Tube", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (isDragging())
			{
				Region dragRegion = getLinearDragRegion();
				
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
		Mediator.doEarthworkerBuild(earthworker, panel.getPosition(x, y), Fixture.TUBE);
		panel.completeOverlay(this);
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		Region dragRegion = getLinearDragRegion();
		List<Position> tubeRow = new ArrayList<Position>();
		
		for (Position rowPos : dragRegion)
			tubeRow.add(rowPos);
		
		earthworker.assignNow(new EarthworkerConstructRowTask(tubeRow, Fixture.TUBE));
		panel.completeOverlay(this);
	}
}
