package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.Region;
import com.robbix.mp5.utils.Utils;

public class PlaceUnitOverlay extends InputOverlay
{
	private Sprite unitSprite;
	private Sprite turretSprite;
	
	private Unit unit;
	
	private UnitFactory factory;
	private String type;
	
	public PlaceUnitOverlay(Unit unit)
	{
		this.unit = unit;
		this.showTubeConnectivity = unit.needsConnection() || unit.isConnectionSource();
	}
	
	public PlaceUnitOverlay(UnitFactory factory, String type)
	{
		this.factory = factory;
		this.type = type;
		this.unit = factory.newUnit(type);
		this.showTubeConnectivity = unit.needsConnection() || unit.isConnectionSource();
	}
	
	public PlaceUnitOverlay(UnitFactory factory, UnitType type)
	{
		this.factory = factory;
		this.type = type.getName();
		this.unit = factory.newUnit(this.type);
		this.showTubeConnectivity = unit.needsConnection() || unit.isConnectionSource();
	}
	
	public void paintImpl(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (unitSprite == null)
			{
				int hue = unit.getOwner().getColorHue();
				unitSprite = panel.getSpriteLibrary().getDefaultSprite(unit);
				unitSprite = (unitSprite == SpriteSet.BLANK_SPRITE)
					? null
					: Utils.getTranslucency(unitSprite, hue, 0.5f);
			}
			
			if (turretSprite == null && unit.hasTurret())
			{
				Unit turret = unit.getTurret();
				int hue = unit.getOwner().getColorHue();
				turretSprite = panel.getSpriteLibrary().getDefaultSprite(turret);
				turretSprite = (turretSprite == SpriteSet.BLANK_SPRITE)
					? null
					: Utils.getTranslucency(turretSprite, hue, 0.5f);
			}
			
			Position center = unit.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, unit.getType(), pos);
			
			if (unitSprite != null)
				panel.draw(g, unitSprite, pos);
			
			if (unit.hasTurret())
			{
				if (turretSprite != null)
					panel.draw(g, turretSprite, pos);
			}
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = unit.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) panel.draw(g, toolTip, pos);
				                             else panel.draw(g, toolTip, inner.move(pos));
			}
		}
	}
	
	public void onLeftClick()
	{
		Position center = unit.getFootprint().getCenter();
		Position pos = getCursorPosition().subtract(center);
		LayeredMap map = panel.getMap();
		
		if (map.canPlaceUnit(pos, unit.getFootprint()))
		{
			if (unit.isMine())
			{
				ResourceDeposit res = map.getResourceDeposit(pos.shift(1, 0));
				
				if (res == null || res.getType() != ResourceType.COMMON_ORE)
					return;
			}
			
			map.putUnit(unit, pos);
			panel.refresh();
			
			if (factory != null)
			{
				unit = factory.newUnit(type);
			}
			else
			{
				complete();
			}
		}
	}
}
