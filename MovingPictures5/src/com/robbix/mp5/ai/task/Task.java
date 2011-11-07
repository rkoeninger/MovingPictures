package com.robbix.mp5.ai.task;

import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Filter;

public abstract class Task
{
	public static final Filter<Unit> VEHICLE_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.getType().isVehicleType();
		}
	};

	public static final Filter<Unit> STRUCTURE_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.isStructure();
		}
	};

	public static final Filter<Unit> STRUCTURE_OR_GUARDPOST = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.isStructure() || unit.getType().isGuardPostType();
		}
	};
	
	public static final Filter<Unit> TRUCK_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.isTruck();
		}
	};
	
	public static final Filter<Unit> DOCKABLE = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			String typeName = unit.getType().getName();
			
			return typeName.contains("Truck") || typeName.contains("ConVec");
		}
	};
	
	public static final Filter<Unit> TURRET_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.isTurret() || unit.getType().isGuardPostType();
		}
	};
	
	public static final Filter<Unit> HAS_TURRET_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.hasTurret();
		}
	};

	public static final Filter<Unit> MINER_ONLY = new Filter<Unit>()
	{
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return unit.isMiner();
		}
	};
	
	private boolean interruptible;
	private Filter<Unit> unitFilter;
	
	public Task(boolean interruptible, Filter<Unit> unitFilter)
	{
		this.interruptible = interruptible;
		this.unitFilter = unitFilter;
	}
	
	public boolean isInterruptible()
	{
		return interruptible;
	}
	
	public boolean isAcceptable(Unit unit)
	{
		return unitFilter.accept(unit);
	}
	
	public abstract void step(Unit unit);
}
