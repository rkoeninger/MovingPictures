package com.robbix.mp5.ambevent;

public class AmbientEvent
{
	private AmbientEventType type;
	private String arg;
	private int step;
	private int length;
	
	private AmbientEvent(AmbientEventType type, String arg)
	{
		this.type = type;
		this.arg = arg;
	}
	
	public AmbientEventType getType()
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
}
