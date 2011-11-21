package com.robbix.mp5.event;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event types include things like "aDeath" and "aRocket". This includes events that
 * are visible, like a death explosion, that are not themselves visible, like
 * the creation of a new unit (the new unit is visible), events that modify something
 * in the window but on the DisplayPanel, like getting money, and events that may be
 * totally invisible, like research being completed.
 */
public class EventType
{
	private static AtomicInteger nextSerial = new AtomicInteger();
	
	private int serial;
	private String name;
	private int effectAreaW;
	private int effectAreaH;
	
	public EventType(String name)
	{
		this(name, -1, -1);
	}
	
	public EventType(String name, int w, int h)
	{
		this.name = name;
		this.serial = nextSerial.getAndIncrement();
		this.effectAreaW = w;
		this.effectAreaH = h;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getEffectAreaWidth()
	{
		return effectAreaW;
	}
	
	public int getEffectAreaHeight()
	{
		return effectAreaH;
	}
	
	public boolean doesOccurAtPoint()
	{
		return effectAreaW <= 0 || effectAreaH <= 0;
	}
	
	public boolean doesOccurInArea()
	{
		return effectAreaW > 0 && effectAreaH > 0;
	}
	
	public int getSerial()
	{
		return serial;
	}
	
	public Event newEvent()
	{
		return null;
	}
}
