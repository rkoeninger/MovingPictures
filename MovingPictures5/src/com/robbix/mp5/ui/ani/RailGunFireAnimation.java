package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class RailGunFireAnimation extends WeaponFireAnimation
{
	private class SmokeRing
	{
		public Point point;
		public int startingFrame;
	}
	
	private Set<SmokeRing> rings;
	
	private List<Sprite> ringGroup;
	
	private int frame = 0;
	private int frameCount;
	
	private Point firePoint;
	private Point targetPoint;
	private Rectangle bounds;
	
	private double distance;
	private double angle; // in rads
	private double speed = 4;
	
	private Sprite rocketSprite;
	
	public RailGunFireAnimation(Unit attacker, Unit target)
	{
		super(attacker, target);
		
		LayeredMap map = attacker.getMap();
		SpriteLibrary lib = map.getDisplayPanel().getSpriteLibrary();
		
		rings = new HashSet<SmokeRing>();
		
		ringGroup = new ArrayList<Sprite>();
		ringGroup.addAll(lib.getSequence("aRocket/smokeRing1"));
		ringGroup.addAll(lib.getSequence("aRocket/smokeRing2"));
		ringGroup.addAll(lib.getSequence("aRocket/smokeRing3"));
		
		Direction rocketDir = Direction.getDirection(
			attacker.getPosition(),
			target.getPosition()
		);
		
		Point hotspot = lib.getHotspot(attacker, rocketDir);
		
		firePoint = new Point(attacker.getAbsX(), attacker.getAbsY());
		firePoint.translate(hotspot.x, hotspot.y);
		
		int tileSize = map.getDisplayPanel().getTileSize();
		int w = target.getWidth();
		int h = target.getHeight();
		int xTargetOffset = (tileSize * w / 2) + Utils.randInt(-5, 5);
		int yTargetOffset = (tileSize * h / 2) + Utils.randInt(-5, 5);
		
		targetPoint = new Point(
			target.getAbsX() + xTargetOffset,
			target.getAbsY() + yTargetOffset
		);
		
		bounds = new Rectangle(firePoint);
		bounds.add(targetPoint);
		
		distance = targetPoint.distance(firePoint);
		angle = Math.atan2(
			firePoint.y - targetPoint.y,
			firePoint.x - targetPoint.x
		);
		frameCount = (int) (distance / speed);
		
		double rocketAngle = Math.atan2(
			firePoint.y - targetPoint.y,
			targetPoint.x - firePoint.x
		);
		rocketAngle /= (2 * Math.PI);
		int i = ((((int) Math.round(rocketAngle * 16)) % 16) + 16) % 16 * 2;
		
		rocketSprite = lib.getSequence("aRocket/projectile").get(i);
	}
	
	public boolean atHotPoint()
	{
		return frame == frameCount - 1;
	}
	
	public boolean isDone()
	{
		return frame >= frameCount && rings.isEmpty();
	}
	
	public boolean hasCallback()
	{
		return true;
	}
	
	public void step()
	{
		throw new Error("should call step(AtomicReference)");
	}
	
	public void step(AtomicReference<Runnable> ref)
	{
		if (frame == 0)
		{
			ref.set(new Runnable()
			{
				public void run()
				{
					Mediator.sounds.play("railGunFire");
				}
			});
		}
		
		if ((frame + 1) % 3 == 0 && rocketSprite != null)
		{
			SmokeRing ring = new SmokeRing();
			ring.startingFrame = frame;
			
			double progress = (frameCount - frame) / (double)(frameCount);
			
			ring.point = new Point(
				targetPoint.x + (int) (distance * Math.cos(angle) * progress),
				targetPoint.y + (int) (distance * Math.sin(angle) * progress)
			);
			
			rings.add(ring);
		}
		
		if (atHotPoint())
		{
			ref.set(new Runnable()
			{
				public void run()
				{
					Mediator.sounds.play("railGunHit");
				}
			});
			
			rocketSprite = null;
		}
		
		frame++;
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	public void paint(Graphics g)
	{
		Iterator<SmokeRing> ringItr = rings.iterator();
		
		while (ringItr.hasNext())
		{
			SmokeRing ring = ringItr.next();
			
			int ringFrame = (frame - ring.startingFrame) / 2;
			
			if (ringFrame < 0)
			{
				continue;
			}
			else if (ringFrame >= ringGroup.size())
			{
				ringItr.remove();
				continue;
			}
			
			Sprite ringSprite = ringGroup.get(ringFrame);
			
			g.drawImage(
				ringSprite.getImage(),
				ringSprite.getXOffset() + ring.point.x,
				ringSprite.getYOffset() + ring.point.y,
				null
			);
		}
		
		if (rocketSprite != null)
		{
			double progress = (frameCount - frame) / (double)(frameCount);
			
			int x = targetPoint.x;
			int y = targetPoint.y;
			
			x += (int) (distance * Math.cos(angle) * progress);
			y += (int) (distance * Math.sin(angle) * progress);
			
			g.drawImage(
				rocketSprite.getImage(),
				x + rocketSprite.getXOffset(),
				y + rocketSprite.getYOffset(),
				null
			);
		}
		else
		{
			
		}
	}
}
