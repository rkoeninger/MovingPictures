package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.JListDialog;

public class CommandGroupOverlay extends InputOverlay
{
	private Set<Unit> units;
	private Unit leadUnit;
	
	public CommandGroupOverlay(Unit leadUnit, Set<Unit> units)
	{
		super("move");
		this.leadUnit = leadUnit;
		this.units = units;
	}
	
	public void init()
	{
		super.init();
		panel.showStatus(leadUnit);
	}
	
	public void dispose()
	{
		super.dispose();
		panel.showStatus((Unit)null);
	}
	
	public void paintOverUnits(Graphics g)
	{
		for (Unit unit : units)
			drawSelectedUnitBox(g, unit);
	}
	
	public void onCommand(Command command)
	{
		if (command == Command.SELF_DESTRUCT)
		{
			for (Unit unit : units)
				Mediator.selfDestruct(unit);
			
			complete();
		}
		else if (command == Command.KILL)
		{
			for (Unit unit : units)
				Mediator.kill(unit);
			
			complete();
		}
		else if (command == Command.STOP)
		{
			for (Unit unit : units)
				unit.cancelAssignments();
		}
		else if (command == Command.TRANSFER)
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
	
	public void onLeftClick()
	{
		Mediator.doGroupMove(units, getCursorPosition());
		Mediator.playSound("beep2");
	}
}
