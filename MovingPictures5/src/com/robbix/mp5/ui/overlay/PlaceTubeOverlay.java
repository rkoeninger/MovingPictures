package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;

public class PlaceTubeOverlay extends InputOverlay
{
	private Sprite tubeSprite;
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			if (tubeSprite == null)
			{
				tubeSprite = panel.getSpriteLibrary().getSprite("oTerrainFixture", "tube");
				tubeSprite = Utils.getTranslucency(tubeSprite, -1, 0.8f);
			}
			
			panel.draw(g, tubeSprite, getCursorPosition());
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		if (panel.getMap().canPlaceFixture(pos))
		{
			panel.getMap().putTube(pos);
			panel.refresh();
		}
	}
	
	public void onRightClick()
	{
		complete();
	}
}
