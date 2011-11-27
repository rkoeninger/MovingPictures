package com.robbix.mp5.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;
import com.robbix.utils.Utils;

public class StatusLightDisplayObject extends DisplayObject
{
	private Unit unit;
	
	public StatusLightDisplayObject(Unit unit)
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
			2,
			2
		);
	}
	
	public void paint(DisplayGraphics g)
	{
		if (unit.isGuardPost() || unit.isStructure())
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
}
