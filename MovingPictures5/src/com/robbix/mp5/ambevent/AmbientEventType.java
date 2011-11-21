package com.robbix.mp5.ambevent;

import java.util.concurrent.atomic.AtomicInteger;

public class AmbientEventType
{
	private static AtomicInteger nextSerial = new AtomicInteger();
	
	private int serial;
	private String name;
	private int effectAreaW;
	private int effectAreaH;
	
	public AmbientEventType(String name)
	{
		this(name, -1, -1);
	}
	
	public AmbientEventType(String name, int w, int h)
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
}
