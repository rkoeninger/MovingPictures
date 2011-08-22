package com.robbix.mp5;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.ani.AmbientAnimation;
import com.robbix.mp5.unit.Unit;

public class Engine
{
	private static final int DEFAULT_DELAY = 10;
	
	private boolean paused;
	
	private LayeredMap map;
	private DisplayPanel panel;
	
	private Runnable animationCycle;
	private Timer timer;
	
	private int previousDelay;
	
	private int frame;
	
	private Set<Trigger> triggers;
	
	public Engine(Game game)
	{
		map = game.getMap();
		panel = game.getDisplay();
		paused = true;
		
		triggers = new HashSet<Trigger>();
		
		animationCycle = new AnimationCycle();
		timer = new Timer(DEFAULT_DELAY, new ThreadCycle());
		previousDelay = -1;
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
	
	public void addTrigger(Trigger trigger)
	{
		synchronized (triggers)
		{
			triggers.add(trigger);
		}
	}
	
	public void removeTrigger(Trigger trigger)
	{
		synchronized (triggers)
		{
			triggers.remove(trigger);
		}
	}
	
	public int getDelay()
	{
		return timer.getDelay();
	}
	
	public boolean isThrottled()
	{
		return timer.getDelay() > 0;
	}
	
	public synchronized void toggleThrottle()
	{
		if (timer.getDelay() > 0)
		{
			previousDelay = timer.getDelay();
			timer.setDelay(0);
		}
		else
		{
			timer.setDelay(previousDelay);
			previousDelay = -1;
		}
	}
	
	public int getTime()
	{
		return frame;
	}
	
	public synchronized void unthrottle()
	{
		if (timer.getDelay() == 0)
			throw new IllegalStateException("Already unthrottled");
		
		previousDelay = timer.getDelay();
		timer.setDelay(0);
	}
	
	public synchronized void throttle()
	{
		if (previousDelay < 0)
			throw new IllegalStateException("Already throttled");
		
		timer.setDelay(previousDelay);
		previousDelay = -1;
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
		Set<Runnable> callbacks = new HashSet<Runnable>();
		AtomicReference<Runnable> returnValue = new AtomicReference<Runnable>();
		
		public void run()
		{
			synchronized (Engine.this)
			{
				/*
				 * Triggers
				 */
				synchronized (triggers)
				{
					for (Trigger trigger : triggers)
					{
						trigger.step(Mediator.game, frame);
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
					{
						unit.getTurret().step();
					}
				}
				
				/*
				 * Animation
				 */
				callbacks.clear();
				
				synchronized (panel.getAnimations())
				{
					Iterator<AmbientAnimation> animationItr =
						panel.getAnimations().iterator();
					
					while (animationItr.hasNext())
					{
						AmbientAnimation animation = animationItr.next();
						
						if (animation.hasCallback())
						{
							animation.step(returnValue);
							
							if (returnValue.get() != null)
							{
								callbacks.add(returnValue.get());
								returnValue.set(null);
							}
						}
						else
						{
							animation.step();
						}
						
						if (animation.isDone())
						{
							animationItr.remove();
						}
					}
				}
				
				for (Runnable toRun : callbacks)
				{
					if (toRun != null)
					{
						toRun.run();
					}
				}
				
				panel.repaint();
				
				frame++;
			}
		}
	}
}
