package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.ai.task.EarthworkerConstructRowTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.unit.Unit;

public class BuildTubeOverlay extends InputOverlay
{
	private Unit earthworker;
	private Rectangle dragArea;
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.earthworker = earthworker;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Build Tube", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		CommandUnitOverlay.paintSelectedUnitBox(g, earthworker);
		
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (dragArea == null)
			{
				panel.draw(g, pos);
				g.setColor(TRANS_RED);
				panel.fill(g, pos);
			}
			else
			{
				int tileSize = panel.getTileSize();
				
				int xMin = (dragArea.x / tileSize) * tileSize;
				int yMin = (dragArea.y / tileSize) * tileSize;
				int xMax = (int) Math.ceil((dragArea.x + dragArea.width) / (double) tileSize) * tileSize;
				int yMax = (int) Math.ceil((dragArea.y + dragArea.height) / (double) tileSize) * tileSize;
				
				Rectangle rowArea = new Rectangle(
					xMin, yMin, xMax - xMin, yMax - yMin
				);
				
				if (rowArea.width < rowArea.height)
				{
					rowArea.width = tileSize;
				}
				else
				{
					rowArea.height = tileSize;
				}
				
				panel.draw(g, rowArea);
				g.setColor(TRANS_RED);
				panel.fill(g, rowArea);
			}
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doEarthworkerBuild(earthworker, getCursorPosition(), Fixture.TUBE);
		panel.completeOverlay(this);
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
	
	public void onAreaDragCancelled()
	{
		dragArea = null;
	}
	
	public void onAreaDragging(int x, int y, int w, int h)
	{
		dragArea = new Rectangle(x, y, w, h);
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		dragArea = null;
		
		int tileSize = panel.getTileSize();
		
		int xMin = x / tileSize;
		int yMin = y / tileSize;
		int xMax = (int) Math.ceil((x + w) / (double) tileSize);
		int yMax = (int) Math.ceil((y + h) / (double) tileSize);
		
		Rectangle rowArea = new Rectangle(
			xMin, yMin, xMax - xMin, yMax - yMin
		);
		
		List<Position> row = new ArrayList<Position>();
		
		if (rowArea.width < rowArea.height)
		{
			for (int ry = yMin; ry < yMax; ++ry)
			{
				row.add(new Position(xMin, ry));
			}
		}
		else
		{
			for (int rx = xMin; rx < xMax; ++rx)
			{
				row.add(new Position(rx, yMin));
			}
		}
		
		earthworker.assignNow(new EarthworkerConstructRowTask(row, Fixture.TUBE));
		panel.completeOverlay(this);
	}
}
