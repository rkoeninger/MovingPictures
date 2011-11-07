package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;

public class AcidCloudAnimation extends WeaponAnimation
{
	private class SmokePuff
	{
		public int puffNumber;
		public Point2D point;
		public int startingFrame;
	}
	
	private Set<SmokePuff> puffs;
	
	private int frame = 0;
	private int rocketFrameCount;
	private int totalFrameCount;
	
	private double distance;
	private double angle; // in rads
	private double speed = 4 / 32.0;
	
	private Direction rocketDir;
	
	private Position targetPos;
	
	public AcidCloudAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target);
		
		lib.loadModuleAsync("aAcidCloud");
		
		puffs = new HashSet<SmokePuff>();

		distance = getFireOrigin().distance(getFireImpact());
		angle = Math.atan2(
			getFireOrigin().getY() - getFireImpact().getY(),
			getFireOrigin().getX() - getFireImpact().getX()
		);
		rocketFrameCount = (int) (distance / speed);
		totalFrameCount = rocketFrameCount + 29;
		
		rocketDir = Direction.getDirection(
			attacker.getPosition(),
			target.getPosition()
		);
		
		targetPos = target.getPosition();
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
			playSoundLater("rocketLaunch", getAttacker().getPosition());
		}
		
		if ((frame + 1) % 2 == 0 && frame > 4 && frame < rocketFrameCount)
		{
			SmokePuff puff = new SmokePuff();
			puff.startingFrame = frame;
			puff.puffNumber = Utils.randInt(1, 3);
			
			double progress = (rocketFrameCount - frame) / (double)(rocketFrameCount);
			
			puff.point = new Point2D.Double(
				getFireImpact().getX() + distance * Math.cos(angle) * progress,
				getFireImpact().getY() + distance * Math.sin(angle) * progress
			);
			
			puffs.add(puff);
		}
		
		if (atHotPoint())
		{
			playSoundLater("acidCloud", targetPos);
		}
		
		if (frame >= rocketFrameCount && frame % 10 == 0)
		{
			doSplashDamageLater(targetPos, getAttacker().getType().getDamage(), 2.5);
		}
		
		frame++;
	}
	
	public void paint(Graphics g)
	{
		SpriteSet rocketSprites = lib.getAmbientSpriteSet("aRocket");
		SpriteGroup puff1 = rocketSprites.get("smokePuff1");
		SpriteGroup puff2 = rocketSprites.get("smokePuff2");
		SpriteGroup puff3 = rocketSprites.get("smokePuff3");
		
		Iterator<SmokePuff> puffItr = puffs.iterator();
		
		while (puffItr.hasNext())
		{
			SmokePuff puff = puffItr.next();
			int puffFrame = frame - puff.startingFrame;
			SpriteGroup puffGroup = null;
			
			switch (puff.puffNumber)
			{
			case 1: puffGroup = puff1; break;
			case 2: puffGroup = puff2; break;
			case 3: puffGroup = puff3; break;
			}
			
			if (puffFrame < 0) // Hasn't happened yet
			{
				continue;
			}
			else if (puffFrame >= puffGroup.getFrameCount()) // Already happened
			{
				puffItr.remove();
				continue;
			}
			
			panel.draw(g, puffGroup.getFrame(puffFrame), puff.point);
		}
		
		if (frame < rocketFrameCount)
		{
			Sprite rocketSprite = rocketSprites
				.get("projectile")
				.getSprite(rocketDir.ordinal() * 2);
			double progress = (rocketFrameCount - frame) / (double)(rocketFrameCount);
			Point2D rocketPoint = new Point2D.Double(
				getFireImpact().getX() + distance * Math.cos(angle) * progress,
				getFireImpact().getY() + distance * Math.sin(angle) * progress
			);
			panel.draw(g, rocketSprite, rocketPoint);
		}
		else if (frame < totalFrameCount)
		{
			SpriteSet acidSprites = lib.getAmbientSpriteSet("aAcidCloud");
			SpriteGroup cloud1 = acidSprites.get("cloud1");
			SpriteGroup cloud2 = acidSprites.get("cloud2");
			SpriteGroup cloud3 = acidSprites.get("cloud3");
			int acidFrame = frame - rocketFrameCount;
			
			Sprite acidSprite = null;
			
			if (acidFrame < cloud1.getFrameCount())
			{
				acidSprite = cloud1.getFrame(acidFrame);
			}
			else if (acidFrame - cloud1.getFrameCount() < cloud2.getFrameCount())
			{
				acidSprite = cloud2.getFrame(acidFrame
					- cloud1.getFrameCount());
			}
			else if (acidFrame
				- cloud1.getFrameCount()
				- cloud2.getFrameCount()
				< cloud2.getFrameCount())
			{
				acidSprite = cloud2.getFrame(acidFrame
					- cloud1.getFrameCount()
					- cloud2.getFrameCount());
			}
			else
			{
				acidSprite = cloud3.getFrame(acidFrame
					- cloud1.getFrameCount()
					- cloud2.getFrameCount()
					- cloud2.getFrameCount());
			}
			
			panel.draw(g, acidSprite, targetPos.getCenterPoint());
		}
	}
}