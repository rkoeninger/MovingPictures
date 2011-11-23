package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.ColorScheme;
import com.robbix.utils.Position;

public class PlaceResourceOverlay extends InputOverlay
{
	private Sprite resSprite;
	
	private ResourceDeposit res;
	
	public PlaceResourceOverlay(ResourceDeposit res)
	{
		this.res = res;
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (resSprite == null || resSprite == SpriteSet.BLANK_SPRITE)
			resSprite = panel.getSpriteLibrary().getTranslucentDefault(res, 0.5f);
		
		Position pos = getCursorPosition();
		
		ColorScheme colors = null;
		String toolTip = null;
		
		if (panel.getMap().canPlaceResourceDeposit(pos))
		{
			Unit occupant = panel.getMap().getUnit(pos);
			colors = hasNeighboringDeposit(pos) || isImmobile(occupant) ? YELLOW : GREEN;
			
			if      (hasNeighboringDeposit(pos)) toolTip = "Too close";
			else if (isImmobile(occupant))       toolTip = "Occupied";
		}
		else
		{
			colors = RED;
			toolTip = "Occupied";
		}
		
		g.setColor(colors.getFill());
		g.fill(pos);
		g.draw(resSprite, pos);
		
		if (toolTip != null)
		{
			g.setColor(Color.WHITE);
			g.drawString(toolTip, pos);
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
	
	private boolean isImmobile(Unit unit)
	{
		return unit != null && (unit.isStructure() || unit.isGuardPost());
	}
	
	private boolean hasNeighboringDeposit(Position pos)
	{
		LayeredMap map = panel.getMap();
		Position wPos = pos.shift(-1, 0);
		Position ePos = pos.shift(1, 0);
		
		return map.hasResourceDeposit(wPos) || map.hasResourceDeposit(ePos);
	}
}
