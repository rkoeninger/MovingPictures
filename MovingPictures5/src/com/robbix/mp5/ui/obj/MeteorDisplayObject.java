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
		int frameCount = 0;
		double distance = 0;
		double angle = 0;
		Point2D point = meteor.getAbsPoint();
		
		if (frame < 5)
		{
			SpriteGroup forming = lib.getAmbientSpriteGroup("aMeteor", "forming");
			double progress = (frameCount - frame) / (double)(frameCount);
			double x = point.getX() + distance *  Math.cos(angle) * progress;
			double y = point.getY() + distance * -Math.sin(angle) * progress;
			g.draw(forming.getFrame(frame), new Point2D.Double(x, y));
		}
		else if (frame > frameCount - 9)
		{
			SpriteGroup impact  = lib.getAmbientSpriteGroup("aMeteor", "impact");
			int spriteFrame = impact.getFrameCount() - (frameCount - frame + 1);
			g.draw(impact.getFrame(spriteFrame), point);
		}
		else
		{
			SpriteGroup flying  = lib.getAmbientSpriteGroup("aMeteor", "flying");
			Sprite sprite = flying.getFrame(frame % flying.getFrameCount());
			double progress = (frameCount - frame) / (double)(frameCount);
			double x = point.getX() + distance *  Math.cos(angle) * progress;
			double y = point.getY() + distance * -Math.sin(angle) * progress;
			g.draw(sprite, new Point2D.Double(x, y));
		}
	}
	
	public Rectangle2D getBounds()
	{
		return null;
	}
}
