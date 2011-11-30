package com.robbix.mp5.event;

/**
 * An event that starts at a moment and time and persists
 * for multiple time-steps. It has both a start time and a
 * stop time, which may be unknown when the event is created.
 * 
 * e.g. an Acid Cloud Turret's acid cloud.
 */
public interface PersistentEvent extends Event
{
	public boolean isEndTimeDetermined();
	public int getEndTime();
	public boolean isComplete();
}
