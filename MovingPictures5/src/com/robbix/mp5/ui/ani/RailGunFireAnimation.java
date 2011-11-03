package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;

public class RailGunFireAnimation extends WeaponFireAnimation
{
	private static class SmokeRing
	{
		public Point2D point;
		public int startingFrame;
	}
	
	private Set<SmokeRing> rings;
	
	private int frame = 0;
	private int frameCount;
	
	private Direction rocketDir;
	
	private double distance;
	private double angle; // in rads
	private double speed = 4;
	
	private boolean explosionTime;
	
	public RailGunFireAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target);
		
		rings = new HashSet<SmokeRing>();
		
		distance = getFireOrigin().distance(getFireImpact());
		angle = Math.atan2(
			getFireOrigin().getY() - getFireImpact().getY(),
			getFireOrigin().getX() - getFireImpact().getX()
		);
		frameCount = (int) (distance / speed);
		
		rocketDir = Direction.getDirection(
			attacker.getPosition(),
			target.getPosition()
		);
	}
	
	public boolean atHotPoint()
	{
		return frame == frameCount - 1;
	}
	
	public boolean isDone()
	{
		return frame >= frameCount && rings.isEmpty();
	}
	
	public void step(AtomicReference<Runnable> ref)
	{
		if (frame == 0)
		{
			playSoundLater("railGunFire", getAttacker().getPosition());
		}
		
		if ((frame + 1) % 3 == 0 && !explosionTime)
		{
			SmokeRing ring = new SmokeRing();
			ring.startingFrame = frame;
			
			double progress = (frameCount - frame) / (double)(frameCount);
			
			ring.point = new Point2D.Double(
				getFireImpact().getX() + distance * Math.cos(angle) * progress,
				getFireImpact().getY() + distance * Math.sin(angle) * progress
			);
			
			System.out.println("smoke ring at " + ring.point);
			
			rings.add(ring);
		}
		
		final Position targetPos = getTarget().getPosition();
		
		if (atHotPoint())
		{
			playSoundLater("railGunHit", targetPos);
			explosionTime = true;
		}
		
		frame++;
	}
	
	public void paint(Graphics g)
	{
		SpriteSet rocketSprites = lib.getAmbientSpriteSet("aRocket");
		SpriteGroup ring1 = rocketSprites.get("smokeRing1");
		SpriteGroup ring2 = rocketSprites.get("smokeRing2");
		SpriteGroup ring3 = rocketSprites.get("smokeRing3");
		int ringDuration = ring1.getFrameCount() + ring2.getFrameCount() + ring3.getFrameCount();
		
		Iterator<SmokeRing> ringItr = rings.iterator();
		
		while (ringItr.hasNext())
		{
			SmokeRing ring = ringItr.next();
			int ringFrame = frame - ring.startingFrame;
			
			if (ringFrame < 0) // Hasn't happened yet
			{
				continue;
			}
			else if (ringFrame >= ringDuration) // Already completed
			{
				ringItr.remove();
				continue;
			}
			
			Sprite ringSprite = null;
			
			if (ringFrame < ring1.getFrameCount())
			{
				ringSprite = ring1.getFrame(ringFrame);
			}
			else if (ringFrame - ring1.getFrameCount() < ring2.getFrameCount())
			{
				ringSprite = ring2.getFrame(ringFrame
					- ring1.getFrameCount());
			}
			else
			{
				ringSprite = ring3.getFrame(ringFrame
					- ring1.getFrameCount()
					- ring2.getFrameCount());
			}
			
			panel.draw(g, ringSprite, ring.point);
		}
		
		if (!explosionTime)
		{
			Sprite rocketSprite = rocketSprites
				.get("projectile")
				.getSprite(rocketDir.ordinal() * 2);
			double progress = (frameCount - frame) / (double)(frameCount);
			Point2D rocketPoint = new Point2D.Double(
				getFireImpact().getX() + distance * Math.cos(angle) * progress,
				getFireImpact().getY() + distance * Math.sin(angle) * progress
			);
			panel.draw(g, rocketSprite, rocketPoint);
		}
	}
}
