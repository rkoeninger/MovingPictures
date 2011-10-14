package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.DockTask;
import com.robbix.mp5.ai.task.DumpTask;
import com.robbix.mp5.ai.task.MineTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;

public class CommandTruckOverlay extends InputOverlay
{
	private Set<Unit> trucks;
	
	public CommandTruckOverlay(Unit truck)
	{
		this.trucks = Utils.asSet(truck);
	}
	
	public CommandTruckOverlay(Set<Unit> trucks)
	{
		this.trucks = trucks;
	}
	
	public void init()
	{
		panel.setAnimatedCursor("move");
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit truck : trucks)
			InputOverlay.paintSelectedUnitBox(g, truck);
		
		int w = rect.width;
		int h = rect.height;
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Move", w / 2 - 25, 30);
		g.drawString("Middle Click on Command", w / 2 - 25, 50);
		g.drawString("Right Click to Cancel", w / 2 - 25, 70);
		g.setFont(Font.decode("Arial-bold-20"));
		g.drawString("Kill", 20, h - 30);
		g.drawString("SD", 20, h / 2 + 10);
		g.drawString("Dump", 20, 25);
		g.drawString("Route", w - 70, h / 2 - 30);
		
		if (trucks.size() == 1)
			g.drawString("Dock", w - 50, h - 30);
		
		g.translate(-rect.x, -rect.y);
	}
	
	public void onLeftClick(int x, int y)
	{
		for (Unit truck : trucks)		
			Mediator.doMove(truck, panel.getPosition(x, y));
		
		Mediator.playSound("beep2");
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void onMiddleClick(int x, int y)
	{
		Point p = panel.subtractViewOffset(new Point(x, y));
		Edge edge = getPointEdge(p.x, p.y);
		
		if (edge == Edge.SW)
		{
			for (Unit truck : trucks)
				Mediator.kill(truck);
			
			complete();
		}
		else if (edge == Edge.W)
		{
			for (Unit truck : trucks)
				Mediator.selfDestruct(truck);
			
			complete();
		}
		else if (edge == Edge.NW)
		{
			for (Unit truck : trucks)
				if (!truck.isCargoEmpty())
				{
					Mediator.playSound("dump");
					truck.interrupt(new DumpTask());
				}
		}
		else if (edge == Edge.E)
		{
			panel.pushOverlay(new SelectMineRouteOverlay(trucks));
		}
		else if (edge == Edge.SE && trucks.size() == 1)
		{
			Unit truck = trucks.iterator().next();
			
			if (truck.isCargoEmpty())
				return;
			
			Position adj = truck.getPosition().shift(0, -1);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(adj))
			{
				Unit smelter = map.getUnit(adj);
				
				if (smelter != null && smelter.getType().getName().contains("Smelter"))
				{
					if (!smelter.isDead() && !smelter.isDisabled())
					{
						truck.assignNow(new DockTask(smelter, Cargo.EMPTY));
					}
				}
			}
		}
		else if (edge == Edge.NE && trucks.size() == 1)
		{
			Unit truck = trucks.iterator().next();
			
			if (!truck.isCargoEmpty())
				return;
			
			Position adj = truck.getPosition().shift(1, 0);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(adj))
			{
				Unit mine = map.getUnit(adj);
				ResourceDeposit deposit = map.getResourceDeposit(adj);
				
				if (mine != null && mine.getType().getName().contains("Mine"))
				{
					if (deposit == null)
						throw new IllegalStateException("mine doesn't have deposit");
					
					if (!mine.isDead() && !mine.isDisabled())
					{
						truck.assignNow(new MineTask(deposit.getLoad()));
					}
				}
			}
		}
	}
}
