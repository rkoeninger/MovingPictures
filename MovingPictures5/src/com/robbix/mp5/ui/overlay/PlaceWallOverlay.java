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
		drawInstructions(g, rect, "Place Wall", "Cancel");
		
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
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (panel.getMap().canPlaceFixture(pos))
		{
			panel.getMap().putWall(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
