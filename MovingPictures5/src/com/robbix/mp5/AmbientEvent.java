package com.robbix.mp5;

public class AmbientEvent
{
	private AmbientEventType type;
	private String arg;
	
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
}
