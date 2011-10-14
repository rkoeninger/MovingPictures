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
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class RPGFireAnimation extends WeaponFireAnimation
{
	private class SmokePuff
	{
		public int puffNumber;
		public Point point;
		public int startingFrame;
	}
	
	private Set<SmokePuff> puffs;
	
	private List<SpriteGroup> puffGroups;
	
	private SpriteGroup explosion;
	
	private int frame = 0;
	private int rocketFrameCount;
	private int totalFrameCount;
	
	private Point firePoint;
	private Point targetPoint;
	private Rectangle bounds;
	
	private double distance;
	private double angle; // in rads
	private double speed = 4;
	
	private Sprite rocketSprite;
	
	public RPGFireAnimation(Unit attacker, Unit target)
	{
		super(attacker, target);
		
		LayeredMap map = attacker.getMap();
		SpriteLibrary lib = map.getDisplayPanel().getSpriteLibrary();
		
		puffs = new HashSet<SmokePuff>();
		
		puffGroups = new ArrayList<SpriteGroup>(3);
		
		puffGroups.add(lib.getAmbientSpriteGroup("aRocket", "smokePuff1"));
		puffGroups.add(lib.getAmbientSpriteGroup("aRocket", "smokePuff2"));
		puffGroups.add(lib.getAmbientSpriteGroup("aRocket", "smokePuff3"));
		
		explosion = lib.getAmbientSpriteGroup("aRocket", "explosion");
		
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
		rocketFrameCount = (int) (distance / speed);
		totalFrameCount = rocketFrameCount + explosion.getFrameCount();
		
		double rocketAngle = Math.atan2(
			firePoint.y - targetPoint.y,
			targetPoint.x - firePoint.x
		);
		rocketAngle /= (2 * Math.PI);
		int i = ((((int) Math.round(rocketAngle * 16)) % 16) + 16) % 16 * 2;
		
		rocketSprite = lib.getAmbientSpriteGroup("aRocket", "projectile").getFrame(i);
	}
	
	public boolean atHotPoint()
	{
		return frame == rocketFrameCount - 1;
	}
	
	public boolean isDone()
	{
		return frame >= totalFrameCount;
	}
	
	public void step(AtomicReference<Runnable> ref)
	{
		if (frame == 0)
		{
			ref.set(new Runnable()
			{
				public void run()
				{
					Mediator.playSound("rocketLaunch");
				}
			});
		}
		
		if ((frame + 1) % 2 == 0 && frame < rocketFrameCount)
		{
			SmokePuff puff = new SmokePuff();
			puff.startingFrame = frame;
			puff.puffNumber = Utils.randInt(0, 2);
			
			double progress = (rocketFrameCount - frame) / (double)(rocketFrameCount);
			
			int x = targetPoint.x;
			int y = targetPoint.y;
			
			x += (int) (distance * Math.cos(angle) * progress);
			y += (int) (distance * Math.sin(angle) * progress);
			
			puff.point = new Point(x, y);
			
			puffs.add(puff);
		}
		
		if (atHotPoint())
		{
			ref.set(new Runnable()
			{
				public void run()
				{
					Mediator.playSound("smallExplosion1");
				}
			});
		}
		
		frame++;
	}
	
	public void paint(Graphics g)
	{
		Iterator<SmokePuff> puffItr = puffs.iterator();
		
		while (puffItr.hasNext())
		{
			SmokePuff puff = puffItr.next();
			
			int puffFrame = (frame - puff.startingFrame) / 2;
			
			SpriteGroup puffGroup = puffGroups.get(puff.puffNumber);
			
			if (puffFrame < 0)
			{
				continue;
			}
			else if (puffFrame >= puffGroup.getFrameCount())
			{
				puffItr.remove();
				continue;
			}
			
			Sprite puffSprite = puffGroup.getFrame(puffFrame);
			
			g.drawImage(
				puffSprite.getImage(),
				puffSprite.getXOffset() + puff.point.x,
				puffSprite.getYOffset() + puff.point.y,
				null
			);
		}
		
		if (frame < rocketFrameCount)
		{
			double progress = (rocketFrameCount - frame) / (double)(rocketFrameCount);
			
			int x = targetPoint.x;
			int y = targetPoint.y;
			
			x += (int) (distance * Math.cos(angle) * progress);
			y += (int) (distance * Math.sin(angle) * progress);
			
			g.drawImage(
				rocketSprite.getImage(),
				rocketSprite.getXOffset() + x,
				rocketSprite.getYOffset() + y,
				null
			);
		}
		else if (frame < totalFrameCount)
		{
			Sprite explosionSprite = explosion.getFrame(frame - rocketFrameCount);
			
			g.drawImage(
				explosionSprite.getImage(),
				explosionSprite.getXOffset() + targetPoint.x,
				explosionSprite.getYOffset() + targetPoint.y,
				null
			);
		}
	}
}