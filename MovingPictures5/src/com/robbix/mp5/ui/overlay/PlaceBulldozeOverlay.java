package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;

public class PlaceBulldozeOverlay extends InputOverlay
{
	private Position pos = null;
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Place", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (pos == null) return;
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		g.drawRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
		g.setColor(Utils.getTranslucency(Color.RED, 0.5f));
		g.fillRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
	}
	
	public void onLeftClick(int x, int y)
	{
		final DisplayPanel d = getDisplay();
		d.getMap().clearFixture(pos);
		d.getMap().bulldoze(pos);
		d.refresh();
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
