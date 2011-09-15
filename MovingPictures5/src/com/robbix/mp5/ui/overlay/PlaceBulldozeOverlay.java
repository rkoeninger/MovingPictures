package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.basics.Position;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Bulldoze", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (isCursorOnGrid())
		{
			panel.draw(g, getCursorPosition());
			g.setColor(TRANS_RED);
			panel.fill(g, getCursorPosition());
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (isCursorOnGrid())
		{
			panel.getMap().bulldoze(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
