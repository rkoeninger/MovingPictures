package com.robbix.mp5.ui.overlay;

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
		if (isCursorOnGrid())
		{
			Position pos = getCursorPosition();
			
			if (geyserSprite == null)
			{
				geyserSprite = panel.getSpriteLibrary().getSprite("aGeyser", "geyser");
				geyserSprite = Utils.getTranslucency(geyserSprite, -1, 0.5f);
			}
			
			panel.draw(g, geyserSprite, pos);
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		if (panel.getMap().canPlaceFixture(pos))
		{
			panel.getMap().putGeyser(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick()
	{
		complete();
	}
}
