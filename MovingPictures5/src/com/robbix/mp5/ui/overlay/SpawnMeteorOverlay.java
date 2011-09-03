package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;

public class SpawnMeteorOverlay extends InputOverlay
{
	private Position pos = null;
	
	public void init()
	{
		getDisplay().setAnimatedCursor("attack");
	}
	
	public void dispose()
	{
		getDisplay().setAnimatedCursor(null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		g.setColor(Color.RED);
		g.drawString("Left Click to Spawn", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
	}
	
	public void onLeftClick(int x, int y)
	{
		if (pos != null)
		{
			final DisplayPanel d = getDisplay();
			Mediator.doSpawnMeteor(pos);
			d.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		getDisplay().completeOverlay(this);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseEntered(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseExited(MouseEvent e)
	{
		pos = null;
	}
}
