package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.util.Set;

import com.robbix.mp5.ai.task.MineRouteTask;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Utils;

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
	
	public void paintImpl(Graphics g)
	{
		for (Unit truck : trucks)
			drawSelectedUnitBox(g, truck);
	}
	
	public void onLeftClick()
	{
		Unit selected = panel.getMap().getUnit(getCursorPosition());
		
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
}
