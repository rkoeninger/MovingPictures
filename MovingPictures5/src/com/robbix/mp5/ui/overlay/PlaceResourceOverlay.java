package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.Sprite;

public class PlaceResourceOverlay extends InputOverlay
{
	private Sprite resSprite;
	
	private ResourceDeposit res;
	
	public PlaceResourceOverlay(ResourceDeposit res)
	{
		this.res = res;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawInstructions(g, rect, "Place", "Cancel");
		
		if (isCursorOnGrid())
		{
			if (resSprite == null)
			{
				resSprite = panel.getSpriteLibrary().getDefaultSprite(res);
				resSprite = Utils.getTranslucency(resSprite, -1, 0.5f);
			}
			
			panel.draw(g, resSprite, getCursorPosition());
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (panel.getMap().canPlaceResourceDeposit(pos))
		{
			panel.getMap().putResourceDeposit(res, pos);
			res = (ResourceDeposit)res.clone();
			panel.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
