package com.robbix.mp5.unit;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.geom.Point2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.Entity;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.utils.Position;

public class Meteor extends Entity
{
	private Position target;
	private int startTime;
	private int duration = 80;
	private double totalDistance = 20;
	private double angle = 35 * Math.PI / 180; // in rads
	
	private Point2D targetPoint;
	private Point2D point;
	
	public Meteor(Position target, int startTime)
	{
		this.target = target;
		this.startTime = startTime;
		this.targetPoint = target.getCenterPoint();
		point = new Point2D.Double(
			targetPoint.getX() + totalDistance *  cos(angle),
			targetPoint.getY() + totalDistance * -sin(angle)
		);
	}
	
	public Position getTargetPosition()
	{
		return target;
	}
	
	public boolean isAlive()
	{
		return Game.game.getFrame() < startTime + duration;
	}
	
	public boolean isForming()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame < 5;
	}
	
	public boolean isFlying()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame >= 5 && frame < duration - 9;
	}
	
	public boolean isCrashing()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame >= duration - 9 ;
	}
	
	public String getStatusString()
	{
		String str = point + " ";
		
		if (!isAlive()) str += "dead";
		else if (isForming()) str += "forming";
		else if (isFlying()) str += "flying";
		else if (isCrashing()) str += "crashing";
		
		return str;
	}
	
	public int getFormationTime()
	{
		return startTime;
	}
	
	public int getImpactTime()
	{
		return startTime + duration - 9;
	}
	
	public Position getPosition()
	{
		return new Position(point);
	}
	
	public Point2D getAbsPoint()
	{
		return point;
	}
	
	public LayeredMap getContainer()
	{
		return null;
	}
	
	public void step()
	{
		System.out.println("Meteor step(): " + getStatusString());
		
		if (Game.game.getFrame() == startTime)
		{
			Game.game.playSound("meteor", target);
		}
		else if (Game.game.getFrame() == getImpactTime())
		{
			Game.game.playSound("smallExplosion2", target);
	    	Game.game.doLater(new Runnable()
	    	{
	    		public void run()
	    		{
	    			Game.game.doSplashDamage(target, 300, 2);
	    		}
	    	});
		}
		
		if (isForming() || isFlying())
		{
			double progress = (getImpactTime() - Game.game.getFrame()) / (double)(duration - 9);
			point = new Point2D.Double(
				targetPoint.getX() + totalDistance * progress *  cos(angle),
				targetPoint.getY() + totalDistance * progress * -sin(angle)
			);
		}
		else
		{
			point = targetPoint;
		}
	}
}
