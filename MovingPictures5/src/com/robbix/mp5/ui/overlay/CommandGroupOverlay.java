package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.JListDialog;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Unit;

public class CommandGroupOverlay extends InputOverlay
{
	private Set<Unit> units;
	
	public CommandGroupOverlay(Set<Unit> units)
	{
		super("move");
		this.units = units;
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
	}
	
	public void onCommand(String command)
	{
		if (command.equals("selfDestruct"))
		{
			for (Unit unit : units)
				Mediator.selfDestruct(unit);
			
			complete();
		}
		else if (command.equals("stop"))
		{
			for (Unit unit : units)
				unit.cancelAssignments();
		}
		else if (command.equals("transfer"))
		{
			Collection<Player> players = Mediator.game.getPlayers();
			players = new ArrayList<Player>(players);
			players.remove(units.iterator().next().getOwner());
			
			if (players.isEmpty())
				return;
			
			Object result = JListDialog.showDialog(players.toArray());
			
			if (result == null)
				return;
			
			Player player = (Player) result;
			
			for (Unit unit : units)
				unit.setOwner(player);
			
			complete();
		}
	}
	
	public void onMiddleClick(int x, int y)
	{
		Edge edge = getPointEdge(x, y);
		
		if (edge == Edge.SW)
		{
			for (Unit unit : units)
				Mediator.kill(unit);
			
			complete();
		}
		else if (edge == Edge.W)
		{
			for (Unit unit : units)
				Mediator.selfDestruct(unit);
			
			complete();
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doGroupMove(units, panel.getPosition(x, y));
		Mediator.playSound("beep2");
	}
}
