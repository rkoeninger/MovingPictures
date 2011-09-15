package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;

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
	}
	
	public PlaceUnitOverlay(UnitFactory factory, String type)
	{
		this.factory = factory;
		this.type = type;
		this.unit = factory.newUnit(type);
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
			if (unitSprite == null)
			{
				int hue = unit.getOwner().getColorHue();
				unitSprite = panel.getSpriteLibrary().getSprite(unit);
				unitSprite = Utils.getTranslucency(unitSprite, hue, 0.5f);
			}
			
			if (turretSprite == null && unit.hasTurret())
			{
				Unit turret = unit.getTurret();
				int hue = unit.getOwner().getColorHue();
				turretSprite = panel.getSpriteLibrary().getSprite(turret);
				turretSprite = Utils.getTranslucency(turretSprite, hue, 0.5f);
			}
			
			Position pos = getCursorPosition();
			panel.draw(g, unitSprite, pos);
			
			if (turretSprite != null)
			{
				panel.draw(g, turretSprite, pos);
			}
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (panel.getMap().canPlaceUnit(pos, unit.getFootprint()))
		{
			if (unit.isMine())
			{
				ResourceDeposit res =
					panel.getMap().getResourceDeposit(pos.shift(1, 0));
				
				if (res == null || res.getType() != ResourceType.COMMON_ORE)
					return;
			}
			
			panel.getMap().putUnit(unit, pos);
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
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
