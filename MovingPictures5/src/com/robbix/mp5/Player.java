package com.robbix.mp5;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import com.robbix.mp5.unit.Cost;

public class Player
{
	private String name;
	private int id;
	private int colorHue;
	private Color color;
	
	private Map<ResourceType, Integer> resources;
	private Population population;
	
	public Player(int id, String name, int colorHue)
	{
		this.id = id;
		this.name = name;
		this.colorHue = colorHue;
		this.color = new Color(Color.HSBtoRGB(colorHue / 360.0f, 1.0f, 1.0f));
		this.resources = new EnumMap<ResourceType, Integer>(ResourceType.class);
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int getColorHue()
	{
		return colorHue;
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public Map<ResourceType, Integer> getResources()
	{
		return resources;
	}
	
	public boolean canAfford(Cost cost)
	{
		for (ResourceType type : ResourceType.values())
		{
			int costAmount = cost.getAmount(type);
			Integer hasAmount = resources.get(type);
			
			if (costAmount == 0)
				continue;
			
			if (hasAmount == null)
				hasAmount = 0;
			
			if (hasAmount < costAmount)
				return false;
		}
		
		return true;
	}
	
	public void spend(Cost cost)
	{
		for (ResourceType type : ResourceType.values())
		{
			int costAmount = cost.getAmount(type);
			Integer hasAmount = resources.get(type);
			
			if (costAmount == 0)
				continue;
			
			if (hasAmount == null)
				hasAmount = 0;
			
			if (hasAmount < costAmount)
				throw new RuntimeException("can't afford it");
			
			resources.put(type, hasAmount - costAmount);
		}
	}
	
	public void addResource(ResourceType type, int amount)
	{
		Integer currentAmount = resources.get(type);
		
		if (currentAmount == null)
			currentAmount = 0;
		
		currentAmount += amount;
		resources.put(type, currentAmount);
	}
	
	public Population getPopulation()
	{
		return population;
	}
	
	public String getStatusString()
	{
		Integer commonOre = resources.get(ResourceType.COMMON_ORE);
		Integer rareOre = resources.get(ResourceType.RARE_ORE);
		
		if (commonOre == null)
			commonOre = 0;
		
		if (rareOre == null)
			rareOre = 0;
		
		return "[" + id + "] " + name + " " +
		commonOre + " common, " + rareOre + " rare";
	}
}
