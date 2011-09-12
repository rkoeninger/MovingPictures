package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;

import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.MineRouteTask;
import com.robbix.mp5.unit.Unit;

public class SelectMineRouteOverlay extends InputOverlay
{
	private Unit smelter, mine;
	private Set<Unit> trucks;
	
	public SelectMineRouteOverlay(Unit truck)
	{
		this(Utils.asSet(truck));
	}
	
	public SelectMineRouteOverlay(Set<Unit> trucks)
	{
		this.trucks = trucks;
	}
	
	public void init()
	{
		panel.setAnimatedCursor("dock");
	}
	
	public void dispose()
	{
		panel.setAnimatedCursor(null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		
		if (mine == null && smelter == null)
		{
			g.drawString("Left Click to Select Mine/Smelter", rect.width / 2 - 45, 30);
		}
		else if (mine == null && smelter != null)
		{
			g.drawString("Left Click to Select Mine", rect.width / 2 - 35, 30);
		}
		else if (mine != null && smelter == null)
		{
			g.drawString("Left Click to Select Smelter", rect.width / 2 - 35, 30);
		}
		
		g.drawString("Right Click to Cancel", rect.width / 2 - 25, 50);
		g.translate(-rect.x, -rect.y);
		
		for (Unit truck : trucks)
		{
			CommandUnitOverlay.paintSelectedUnitBox(g, truck);
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Unit selected = panel.getMap().getUnitAbsolute(x, y);
		
		if (selected != null)
		{
			if (selected.isMine())
			{
				mine = selected;
			}
			else if (selected.getType().getName().contains("Smelter"))
			{
				smelter = selected;
			}
		}
		
		if (mine != null && smelter != null)
		{
			for (Unit truck : trucks)
			{
				truck.assignNow(new MineRouteTask(mine, smelter));
			}
			
			panel.completeOverlay(this);
		}
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
}
