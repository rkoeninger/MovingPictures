package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Unit;

public class SelectUnitOverlay extends InputOverlay
{
	public void dispose()
	{
		panel.showStatus((Unit)null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Select", rect.width / 2 - 35, 30);
		g.translate(-rect.x, -rect.y);
		
		if (isDragging())
		{
			panel.draw(g, getDragArea());
			g.setColor(TRANS_RED);
			panel.fill(g, getDragArea());
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Unit selected = panel.getMap().getUnitAbsolute(x, y);
		
		if (selected != null)
		{
			Mediator.sounds.play(selected.getType().getAcknowledgement());
			panel.pushOverlay(new CommandUnitOverlay(selected));
			panel.showStatus(selected);
			panel.showStatus(selected.getOwner());
		}
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		Set<Unit> selected = panel.getMap().getAllAbsolute(x, y, w, h);
		
		if (!selected.isEmpty())
		{
			Player focusedPlayer = getFocusedPlayer(selected);
			
			if (containsNonStructure(selected))
			{
				filterNonStructure(selected);
				Unit leadUnit = getLeadUnit(selected);
				Mediator.sounds.play(leadUnit.getType().getAcknowledgement());
				panel.pushOverlay(new CommandGroupOverlay(selected));
				panel.showStatus((Unit)null);
				panel.showStatus(focusedPlayer);
			}
			else
			{
				Unit struct = getLeadUnit(selected);
				Mediator.sounds.play(struct.getType().getAcknowledgement());
				panel.pushOverlay(new CommandUnitOverlay(struct));
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
		
		while (unitIterator.hasNext())
		{
			Unit unit = unitIterator.next();
			
			if (unit.isStructure() || unit.getType().isGuardPostType())
			{
				unitIterator.remove();
			}
		}
	}
	
	private static boolean containsNonStructure(Set<Unit> units)
	{
		for (Unit unit : units)
		{
			if (!unit.isStructure() && !unit.getType().isGuardPostType())
			{
				return true;
			}
		}
		
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
}
