package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.ColorScheme;
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
	
	public void paintOverUnits(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (resSprite == null)
			{
				resSprite = panel.getSpriteLibrary().getDefaultSprite(res);
				resSprite = Utils.getTranslucency(resSprite, -1, 0.5f);
			}
			
			Position pos = getCursorPosition();
			
			ColorScheme colors = null;
			
			if (panel.getMap().canPlaceResourceDeposit(pos))
			{
				if (hasNeighboringDeposit(pos))
				{
					colors = YELLOW;
				}
				else
				{
					colors = GREEN;
				}
			}
			else
			{
				colors = RED;
			}
			
			panel.draw(g, colors, pos);
			panel.draw(g, resSprite, pos);
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		if (panel.getMap().canPlaceResourceDeposit(pos))
		{
			panel.getMap().putResourceDeposit(res, pos);
			res = (ResourceDeposit)res.clone();
			panel.refresh();
		}
	}
	
	private boolean hasNeighboringDeposit(Position pos)
	{
		for (Position npos : pos.get8Neighbors())
			if (panel.getMap().getBounds().contains(npos)
			 && panel.getMap().hasResourceDeposit(npos))
				return true;
		
		return false;
	}
}
