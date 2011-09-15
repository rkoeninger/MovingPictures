package com.robbix.mp5.ui.overlay;

import java.awt.Color;
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
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Place", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
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
