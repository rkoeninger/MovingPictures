package com.robbix.mp5.event;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventFactory
{
	public static EventFactory load(File file) throws IOException
	{
		return new EventFactory();
	}
	
	private Map<String, EventType> types;
	
	private EventFactory()
	{
		types = new HashMap<String, EventType>();
	}
	
	public Event newEvent(String typeName)
	{
		EventType type = types.get(typeName);
		return type != null ? newEvent(type) : null;
	}
	
	public Event newEvent(EventType type)
	{
		return type.newEvent();
	}
}
