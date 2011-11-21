package com.robbix.mp5.event;

public abstract class Event
{
	private EventType type;
	private String arg;
	private int step;
	private int length;
	
	protected Event(EventType type, String arg)
	{
		this.type = type;
		this.arg = arg;
	}
	
	public EventType getType()
	{
		return type;
	}
	
	public String getArg()
	{
		return arg;
	}
	
	public boolean isComplete()
	{
		return step >= length;
	}
	
	public abstract void step();
}
