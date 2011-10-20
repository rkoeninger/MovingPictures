package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.basics.Position;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawInstructions(g, rect, "Bulldoze", "Cancel");
		
		if (isCursorOnGrid())
		{
			g.setColor(Color.RED);
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
