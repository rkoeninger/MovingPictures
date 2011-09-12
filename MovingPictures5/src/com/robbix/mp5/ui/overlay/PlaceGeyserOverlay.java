package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;

public class PlaceGeyserOverlay extends InputOverlay
{
	private Sprite geyserSprite;
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Place", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);

		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (geyserSprite == null)
			{
				geyserSprite = panel.getSpriteLibrary()
									.getSprite("aGeyser/geyser");
				geyserSprite = Utils.getTranslucency(geyserSprite, -1, 0.5f);
			}
			
			panel.draw(g, geyserSprite, pos);
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (panel.getMap().canPlaceFixture(pos))
		{
			panel.getMap().putGeyser(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
}
