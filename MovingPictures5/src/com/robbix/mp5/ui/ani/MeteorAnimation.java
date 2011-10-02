package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;

public class MeteorAnimation extends AmbientAnimation
{
	private int frame = 0;
	private int frameCount = 80;
	
	private Position target;
	private Point point;
	private Rectangle bounds;
	
	private double distance = 600;
	private double angle = 35 * Math.PI / 180; // in rads
	
	private boolean impactSoundPlayed = false;
	
	private SpriteGroup forming;
	private SpriteGroup flying;
	private SpriteGroup impact;
	
	public MeteorAnimation(Position target, SpriteLibrary lib)
	{
		this.target = target;
		
		forming = lib.getSequence("aMeteor/forming");
		flying  = lib.getSequence("aMeteor/flying");
		impact  = lib.getSequence("aMeteor/impact");
		
		point = new Point(
			target.x * 32 + 16,
			target.y * 32 + 16
		);
		
		bounds = new Rectangle(
			point.x - 32,
			point.y + 32,
			(int) (distance * Math.cos(angle)),
			(int) (distance * Math.sin(angle))
		);
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	public void paint(Graphics g)
	{
		if (frame < 5)
		{
			Sprite sprite = forming.getFrame(frame);

			double progress = (frameCount - frame) / (double)(frameCount);
			
			int x = point.x;
			int y = point.y;
			
			x += (int) (distance *  Math.cos(angle) * progress);
			y += (int) (distance * -Math.sin(angle) * progress);
			
			g.drawImage(
				sprite.getImage(),
				x + sprite.getXOffset(),
				y + sprite.getYOffset(),
				null
			);
		}
		else if (frame > frameCount - 9)
		{
			Sprite sprite = impact.getFrame(impact.getFrameCount() - (frameCount - frame + 1));
			
			int x = point.x;
			int y = point.y;
			
			g.drawImage(
				sprite.getImage(),
				x + sprite.getXOffset(),
				y + sprite.getYOffset(),
				null
			);
		}
		else
		{
			Sprite sprite = flying.getFrame(frame % flying.getFrameCount());
			
			int x = point.x;
			int y = point.y;
			
			x += (int) (distance *  Math.cos(angle) * (frameCount - frame) / (frameCount));
			y += (int) (distance * -Math.sin(angle) * (frameCount - frame) / (frameCount));
			
			g.drawImage(
				sprite.getImage(),
				x + sprite.getXOffset(),
				y + sprite.getYOffset(),
				null
			);
		}
	}
	
	public void step(AtomicReference<Runnable> ref)
	{
		if (frame > frameCount - 9)
		{
			if (!impactSoundPlayed)
			{
				ref.set(new Runnable()
				{
					public void run()
					{
						Mediator.doSplashDamage(target, 300, 2);
						Mediator.sounds.play("smallExplosion2");
					}
				});
				
				impactSoundPlayed = true;
			}
		}
		
		frame++;
	}
	
	public boolean isDone()
	{
		return frame >= frameCount;
	}
}
