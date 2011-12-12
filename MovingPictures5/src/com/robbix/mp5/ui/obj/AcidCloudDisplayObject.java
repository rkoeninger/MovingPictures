package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.AcidCloud;
import com.robbix.utils.Position;

public class AcidCloudDisplayObject extends DisplayObject
{
	private AcidCloud cloud;
	private Rectangle2D bounds;
	
	public AcidCloudDisplayObject(AcidCloud cloud)
	{
		this.cloud = cloud;
		Position pos = cloud.getPosition();
		this.bounds = new Rectangle2D.Double(pos.x - 2, pos.y - 2, 5, 5);
	}
	
	public DisplayLayer getDisplayLayer()
	{
		return DisplayLayer.OVER_UNIT;
	}
	
	public boolean isAlive()
	{
		return cloud.isAlive();
	}
	
	public void paint(DisplayGraphics g)
	{
		if (!isAlive())
			return;
		
		SpriteLibrary lib = panel.getSpriteLibrary();
		SpriteSet acidSprites = lib.getAmbientSpriteSet("aAcidCloud");
		int frame = Game.game.getFrame() - cloud.getStartTime();
		
		if (cloud.isForming())
		{
			SpriteGroup group = acidSprites.get("cloud1");
			g.draw(group.getFrame(frame), cloud.getAbsPoint());
		}
		else if (cloud.isBurning())
		{
			SpriteGroup group = acidSprites.get("cloud2");
			g.draw(group.getFrame(frame % group.getFrameCount()), cloud.getAbsPoint());
			
		}
		else if (cloud.isFading())
		{
			SpriteGroup group = acidSprites.get("cloud3");
			frame -= cloud.getFormingDuration();
			frame -= cloud.getBurningDuration();
			g.draw(group.getFrame(frame), cloud.getAbsPoint());
		}
	}
	
	public Rectangle2D getBounds()
	{
		return bounds;
	}
}
