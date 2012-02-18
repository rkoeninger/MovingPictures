package com.robbix.mp5.unit;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.Entity;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.utils.Position;
import com.robbix.utils.Utils;

public class Rocket extends Entity
{
	public static class SmokePuff
	{
		public int puffNumber;
		public Point2D point;
		public int startingFrame;
		
		public SmokePuff(int puffNumber, Point2D point, int startingFrame)
		{
			this.puffNumber = puffNumber;
			this.point = point;
			this.startingFrame = startingFrame;
		}
	}
	
	private List<SmokePuff> puffs;
	
	private Point2D startingPoint;
	private Point2D point;
	private double speed = 0.2;
	private Point2D targetPoint;
	private double angle;
	private int frame = 0;
	private double distance;
	private int rocketFrameCount;
	private double damage;
	
	public Rocket(Point2D startPoint, Point2D targetPoint, double damage)
	{
		this.startingPoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
		this.point = startPoint;
		this.targetPoint = targetPoint;
		double dx = targetPoint.getX() - point.getX();
		double dy = targetPoint.getY() - point.getY();
		this.angle = atan2(dx, dy);
		this.puffs = new ArrayList<SmokePuff>();
		distance = sqrt(dx * dx + dy * dy);
		rocketFrameCount = (int) (distance / speed);
		this.damage = damage;
	}
	
	public List<SmokePuff> getPuffs()
	{
		return puffs;
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
		return Game.game.getMap();
	}
	
	public void step()
	{
		double rx = point.getX() + speed * cos(angle);
		double ry = point.getY() + speed * sin(angle);
		point = new Point2D.Double(rx, ry);
		
		if (frame == 0)
		{
			Game.game.playSoundLater("rocketLaunch", getPosition());
			
			for (int p = 0; p < 12; ++p)
			{
				double spreadAngle = Utils.randFloat(0, 2 * Math.PI);
				double x = startingPoint.getX();
				double y = startingPoint.getY();
				x += Utils.randFloat(-0.25, 0.25);
				y += Utils.randFloat(-0.25, 0.25);
				x += Math.cos(angle) * -0.5;
				y += Math.sin(angle) * -0.5;
				x += Math.cos(spreadAngle) * (p / 24.0);
				y += Math.sin(spreadAngle) * (p / 24.0);
				puffs.add(new SmokePuff(Utils.randInt(1, 3), new Point2D.Double(x, y), Utils.randInt(0, 12)));
			}
		}
		
		if ((frame + 1) % 2 == 0 && frame > 4 && frame < rocketFrameCount)
		{
			double progress = (rocketFrameCount - frame) / (double)(rocketFrameCount);
			
			Point2D p = new Point2D.Double(
				targetPoint.getX() + distance * Math.cos(angle) * progress,
				targetPoint.getY() + distance * Math.sin(angle) * progress
			);
			
			puffs.add(new SmokePuff(Utils.randInt(1, 3), p, frame));
		}
		
		if (frame == rocketFrameCount)
		{
			Game.game.playSoundLater("smallExplosion1", getPosition());
			Game.game.doSplashDamageLater(getPosition(), damage, 1);
		}
		
		frame++;
	}
	
	public boolean isAlive()
	{
		return targetPoint.distance(point) > 0.5;
	}
}
