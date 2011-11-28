package com.robbix.mp5.ui.obj;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.Utils;

public class UnitDisplayObject extends DisplayObject
{
	private Unit unit;
	
	public UnitDisplayObject(Unit unit)
	{
		this.unit = unit;
	}
	
	public boolean isAlive()
	{
		return !unit.isDead() && !unit.isFloating();
	}
	
	public Rectangle2D getBounds()
	{
		Point2D absPoint = unit.getAbsPoint();
		return new Rectangle2D.Double(
			absPoint.getX() - 0.5,
			absPoint.getY() - 0.5,
			unit.getWidth() + 1,
			unit.getHeight() + 1
		);
	}
	
	public void paint(DisplayGraphics g)
	{
		drawUnit(g, unit);
	}
	
	private void drawUnit(DisplayGraphics g, Unit unit)
	{
		if (!unit.isTurret() && panel.getScale() < panel.getMinimumShowUnitScale())
		{
			g.setColor(unit.getOwner().getColor());
			g.fill(unit.getOccupiedBounds());
			return;
		}
		
		Point2D point = unit.getAbsPoint();
		Sprite sprite = panel.getSpriteLibrary().getSprite(unit);
		
		if (panel.isShowingShadows() && !unit.isTurret())
		{
			Point2D shadowOffset = panel.getShadowOffset();
			double px = point.getX();
			double py = point.getY();
			double sx = shadowOffset.getX();
			double sy = shadowOffset.getY();
			double scaleFactor = 32.0 * Math.pow(2, panel.getScale());
			Point2D shadowPoint = new Point2D.Double(
				px + sx + (sprite.getXOffset(panel.getScale()) / scaleFactor),
				py + sy + (sprite.getYOffset(panel.getScale()) / scaleFactor)
			);
			g.drawImage(sprite.getShadow(), shadowPoint);
			
			if (unit.hasTurret())
			{
				Sprite turretSprite = panel.getSpriteLibrary().getSprite(unit.getTurret());
				
				if (turretSprite != null && turretSprite != SpriteSet.BLANK_SPRITE)
				{
					shadowPoint = new Point2D.Double(
						px + sx + (turretSprite.getXOffset(panel.getScale()) / scaleFactor),
						py + sy + (turretSprite.getYOffset(panel.getScale()) / scaleFactor)
					);
					g.drawImage(turretSprite.getShadow(), shadowPoint);
				}
			}
		}
		
		if (!unit.isTurret() && sprite == SpriteSet.BLANK_SPRITE)
		{
			g.setColor(unit.getOwner().getColor());
			g.fill(unit.getOccupiedBounds());
		}
		else
		{
			RColor color = unit.getOwner() != null ? unit.getOwner().getColor() : null;
			g.draw(sprite, point, color);
		}
		
		if (unit.hasTurret())
		{
			drawUnit(g, unit.getTurret());
		}
		else if (unit.isGuardPost() || unit.isStructure())
		{
			drawStatusLight(g, unit);
		}
	}
	
	private void drawStatusLight(DisplayGraphics g, Unit unit)
	{
		if (!panel.getCurrentPlayer().owns(unit))
			return;
		
		Position pos = unit.getPosition();
		
		if (unit.isIdle())
		{
			g.draw(panel.getSpriteLibrary().getSprite("aStructureStatus", "idle"), pos);
		}
		else if (unit.isDisabled())
		{
			SpriteGroup seq = panel.getSpriteLibrary().getAmbientSpriteGroup("aStructureStatus", "disabled");
			int index = Utils.getTimeBasedIndex(100, seq.getSpriteCount());
			g.draw(seq.getSprite(index), pos);
		}
		else if (unit.isStructure())
		{
			g.draw(panel.getSpriteLibrary().getSprite("aStructureStatus", "active"), pos);
		}
	}
}
