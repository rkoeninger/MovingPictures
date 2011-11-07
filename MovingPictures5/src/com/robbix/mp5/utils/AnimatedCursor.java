package com.robbix.mp5.utils;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

public class AnimatedCursor
{
	private Cursor[] cursors;
	private int[] delays;
	private String name;
	private Map<Component, Timer> assignments;
	
	public AnimatedCursor(String name, Cursor cursor)
	{
		this(name, new Cursor[]{cursor}, new int[]{Integer.MAX_VALUE});
	}
	
	public AnimatedCursor(String name, Cursor[] cursors, int delay)
	{
		this(name, cursors, nCopies(cursors.length, delay));
	}
	
	public AnimatedCursor(String name, Cursor[] cursors, int[] delays)
	{
		if (cursors.length == 0)
			throw new IllegalArgumentException("Must have at least 1 cursor");
		
		if (cursors.length != delays.length)
			throw new IllegalArgumentException("Cursor count not equal to delay count");
		
		this.cursors = cursors;
		this.delays = delays;
		this.name = name;
		this.assignments = new HashMap<Component, Timer>();
	}
	
	public void show(Component component)
	{
		Timer timer = assignments.get(component);
		
		if (timer == null)
		{
			timer = new Timer(0, null);
			timer.addActionListener(new DoRotateCursor(component, timer));
			assignments.put(component, timer);
		}
		
		if (!timer.isRunning())
			timer.start();
	}
	
	public void hide(Component component)
	{
		Timer timer = assignments.get(component);
		
		if (timer != null)
		{
			timer.stop();
			assignments.remove(component);
			component.setCursor(null);
		}
	}
	
	public void hideAll()
	{
		for (Timer timer : assignments.values())
			timer.stop();
		
		assignments.clear();
	}
	
	private class DoRotateCursor implements ActionListener
	{
		private Component component;
		private Timer timer;
		private int index = 0;
		
		public DoRotateCursor(Component component, Timer timer)
		{
			this.component = component;
			this.timer = timer;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Cursor current = cursors[index];
			component.setCursor(current);
			timer.setDelay(delays[index]);
			index = (index + 1) % cursors.length;
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public Cursor getCursor(int index)
	{
		return cursors[index];
	}
	
	/**
	 * Get delay for nth frame, measured in milliseconds.
	 */
	public int getDelay(int index)
	{
		return delays[index];
	}
	
	public int getFrameCount()
	{
		return cursors.length;
	}
	
	private static int[] nCopies(int copies, int value)
	{
		int[] array = new int[copies];
		Arrays.fill(array, value);
		return array;
	}
}
