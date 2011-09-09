package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.unit.Unit;

public class CommandGroupOverlay extends InputOverlay
{
	private Set<Unit> units;
	private boolean trucks;
	
	public CommandGroupOverlay(Set<Unit> units)
	{
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
	
	public void init()
	{
		getDisplay().setAnimatedCursor("move");
	}
	
	public void dispose()
	{
		getDisplay().showStatus((Unit)null);
		getDisplay().setAnimatedCursor(null);
	}
	
	public void onRightClick(int x, int y)
	{
		getDisplay().completeOverlay(this);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit unit : units)
		{
			CommandUnitOverlay.paintSelectedUnitBox(g, unit);
		}
		
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		final int h = rect.height;
		g.setColor(Color.RED);
		
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Move", w / 2 - 25, 30);
		g.drawString("Middle Click on Command", w / 2 - 25, 50);
		g.drawString("Right Click to Cancel", w / 2 - 25, 70);
		
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
		final int w = getDisplay().getVisibleRect().width;
		final int h = getDisplay().getVisibleRect().height;
		final int x0 = getDisplay().getVisibleRect().x;
		final int y0 = getDisplay().getVisibleRect().y;
		
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
			
			getDisplay().pushOverlay(new SelectMineRouteOverlay(trucks));
			return;
		}
		
		getDisplay().completeOverlay(this);
	}
	
	public void onLeftClick(int x, int y)
	{
		final int tileSize = getDisplay().getMap().getTileSize();
		Position targetPos = new Position(x / tileSize, y / tileSize);
		Mediator.doGroupMove(units, targetPos);
		Mediator.sounds.play("beep2");
	}
}
