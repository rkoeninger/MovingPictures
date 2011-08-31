package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;

public class PlaceUnitOverlay extends InputOverlay
{
	private BufferedImage hoverSprite;
	private int offX, offY;

	private BufferedImage turretHoverSprite;
	private int turretOffX, turretOffY;
	
	private Unit unit;
	
	private Position pos = null;
	
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
	
	public void paintOverUnits(Graphics g)
	{
		final int w = getDisplay().getWidth();
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Place", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		
		if (pos == null) return;
		
		if (hoverSprite == null)
		{
			Sprite sprite = getDisplay().getSpriteLibrary().getSprite(unit);
			
			BufferedImage scoutSprite =
				(BufferedImage) sprite.getImage(unit.getOwner().getColorHue());
			offX = sprite.getXOffset();
			offY = sprite.getYOffset();
			
			hoverSprite = Utils.getTranslucency(scoutSprite, 0.5f);
		}
		
		if (turretHoverSprite == null && unit.hasTurret())
		{
			Sprite sprite =
				getDisplay().getSpriteLibrary().getSprite(unit.getTurret());
			
			BufferedImage scoutSprite =
				(BufferedImage) sprite.getImage(unit.getOwner().getColorHue());
			turretOffX = sprite.getXOffset();
			turretOffY = sprite.getYOffset();
			
			turretHoverSprite = Utils.getTranslucency(scoutSprite, 0.5f);
		}
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		g.drawImage(
			hoverSprite,
			pos.x * tileSize + offX,
			pos.y * tileSize + offY,
			null
		);

		if (unit.hasTurret())
		{
			g.drawImage(
				turretHoverSprite,
				pos.x * tileSize + turretOffX,
				pos.y * tileSize + turretOffY,
				null
			);
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		final DisplayPanel d = getDisplay();
		
		if (d.getMap().canPlaceUnit(pos, unit.getFootprint()))
		{
			if (unit.isMine())
			{
				ResourceDeposit res =
					d.getMap().getResourceDeposit(pos.shift(1, 0));
				
				if (res == null || res.getType() != ResourceType.COMMON_ORE)
					return;
			}
			
			d.getMap().putUnit(unit, pos);
			d.refresh();
			
			if (factory != null)
			{
				unit = factory.newUnit(type);
			}
			else
			{
				d.completeOverlay(this);
			}
		}
	}
	
	public void onRightClick(int x, int y)
	{
		getDisplay().completeOverlay(this);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseEntered(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseExited(MouseEvent e)
	{
		pos = null;
	}
}
