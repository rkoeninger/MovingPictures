package com.robbix.mp5.unit;

import java.util.Collections;
import java.util.Map;

import com.robbix.mp5.map.ResourceType;

public class Cost
{
	public static final Cost FREE = new Cost();
	
	private Map<ResourceType, Integer> resourceMap;
	
	/**
	 * Creates a "free" Cost object.
	 */
	private Cost()
	{
		this.resourceMap = Collections.emptyMap();
	}
	
	public Cost(Map<ResourceType, Integer> resourceMap)
	{
		this.resourceMap = resourceMap;
	}
	
	public int getAmount(ResourceType type)
	{
		if (resourceMap.containsKey(type))
		{
			return resourceMap.get(type);
		}
		
		return 0;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<ResourceType, Integer> entry : resourceMap.entrySet())
		{
			if (builder.length() > 0)
			{
				builder.append(", ");
			}
			
			String type = entry.getKey() == ResourceType.COMMON_ORE
				? "common" : "rare";
			
			builder.append(entry.getValue()).append(' ').append(type);
		}
		
		return builder.toString();
	}
}
