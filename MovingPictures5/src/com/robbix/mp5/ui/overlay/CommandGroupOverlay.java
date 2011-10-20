package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
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
		{
			if (unit.isTruck())
			{
				trucks = true;
				break;
			}
		}
	}
	
	public void dispose()
	{
		panel.showStatus((Unit)null);
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit unit : units)
		{
			InputOverlay.paintSelectedUnitBox(g, unit);
		}
		
		drawInstructions(g, rect, "Move", "Give Command", "Cancel");
		
		g.translate(rect.x, rect.y);
		int w = rect.width;
		int h = rect.height;
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-bold-20"));
		g.drawString("Kill", 20, h - 30);
		g.drawString("SD", 20, h / 2 + 10);
		
		if (trucks)
		{
			g.drawString("Route", w - 70, h / 2 - 30);
		}
		
		g.translate(-rect.x, -rect.y);
	}
	
	public void onMiddleClick(int x, int y)
	{
		Rectangle rect = panel.getVisibleRect();
		int w = rect.width;
		int h = rect.height;
		int x0 = rect.x;
		int y0 = rect.y;
		
		int edge = ((x - x0) / (w / 3)) + (((y - y0) / (h / 3)) * 3);
		
		if (edge == 6)
		{
			for (Unit unit : units)
			{
				Mediator.kill(unit);
			}
		}
		else if (edge == 3)
		{
			for (Unit unit : units)
			{
				Mediator.selfDestruct(unit);
			}
		}
		else if (edge == 5)
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
