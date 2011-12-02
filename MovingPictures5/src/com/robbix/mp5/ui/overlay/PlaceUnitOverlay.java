package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.Region;

public class PlaceUnitOverlay extends InputOverlay
{
	private Sprite unitSprite;
	private Sprite turretSprite;
	private RColor color;
	
	private Unit unit;
	
	private UnitFactory factory;
	private String type;
	private Player player;
	
	public PlaceUnitOverlay(UnitFactory factory, UnitType type, Player player)
	{
		this.factory = factory;
		this.type = type.getName();
		this.player = player;
		this.color = player.getColor();
		this.unit = factory.newUnit(this.type, player);
		this.showTubeConnectivity = unit.needsConnection() || unit.isConnectionSource();
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (isCursorOnGrid())
		{
			if (unitSprite == null || unitSprite == SpriteSet.BLANK_SPRITE)
				unitSprite = panel.getSpriteLibrary().getTranslucentDefault(unit, 0.5);
			
			if ((turretSprite == null || turretSprite == SpriteSet.BLANK_SPRITE) && unit.hasTurret())
				turretSprite = panel.getSpriteLibrary().getTranslucentDefault(unit.getTurret(), 0.5);
			
			Position center = unit.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, unit.getType(), pos);
			g.draw(unitSprite, pos, color);
			
			if (unit.hasTurret())
				g.draw(turretSprite, pos, color);
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = unit.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) g.drawString(toolTip, pos);
				                             else g.drawString(toolTip, inner.move(pos));
			}
		}
	}
	
	public void onLeftClick()
	{
		attemptPlacement(true);
	}
	
	public void onLeftClickDrag()
	{
		attemptPlacement(false);
	}
	
	private void attemptPlacement(boolean playErrorSound)
	{
		Position center = unit.getFootprint().getCenter();
		Position pos = getCursorPosition().subtract(center);
		LayeredMap map = panel.getMap();
		
		if (map.canPlaceUnit(pos, unit.getFootprint()))
		{
			if (unit.isMine())
			{
				ResourceDeposit res = map.getResourceDeposit(pos.shift(1, 0));
				
				if (res == null || (res.isRare() != unit.is("Rare")))
				{
					if (playErrorSound)
						Game.game.playSound("structureError");
					
					return;
				}
			}
			
			map.putUnit(unit, pos);
			panel.refresh();
			
			if (factory != null)
			{
				unit = factory.newUnit(type, player);
			}
			else
			{
				complete();
			}
		}
		else
		{
			if (playErrorSound)
				Game.game.playSound("structureError");
		}
	}
}
