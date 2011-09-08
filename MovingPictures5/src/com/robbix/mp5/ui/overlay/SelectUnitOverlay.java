package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
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
	private Rectangle dragArea;
	
	static Color TRANS_RED = new Color(255, 0, 0, 127);
	
	public void dispose()
	{
		getDisplay().showStatus((Unit)null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Select", w / 2 - 35, 30);
		g.translate(-rect.x, -rect.y);
		
		if (dragArea != null)
		{
			g.drawRect(dragArea.x, dragArea.y, dragArea.width, dragArea.height);
			g.setColor(TRANS_RED);
			g.fillRect(dragArea.x, dragArea.y, dragArea.width, dragArea.height);
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Unit selected = getDisplay().getMap().getUnitAbsolute(x, y);
		
		if (selected != null)
		{
			Mediator.sounds.play(selected.getType().getAcknowledgement());
			getDisplay().pushOverlay(new CommandUnitOverlay(selected));
			getDisplay().showStatus(selected);
			getDisplay().showStatus(selected.getOwner());
		}
	}
	
	public void onAreaDragCancelled()
	{
		dragArea = null;
	}
	
	public void onAreaDragging(int x, int y, int w, int h)
	{
		dragArea = new Rectangle(x, y, w, h);
	}
	
	public void onAreaDragged(int x, int y, int w, int h)
	{
		dragArea = null;
		Set<Unit> selected = getDisplay().getMap().getAllAbsolute(x, y, w, h);
		
		if (!selected.isEmpty())
		{
			Player focusedPlayer = getFocusedPlayer(selected);
			
			if (containsNonStructure(selected))
			{
				filterNonStructure(selected);
				Unit leadUnit = getLeadUnit(selected);
				Mediator.sounds.play(leadUnit.getType().getAcknowledgement());
				getDisplay().pushOverlay(new CommandGroupOverlay(selected));
				getDisplay().showStatus((Unit)null);
				getDisplay().showStatus(focusedPlayer);
			}
			else
			{
				Unit struct = getLeadUnit(selected);
				Mediator.sounds.play(struct.getType().getAcknowledgement());
				getDisplay().pushOverlay(new CommandUnitOverlay(struct));
				getDisplay().showStatus(struct);
				getDisplay().showStatus(struct.getOwner());
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
