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
	
	public MeteorDisplayObject(Meteor meteor)
	{
		this.meteor = meteor;
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
		SpriteLibrary lib = panel.getSpriteLibrary();
		int frame = Game.game.getFrame() - meteor.getFormationTime();
		Point2D point = meteor.getAbsPoint();
		
		if (meteor.isForming())
		{
			SpriteGroup forming = lib.getAmbientSpriteGroup("aMeteor", "forming");
			g.draw(forming.getFrame(frame), point);
		}
		else if (meteor.isCrashing())
		{
			SpriteGroup impact  = lib.getAmbientSpriteGroup("aMeteor", "impact");
			g.draw(impact.getFrame(Game.game.getFrame() - meteor.getImpactTime()), point);
		}
		else if (meteor.isFlying())
		{
			SpriteGroup flying  = lib.getAmbientSpriteGroup("aMeteor", "flying");
			Sprite sprite = flying.getFrame(frame % flying.getFrameCount());
			g.draw(sprite, point);
		}
	}
	
	public Rectangle2D getBounds()
	{
		double x = meteor.getTargetPosition().x - 0.5;
		double y = meteor.getTargetPosition().y + 0.5;
		double w = meteor.getAbsPoint().getX() - x;
		double h = meteor.getAbsPoint().getY() - y;
		return new Rectangle2D.Double(x, y, w, h);
	}
}
