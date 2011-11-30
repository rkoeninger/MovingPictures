package com.robbix.mp5.ui.ani;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Direction;
import com.robbix.utils.Position;
import com.robbix.utils.Utils;

public class RPGAnimation extends WeaponAnimation
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
	
	public RPGAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target);
		
		puffs = new HashSet<SmokePuff>();

		distance = getFireOrigin().distance(getFireImpact());
		angle = Math.atan2(
			getFireOrigin().getY() - getFireImpact().getY(),
			getFireOrigin().getX() - getFireImpact().getX()
		);
		rocketFrameCount = (int) (distance / speed);
		totalFrameCount = rocketFrameCount + 12;
		
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
	
	public void step()
	{
		if (frame == 0)
		{
			playSoundLater("rocketLaunch", getAttacker().getPosition());
			
			for (int p = 0; p < 12; ++p)
			{
				SmokePuff puff = new SmokePuff();
				puff.startingFrame = Utils.randInt(0, 12);
				puff.puffNumber = Utils.randInt(1, 3);
				double spreadAngle = Utils.randFloat(0, 2 * Math.PI);
				double x = getFireOrigin().getX();
				double y = getFireOrigin().getY();
				x += Utils.randFloat(-0.25, 0.25);
				y += Utils.randFloat(-0.25, 0.25);
				x += Math.cos(angle) * -0.5;
				y += Math.sin(angle) * -0.5;
				x += Math.cos(spreadAngle) * (p / 24.0);
				y += Math.sin(spreadAngle) * (p / 24.0);
				puff.point = new Point2D.Double(x, y);
				puffs.add(puff);
			}
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
			playSoundLater("smallExplosion1", targetPos);
			doSplashDamageLater(targetPos, getAttacker().getType().getDamage(), 1);
		}
		
		frame++;
	}
	
	public void paint(DisplayGraphics g)
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
			
			g.draw(puffGroup.getFrame(puffFrame), puff.point);
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
			g.draw(rocketSprite, rocketPoint);
		}
		else if (frame < totalFrameCount)
		{
			SpriteGroup explosionGroup = rocketSprites.get("explosion");
			Sprite explosionSprite = explosionGroup.getFrame(frame - rocketFrameCount);
			g.draw(explosionSprite, getFireImpact());
		}
	}
}