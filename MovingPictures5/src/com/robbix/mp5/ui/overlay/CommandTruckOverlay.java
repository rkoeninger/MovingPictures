package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.DockTask;
import com.robbix.mp5.ai.task.DumpTask;
import com.robbix.mp5.ai.task.MineTask;
import com.robbix.mp5.basics.JListDialog;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.player.Player;
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
	
	public void paintOverUnits(Graphics g)
	{
		for (Unit truck : trucks)
			drawSelectedUnitBox(g, truck);
	}
	
	public void onCommand(String command)
	{
		if (command.equals("selfDestruct"))
		{
			for (Unit truck : trucks)
				Mediator.selfDestruct(truck);
			
			complete();
		}
		else if (command.equals("kill"))
		{
			for (Unit truck : trucks)
				Mediator.kill(truck);
			
			complete();
		}
		else if (command.equals("dump"))
		{
			for (Unit truck : trucks)
				if (!truck.isCargoEmpty())
				{
					Mediator.playSound("dump", truck.getPosition());
					truck.interrupt(new DumpTask());
				}
		}
		else if (command.equals("patrol"))
		{
			push(new SelectMineRouteOverlay(trucks));
		}
		else if (command.equals("transfer"))
		{
			Collection<Player> players = Mediator.game.getPlayers();
			players = new ArrayList<Player>(players);
			players.remove(trucks.iterator().next().getOwner());
			
			if (players.isEmpty())
				return;
			
			Object result = JListDialog.showDialog(players.toArray());
			
			if (result == null)
				return;
			
			Player player = (Player) result;
			
			for (Unit truck : trucks)
				truck.setOwner(player);
			
			complete();
		}
		else if (command.equals("dock") && trucks.size() == 1)
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
		else if (command.equals("mine") && trucks.size() == 1)
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
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		for (Unit truck : trucks)		
			Mediator.doMove(truck, pos);
		
		Mediator.playSound("beep2");
	}
}
