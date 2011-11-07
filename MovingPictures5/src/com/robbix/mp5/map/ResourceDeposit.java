package com.robbix.mp5.map;

import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.utils.Position;

public class ResourceDeposit implements Cloneable
{
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
	
	public boolean isCommon()
	{
		return type == ResourceType.COMMON_ORE;
	}
	
	public boolean isRare()
	{
		return type == ResourceType.RARE_ORE;
	}
	
	public YieldRange getYieldRange()
	{
		return range;
	}
	
	public boolean isLow()
	{
		return range == YieldRange.LOW;
	}
	
	public boolean isMedium()
	{
		return range == YieldRange.MEDIUM;
	}
	
	public boolean isHigh()
	{
		return range == YieldRange.HIGH;
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
	
	public String toString()
	{
		String resName = type == ResourceType.COMMON_ORE
			? "common"
			: "rare";
		return resName + (range.ordinal() + 1);
	}
}
