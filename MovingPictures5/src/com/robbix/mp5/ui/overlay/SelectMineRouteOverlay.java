package com.robbix.mp5.ui.overlay;

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
		super("dock");
		
		this.trucks = trucks;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit truck : trucks)
			InputOverlay.paintSelectedUnitBox(g, truck);
		
		String leftClick = "Select ";
		
		if      (mine == null && smelter == null) leftClick += "Smelter/Mine";
		else if (mine == null && smelter != null) leftClick += "Smelter";
		else if (mine != null && smelter == null) leftClick += "Mine";
		
		drawInstructions(g, rect, leftClick, "Cancel");
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
				truck.assignNow(new MineRouteTask(mine, smelter));
			
			complete();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
