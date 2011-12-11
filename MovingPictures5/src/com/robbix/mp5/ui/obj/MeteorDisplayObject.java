package com.robbix.mp5.ui.obj;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Meteor;

public class MeteorDisplayObject extends DisplayObject
{
	private Meteor meteor;
	private Rectangle2D baseBounds;
	
	public MeteorDisplayObject(Meteor meteor)
	{
		this.meteor = meteor;
		this.baseBounds = new Rectangle2D.Double(
			meteor.getTargetPosition().x - 1,
			meteor.getTargetPosition().y - 1,
			3,
			3
		);
	}
	
	public DisplayLayer getDisplayLayer()
	{
		return DisplayLayer.AIR;
	}
	
	public boolean isAlive()
	{
		return meteor.isAlive();
	}
	
	public void paint(DisplayGraphics g)
	{
		if (!isAlive())
			return;
		
		SpriteLibrary lib = panel.getSpriteLibrary();
		int frame = Game.game.getFrame() - meteor.getFormationTime();
		Point2D point = meteor.getAbsPoint();
		
		if (meteor.isForming())
		{
			SpriteGroup group = lib.getAmbientSpriteGroup("aMeteor", "forming");
			g.draw(group.getFrame(frame), point);
		}
		else if (meteor.isCrashing())
		{
			SpriteGroup group  = lib.getAmbientSpriteGroup("aMeteor", "impact");
			Sprite sprite = group.getFrame(Game.game.getFrame() - meteor.getImpactTime());
			g.draw(sprite, point);
		}
		else if (meteor.isFlying())
		{
			SpriteGroup group  = lib.getAmbientSpriteGroup("aMeteor", "flying");
			Sprite sprite = group.getFrame(frame % group.getFrameCount());
			g.draw(sprite, point);
		}
	}
	
	public Rectangle2D getBounds()
	{
		double x = meteor.getTargetPosition().x - 0.5;
		double y = meteor.getTargetPosition().y + 0.5;
		double w = meteor.getAbsPoint().getX() - x;
		double h = y - meteor.getAbsPoint().getY();
		return baseBounds.createUnion(new Rectangle2D.Double(x, y, w, h));
	}
}
