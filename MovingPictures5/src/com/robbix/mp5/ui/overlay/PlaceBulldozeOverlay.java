package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintOverUnits(Graphics g)
	{
		if (isCursorOnGrid())
		{
			panel.draw(g, RED, getCursorPosition());
		}
	}
	
	public void onLeftClick()
	{
		panel.getMap().bulldoze(getCursorPosition());
		panel.refresh();
	}
}
