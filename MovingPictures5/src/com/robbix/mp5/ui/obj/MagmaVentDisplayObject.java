package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;

import com.robbix.mp5.map.Fixture;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.utils.Position;
import com.robbix.utils.Utils;

public class MagmaVentDisplayObject extends DisplayObject
{
	private Position pos;
	
	public MagmaVentDisplayObject(Position pos)
	{
		this.pos = pos;
	}
	
	public boolean isAlive()
	{
		return panel.getMap().hasMagmaVent(pos);
	}
	
	public Rectangle2D getBounds()
	{
		return new Rectangle2D.Double(pos.x, pos.y, 1, 1);
	}
	
	public void paint(DisplayGraphics g)
	{
		SpriteGroup group = panel.getSpriteLibrary().getSpriteGroup(Fixture.MAGMA);
		
		if (group != SpriteSet.BLANK_GROUP)
		{
			Sprite sprite = group.getFrame(Utils.getTimeBasedIndex(80, group.getFrameCount()));
			g.draw(sprite, pos);
		}
	}
}
