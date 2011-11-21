package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.RGraphics;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintImpl(RGraphics g)
	{
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			g.setColor(panel.getMap().isBulldozed(pos) ? YELLOW.getFill() : GREEN.getFill());
			g.fillPosition(pos);
			
			if (panel.getMap().isBulldozed(pos))
			{
				g.setColor(Color.WHITE);
				g.drawString("Already bulldozed", pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		panel.getMap().bulldoze(getCursorPosition());
		panel.refresh();
	}
}
