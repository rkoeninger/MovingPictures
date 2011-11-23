package com.robbix.mp5.ui.ani;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.utils.Position;

public class MeteorAnimation extends AmbientAnimation
{
	private int frame = 0;
	private int frameCount = 80;
	
	private Position target;
	private Point2D point;
	private Rectangle2D bounds;
	
	private double distance = 600 / 32;
	private double angle = 35 * Math.PI / 180; // in rads
	
	private boolean impactSoundPlayed = false;
	
	public MeteorAnimation(SpriteLibrary lib, Position target)
	{
		super(lib);
		
		this.target = target;
		
		point = new Point2D.Double(
			target.x + 0.5,
			target.y + 0.5
		);
		
		bounds = new Rectangle2D.Double(
			target.x - 0.5,
			target.y + 0.5,
			(int) (distance * Math.cos(angle)),
			(int) (distance * Math.sin(angle))
		);
	}
	
	public Rectangle2D getBounds()
	{
		return bounds;
	}
	
	public void paint(DisplayGraphics g)
	{
		SpriteGroup forming = lib.getAmbientSpriteGroup("aMeteor", "forming");
		SpriteGroup flying  = lib.getAmbientSpriteGroup("aMeteor", "flying");
		SpriteGroup impact  = lib.getAmbientSpriteGroup("aMeteor", "impact");
		
		if (frame < 5)
		{
			double progress = (frameCount - frame) / (double)(frameCount);
			double x = point.getX() + distance *  Math.cos(angle) * progress;
			double y = point.getY() + distance * -Math.sin(angle) * progress;
			g.draw(forming.getFrame(frame), new Point2D.Double(x, y));
		}
		else if (frame > frameCount - 9)
		{
			int spriteFrame = impact.getFrameCount() - (frameCount - frame + 1);
			g.draw(impact.getFrame(spriteFrame), point);
		}
		else
		{
			Sprite sprite = flying.getFrame(frame % flying.getFrameCount());
			double progress = (frameCount - frame) / (double)(frameCount);
			double x = point.getX() + distance *  Math.cos(angle) * progress;
			double y = point.getY() + distance * -Math.sin(angle) * progress;
			g.draw(sprite, new Point2D.Double(x, y));
		}
	}
	
	public void step()
	{
		if (frame > frameCount - 9 && !impactSoundPlayed)
		{
			doSplashDamageLater(target, 300, 2);
			playSoundLater("smallExplosion2", target);
			impactSoundPlayed = true;
		}
		
		frame++;
	}
	
	public boolean isDone()
	{
		return frame >= frameCount;
	}
}
