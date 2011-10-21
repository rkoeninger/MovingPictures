package com.robbix.mp5.ui.overlay;

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
		this(Utils.asSet(truck));
	}
	
	public CommandTruckOverlay(Set<Unit> trucks)
	{
		super("move");
		this.trucks = trucks;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		for (Unit truck : trucks)
			InputOverlay.paintSelectedUnitBox(g, truck);
		
		drawInstructions(g, rect, "Move", "Command", "Cancel");
		drawCommand(g, rect, Edge.SW, "Kill");
		drawCommand(g, rect, Edge.W,  "SD");
		drawCommand(g, rect, Edge.NW, "Dump");
		drawCommand(g, rect, Edge.E,  "Route");
		
		if (trucks.size() == 1)
			drawCommand(g, rect, Edge.SE, "Dock");
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
					Mediator.playSound("dump", truck.getPosition());
					truck.interrupt(new DumpTask());
				}
		}
		else if (edge == Edge.E)
		{
			push(new SelectMineRouteOverlay(trucks));
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
