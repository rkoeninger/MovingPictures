package com.robbix.mp5;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.Timer;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.ani.AmbientAnimation;
import com.robbix.mp5.unit.Unit;

public class Engine
{
	private static final int DEFAULT_DELAY = 10;
	
	private boolean paused;
	private Game game;
	private LayeredMap map;
	private DisplayPanel panel;
	private Runnable animationCycle;
	private Timer timer;
	private int frame;
	
	public Engine(Game game)
	{
		this.game = game;
		map = game.getMap();
		panel = game.getDisplay();
		paused = true;
		animationCycle = new AnimationCycle();
		timer = new Timer(DEFAULT_DELAY, new ThreadCycle());
		frame = 0;
	}
	
	public synchronized void play()
	{
		paused = false;
		
		if (!timer.isRunning())
			timer.start();
	}
	
	public void stop()
	{
		paused = true;
	}
	
	public void pause()
	{
		paused = !paused;
	}
	
	public boolean isRunning()
	{
		return !paused;
	}
	
	public void step()
	{
		if (!paused)
			throw new IllegalStateException("Thread is running");
		
		animationCycle.run();
	}
	
	public void dispose()
	{
		pause();
	}
	
	public int getDelay()
	{
		return timer.getDelay();
	}
	
	public void setDelay(int delay)
	{
		timer.setDelay(delay);
	}
	
	public boolean isThrottled()
	{
		return timer.getDelay() > 0;
	}
	
	public int getTime()
	{
		return frame;
	}
	
	private class ThreadCycle implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (!paused)
			{
				animationCycle.run();
			}
		}
	}
	
	private class AnimationCycle implements Runnable
	{
		long prevTime;
		int framePeriod = 32;
		double fps = 0.0;
		
		public void run()
		{
			synchronized (Engine.this)
			{
				long time = System.nanoTime();
				
				if (frame % framePeriod == 0 && frame != 0)
				{
					fps = 1000000000.0 / (time - prevTime);
				}
				
				prevTime = time;
				panel.showFrameNumber(frame, fps);
				
				/*
				 * Triggers
				 */
				synchronized (game.getTriggers())
				{
					for (Trigger trigger : game.getTriggers())
					{
						trigger.step(Game.game, frame);
					}
				}
				
				/*
				 * Mechanics
				 */
				for (Unit unit : map.getUnitIterator())
				{
					if (unit.isDead() || unit.isFloating())
						continue;
					
					unit.step();
					
					if (unit.hasTurret())
						unit.getTurret().step();
				}
				
				/*
				 * Animation
				 */
				synchronized (panel.getAnimations())
				{
					Iterator<AmbientAnimation> animationItr =
						panel.getAnimations().iterator();
					
					while (animationItr.hasNext())
					{
						AmbientAnimation animation = animationItr.next();
						animation.step();
						
						if (animation.isDone())
							animationItr.remove();
					}
				}
				
				for (Runnable doRun : game.getAndClearDoLaters())
					doRun.run();
				
				for (DisplayPanel panel : game.getDisplays())
					panel.repaint();
				
				frame++;
			}
		}
	}
}
