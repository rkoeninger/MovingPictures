package com.robbix.mp5.event;

public interface Event
{
	public EventType getType();
	public String getArg();
	public int getTime();
	public void step();
}
