package com.robbix.mp5;

import com.robbix.utils.Position;
import com.robbix.utils.Region;
import com.robbix.utils.Utils;

public class MeteorShowerTrigger extends Trigger
{
	private int freq;
	private int duration;
	private int startTime = -1;
	
	public MeteorShowerTrigger(int freq, int duration)
	{
		if (freq <= 0)
			throw new IllegalArgumentException();
		
		this.freq = freq;
		this.duration = duration;
	}
	
	public void step(Game game, int time)
	{
		if (startTime == -1)
		{
			startTime = time;
		}
		
		if (time - startTime >= duration)
		{
			return; // Need to be removed
		}
		
		if (time % freq == 0)
		{
			Region mapBounds = game.getMap().getBounds();
			Position pos = new Position(
				Utils.randInt(0, mapBounds.w - 1),
				Utils.randInt(0, mapBounds.h - 1)
			);
			Game.game.doSpawnMeteor(pos);
		}
	}
}
