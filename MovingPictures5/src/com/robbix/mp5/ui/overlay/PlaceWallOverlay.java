package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;

public class PlaceWallOverlay extends InputOverlay
{
	private Sprite wallSprite;
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			if (wallSprite == null)
			{
				wallSprite = panel.getSpriteLibrary().getSprite("oTerrainFixture", "wall");
				wallSprite = Utils.getTranslucency(wallSprite, -1, 0.8f);
			}
			
			panel.draw(g, wallSprite, getCursorPosition());
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		if (panel.getMap().canPlaceFixture(pos))
		{
			panel.getMap().putWall(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick()
	{
		complete();
	}
}
