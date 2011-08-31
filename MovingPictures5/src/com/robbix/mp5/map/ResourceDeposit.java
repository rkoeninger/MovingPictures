package com.robbix.mp5.map;

import com.robbix.mp5.Player;
import com.robbix.mp5.ResourceType;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.unit.Cargo;

public class ResourceDeposit implements Cloneable
{
	public static final ResourceDeposit UNKNOWN =
		new ResourceDeposit(null, null, -1, -1, -1, -1, -1);
	
	public static ResourceDeposit get1BarCommon()
	{
		return new ResourceDeposit(
			ResourceType.COMMON_ORE,
			YieldRange.LOW,
			100,
			150,
			50,
			20,
			20
		);
	}
	
	public static ResourceDeposit get2BarCommon()
	{
		return new ResourceDeposit(
			ResourceType.COMMON_ORE,
			YieldRange.MEDIUM,
			200,
			300,
			100,
			20,
			20
		);
	}
	
	public static ResourceDeposit get3BarCommon()
	{
		return new ResourceDeposit(
			ResourceType.COMMON_ORE,
			YieldRange.HIGH,
			300,
			450,
			150,
			20,
			20
		);
	}
	
	public static ResourceDeposit get1BarRare()
	{
		return new ResourceDeposit(
			ResourceType.RARE_ORE,
			YieldRange.LOW,
			100,
			150,
			50,
			20,
			20
		);
	}
	
	public static ResourceDeposit get2BarRare()
	{
		return new ResourceDeposit(
			ResourceType.RARE_ORE,
			YieldRange.MEDIUM,
			200,
			300,
			100,
			20,
			20
		);
	}
	
	public static ResourceDeposit get3BarRare()
	{
		return new ResourceDeposit(
			ResourceType.RARE_ORE,
			YieldRange.HIGH,
			300,
			450,
			150,
			20,
			20
		);
	}
	
	public static enum YieldRange
	{
		LOW, MEDIUM, HIGH
	}
	
	private ResourceType type;
	private YieldRange range;
	private int headYield;
	private int middleYield;
	private int tailYield;
	private int headLoads;
	private int middleLoads;
	
	private int loadCount = 0;
	
	private Position pos;
	
	public ResourceDeposit(
		ResourceType type,
		YieldRange range,
		int yield)
	{
		this(type, range, yield, yield, yield, 1, 1);
	}
	
	public ResourceDeposit(
		ResourceType type,
		YieldRange range,
		int headYield,
		int middleYield,
		int tailYield,
		int headLoads,
		int middleLoads)
	{
		this.type = type;
		this.range = range;
		this.headYield = headYield;
		this.middleYield = middleYield;
		this.tailYield = tailYield;
		this.headLoads = headLoads;
		this.middleLoads = middleLoads;
	}
	
	public ResourceDeposit clone()
	{
		return new ResourceDeposit(
			type,
			range,
			headYield,
			middleYield,
			tailYield,
			headLoads,
			middleLoads
		);
	}
	
	public ResourceType getType()
	{
		return type;
	}
	
	public YieldRange getYieldRange()
	{
		return range;
	}
	
	public int getLoadCount()
	{
		return loadCount;
	}
	
	public Cargo getLoad()
	{
		int loadAmount;
		
		if (loadCount < headLoads)
		{
			loadAmount = headYield;
		}
		else if (loadCount < middleLoads + headLoads)
		{
			loadAmount = middleYield;
		}
		else
		{
			loadAmount = tailYield;
		}
		
		loadCount++;
		
		return Cargo.newTruckCargo(type, loadAmount);
	}
	
	public void setPosition(Position pos)
	{
		this.pos = pos;
	}
	
	public Position getPosition()
	{
		return pos;
	}
	
	public boolean isSurveyedBy(Player player)
	{
		return true;
	}
}
