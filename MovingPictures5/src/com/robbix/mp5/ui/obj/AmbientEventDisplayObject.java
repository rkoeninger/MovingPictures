package com.robbix.mp5.ui.obj;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.event.AmbientEvent;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;

public class AmbientEventDisplayObject extends DisplayObject
{
	private AmbientEvent event;
	private int frame = 0;
	private int frameCount = -1;
	private Rectangle2D bounds;
	
	public AmbientEventDisplayObject(AmbientEvent event)
	{
		this.event = event;
		Point2D point = event.getPosition();
		this.bounds = new Rectangle2D.Double(point.getX() - 1, point.getY() - 1, 2, 2);
	}
	
	public boolean isAlive()
	{
		return frameCount < 0 || frame < frameCount;
	}
	
	public DisplayLayer getDisplayLayer()
	{
		return DisplayLayer.OVER_UNIT;
	}
	
	public void paint(DisplayGraphics g)
	{
		SpriteLibrary lib = panel.getSpriteLibrary();
		SpriteSet set = lib.getAmbientSpriteSet(event.getType().getName());
		
		if (set == SpriteSet.BLANK)
			return;
		
		SpriteGroup group = set.get(event.getArg());
		int frame = Game.game.getFrame() - event.getTime();
		Sprite sprite = group.getFrame(frame);
		g.draw(sprite, event.getPosition());
	}
	
	public Rectangle2D getBounds()
	{
		return bounds;
	}
}
