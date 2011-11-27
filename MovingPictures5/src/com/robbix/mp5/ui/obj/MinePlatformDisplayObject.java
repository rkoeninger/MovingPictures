package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;

public class MinePlatformDisplayObject extends DisplayObject
{
	private Unit mine;
	
	public MinePlatformDisplayObject(Unit mine)
	{
		if (!mine.isMine())
			throw new IllegalArgumentException();
		
		this.mine = mine;
	}
	
	public boolean isAlive()
	{
		return !mine.isFloating() && !mine.isDead();
	}
	
	public Rectangle2D getBounds()
	{
		Position pos = mine.getPosition();
		return new Rectangle2D.Double(pos.x, pos.y, 1, 1);
	}
	
	public void paint(DisplayGraphics g)
	{
		Sprite sprite = panel.getSpriteLibrary().getSprite("aMine", "platform");
		
		if (sprite != null && sprite != SpriteSet.BLANK_SPRITE)
		{
			g.draw(sprite, mine.getPosition());
		}
	}
}
