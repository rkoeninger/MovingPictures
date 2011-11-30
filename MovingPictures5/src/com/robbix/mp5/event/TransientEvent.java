package com.robbix.mp5.event;

/**
 * An event that occurs at a moment in time and does not persist.
 * It may have a visible effect, like an explosion, that is visible for
 * multiple frames, but the event itself occurs in a single moment.
 * 
 * e.g. The creation or death of a unit.
 */
public interface TransientEvent extends Event
{
	public void step();
}
