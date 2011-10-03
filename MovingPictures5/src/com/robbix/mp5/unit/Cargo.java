package com.robbix.mp5.unit;

import com.robbix.mp5.ResourceType;

public class Cargo
{
	public static enum Type
	{
		EMPTY,
		COMMONORE,
		RAREORE,
		STRUCTURE_KIT;
	}
	
	private Type type;
	private Object param;
	
	public static final Cargo EMPTY = new Cargo(Type.EMPTY, 0);
	
	public static Cargo newConVecCargo(String structureType)
	{
		return new Cargo(Type.STRUCTURE_KIT, structureType);
	}
	
	public static Cargo newTruckCargo(String typeString, int amount)
	{
		Cargo.Type type = Cargo.Type.valueOf(Cargo.Type.class, typeString);
		return newTruckCargo(type, amount);
	}
	
	public static Cargo newTruckCargo(Type type, int amount)
	{
		if (type == Type.EMPTY || amount <= 0)
			return EMPTY;
		
		return new Cargo(type, amount);
	}
	
	public static Cargo newTruckCargo(ResourceType type, int amount)
	{
		if (amount <= 0)
			return EMPTY;
		
		switch (type)
		{
		case COMMON_ORE: return new Cargo(Type.COMMONORE, amount);
		case RARE_ORE:   return new Cargo(Type.RAREORE, amount);
		default: throw new IllegalArgumentException("invalid res type");
		}
	}
	
	private Cargo(Type type, Object param)
	{
		this.type = type;
		this.param = param;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public int getAmount()
	{
		return ((Number) param).intValue();
	}
	
	public String getStructureType()
	{
		return (String) param;
	}
	
	public String toString()
	{
		if (type == Type.EMPTY)
		{
			return "empty";
		}
		else if (type == Type.STRUCTURE_KIT)
		{
			return getStructureType();
		}
		else
		{
			return getAmount() + " " + type;
		}
	}
}
