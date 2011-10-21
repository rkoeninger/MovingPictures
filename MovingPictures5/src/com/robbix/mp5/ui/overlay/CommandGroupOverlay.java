package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.unit.Unit;

public class CommandGroupOverlay extends InputOverlay
{
	private Set<Unit> units;
	private boolean trucks;
	
	public CommandGroupOverlay(Set<Unit> units)
	{
		super("move");
		this.units = units;
		trucks = false;
		
		for (Unit unit : units)
			if (unit.isTruck())
			{
				trucks = true;
				break;
			}
	}
	
	public void dispose()
	{
		super.dispose();
		panel.showStatus((Unit)null);
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit unit : units)
			InputOverlay.paintSelectedUnitBox(g, unit);
		
		drawInstructions(g, rect, "Move", "Give Command", "Cancel");
		drawCommand(g, rect, Edge.SW, "Kill");
		drawCommand(g, rect, Edge.W,  "SD");
		
		if (trucks)
			drawCommand(g, rect, Edge.E, "Route");
	}
	
	public void onMiddleClick(int x, int y)
	{
		Edge edge = getPointEdge(x, y);
		
		if (edge == Edge.SW)
		{
			for (Unit unit : units)
			{
				Mediator.kill(unit);
			}
		}
		else if (edge == Edge.W)
		{
			for (Unit unit : units)
			{
				Mediator.selfDestruct(unit);
			}
		}
		else if (edge == Edge.E)
		{
			Set<Unit> trucks = new HashSet<Unit>();
			
			for (Unit unit : units)
			{
				if (unit.isTruck())
				{
					trucks.add(unit);
				}
			}
			
			push(new SelectMineRouteOverlay(trucks));
			return;
		}
		
		panel.completeOverlay(this);
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doGroupMove(units, panel.getPosition(x, y));
		Mediator.playSound("beep2");
	}
}
