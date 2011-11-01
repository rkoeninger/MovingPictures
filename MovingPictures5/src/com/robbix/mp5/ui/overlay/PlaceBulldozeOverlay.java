package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.basics.Position;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintOverUnits(Graphics g)
	{
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			panel.draw(g, panel.getMap().isBulldozed(pos) ? YELLOW : GREEN, pos);
			
			if (panel.getMap().isBulldozed(pos))
			{
				g.setColor(Color.WHITE);
				panel.draw(g, "Already bulldozed", pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		panel.getMap().bulldoze(getCursorPosition());
		panel.refresh();
	}
}
