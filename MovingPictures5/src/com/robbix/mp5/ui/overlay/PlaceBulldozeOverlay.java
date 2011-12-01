package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.utils.Position;

public class PlaceBulldozeOverlay extends InputOverlay
{
	public void paintImpl(DisplayGraphics g)
	{
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			g.setColor(panel.getMap().isBulldozed(pos) ? YELLOW.getFill() : GREEN.getFill());
			g.fill(pos);
			
			if (panel.getMap().isBulldozed(pos))
			{
				g.setColor(Color.WHITE);
				g.drawString("Already bulldozed", pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		bulldoze();
	}
	
	public void onLeftClickDrag()
	{
		bulldoze();
	}
	
	private void bulldoze()
	{
		Position pos = getCursorPosition();
		panel.getMap().bulldoze(pos);
		panel.refresh(pos);
	}
}
