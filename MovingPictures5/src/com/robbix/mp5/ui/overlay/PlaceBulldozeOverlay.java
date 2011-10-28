package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			panel.draw(g, RED, getCursorPosition());
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		if (isCursorOnGrid())
		{
			panel.getMap().bulldoze(getCursorPosition());
			panel.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
