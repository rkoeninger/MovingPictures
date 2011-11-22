package com.robbix.mp5.ui.overlay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.robbix.mp5.Game;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.RIterator;

public class SelectUnitOverlay extends InputOverlay
{
	public SelectUnitOverlay()
	{
		super.closesOnEscape     = false;
		super.closesOnRightClick = false;
	}
	
	public void dispose()
	{
		panel.showStatus((Unit)null);
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (isDragging())
		{
			g.setColor(WHITE.getFill());
			g.fillRegion(getDragRegion());
		}
	}
	
	public void onLeftClick()
	{
		Unit selected = panel.getMap().getUnit(getCursorPosition());
		
		if (selected != null)
		{
			Game.game.playSound(selected.getType().getAcknowledgement());
			push(selected.isTruck()
				? new CommandTruckOverlay(selected)
				: new CommandUnitOverlay(selected));
			panel.showStatus(selected);
			panel.showStatus(selected.getOwner());
		}
	}
	
	public void onAreaDragged()
	{
		Set<Unit> selected = panel.getMap().getUnits(getDragRegion());
		
		if (!selected.isEmpty())
		{
			Player focusedPlayer = getFocusedPlayer(selected);
			
			if (containsNonStructure(selected))
			{
				filterNonStructure(selected);
				
				if (selected.size() == 1)
				{
					Unit only = (Unit) selected.toArray()[0];
					Game.game.playSound(only.getType().getAcknowledgement());
					push(new CommandUnitOverlay(only));
					panel.showStatus(only);
					panel.showStatus(only.getOwner());
				}
				else
				{
					Unit leadUnit = getLeadUnit(selected);
					Game.game.playSound(leadUnit.getType().getAcknowledgement());
					push(areAllTrucks(selected)
						? new CommandTruckOverlay(selected.toArray(new Unit[0]))
						: new CommandGroupOverlay(leadUnit, selected)
					);
					panel.showStatus(leadUnit);
					panel.showStatus(focusedPlayer);
				}
			}
			else
			{
				Unit struct = getLeadUnit(selected);
				Game.game.playSound(struct.getType().getAcknowledgement());
				push(new CommandUnitOverlay(struct));
				panel.showStatus(struct);
				panel.showStatus(struct.getOwner());
			}
		}
	}
	
	private static Unit getLeadUnit(Set<Unit> units)
	{
		return units.iterator().next();
	}
	
	private static void filterNonStructure(Set<Unit> units)
	{
		Iterator<Unit> unitIterator = units.iterator();
		
		for (Unit unit : RIterator.wrap(unitIterator))
			if (unit.isStructure() || unit.getType().isGuardPostType())
				unitIterator.remove();
	}
	
	private static boolean containsNonStructure(Set<Unit> units)
	{
		for (Unit unit : units)
			if (!unit.isStructure() && !unit.getType().isGuardPostType())
				return true;
		
		return false;
	}
	
	private static Player getFocusedPlayer(Set<Unit> units)
	{
		Map<Player, Integer> ownerCounts = new HashMap<Player, Integer>();
		
		for (Unit unit : units)
		{
			Player owner = unit.getOwner();
			Integer ownerCount = ownerCounts.get(owner);
			ownerCounts.put(owner, ownerCount == null ? 1 : ownerCount + 1);
		}
		
		Map.Entry<Player, Integer> greatestCount = null;
		
		for (Map.Entry<Player, Integer> ownerCount : ownerCounts.entrySet())
		{
			if (greatestCount == null
			 || ownerCount.getValue() > greatestCount.getValue())
			{
				greatestCount = ownerCount;
			}
		}
		
		Player focusedPlayer = greatestCount.getKey();
		Iterator<Unit> unitIterator = units.iterator();
		
		while (unitIterator.hasNext())
		{
			Unit unit = unitIterator.next();
			
			if (!unit.getOwner().equals(focusedPlayer))
			{
				unitIterator.remove();
			}
		}
		
		return focusedPlayer;
	}
	
	private static boolean areAllTrucks(Set<Unit> units)
	{
		for (Unit unit : units)
			if (!unit.isTruck())
				return false;
		
		return true;
	}
}
