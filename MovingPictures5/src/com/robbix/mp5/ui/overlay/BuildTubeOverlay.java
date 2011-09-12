package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
	private Position pos = null;
	private Rectangle dragArea;
	static Color TRANS_RED = new Color(255, 0, 0, 127);
	
	public BuildTubeOverlay(Unit earthworker)
	{
		this.earthworker = earthworker;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Build Tube", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		CommandUnitOverlay.paintSelectedUnitBox(g, earthworker);
		
		if (pos == null)
			return;
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		if (dragArea == null)
		{
			g.drawRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
			g.setColor(TRANS_RED);
			g.fillRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
		}
		else
		{
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
			
			g.drawRect(rowArea.x, rowArea.y, rowArea.width, rowArea.height);
			g.setColor(TRANS_RED);
			g.fillRect(rowArea.x, rowArea.y, rowArea.width, rowArea.height);
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doEarthworkerBuild(earthworker, pos, Fixture.TUBE);
		getDisplay().completeOverlay(this);
	}
	
	public void onRightClick(int x, int y)
	{
		getDisplay().completeOverlay(this);
	}
	
	public void onAreaDragCancelled()
	{
		dragArea = null;
	}
	
	public void onAreaDragging(int x, int y, int w, int h)
	{
		dragArea = new Rectangle(x, y, w, h);
		pos = new Position(
			x / getDisplay().getMap().getTileSize(),
			y / getDisplay().getMap().getTileSize()
		);
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		dragArea = null;
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
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
		getDisplay().completeOverlay(this);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseEntered(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseExited(MouseEvent e)
	{
		pos = null;
	}
}
