package com.robbix.mp5.ui.overlay;

import java.awt.Color;
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
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Place Wall", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
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
