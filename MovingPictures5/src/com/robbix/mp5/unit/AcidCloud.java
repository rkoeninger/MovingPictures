package com.robbix.mp5.unit;

import java.awt.geom.Point2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.Entity;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.utils.Position;

public class AcidCloud extends Entity
{
	private Position pos;
	private int startTime;
	private int formingDuration = 10;
	private int burningDuration = 64;
	private int fadingDuration = 16;
	private int burnFreq = 10;
	
	public AcidCloud(Position pos, int startTime)
	{
		this.pos = pos;
		this.startTime = startTime;
	}
	
	public Position getPosition()
	{
		return pos;
	}
	
	public Point2D getAbsPoint()
	{
		return pos.getCenterPoint();
	}
	
	public int getStartTime()
	{
		return startTime;
	}
	
	public int getFormingDuration()
	{
		return formingDuration;
	}
	
	public int getBurningDuration()
	{
		return burningDuration;
	}
	
	public boolean isForming()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame < formingDuration;
	}
	
	public boolean isBurning()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame < formingDuration + burningDuration;
	}
	
	public boolean isFading()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame < formingDuration + burningDuration + fadingDuration;
	}
	
	public LayeredMap getContainer()
	{
		return null;
	}
	
	public void step()
	{
		int frame = Game.game.getFrame() - startTime;
		
		if (frame == startTime + formingDuration)
		{
			Game.game.playSound("acidCloud", pos);
		}
		
		if (isBurning() && (frame % burnFreq == 0))
		{
			Game.game.doSplashDamage(pos, 20, 2.5);
		}
	}
	
	public boolean isAlive()
	{
		int frame = Game.game.getFrame() - startTime;
		return frame < formingDuration + burningDuration + fadingDuration;
	}
}
