package com.robbix.mp5.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.Cargo;
import com.robbix.utils.Direction;
import com.robbix.utils.RImage;

public abstract class SpriteSet
{
	public static final Sprite BLANK_SPRITE = new Sprite(new RImage(32, 32, new Color(0, 0, 0, 0)), 0, 0);
	
	public static final SpriteGroup BLANK_GROUP = new SpriteGroup(BLANK_SPRITE, true)
	{
		public Sprite getSprite(int index)
		{
			return getFrame(0);
		}
	};
	
	public static final SpriteSet BLANK = new SpriteSet("Blank")
	{
		public Class<?>[] getParameterList()
		{
			return new Class<?>[0];
		}
		
		public Object[] getArgumentList()
		{
			return new Object[0];
		}
		
		public void set(SpriteGroup group, Object... args)
		{
		}
		
		public SpriteGroup get(Object... args)
		{
			return BLANK_GROUP;
		}
	};
	
	private String name;
	
	protected SpriteSet(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public abstract Class<?>[] getParameterList();
	public abstract Object[] getArgumentList();
	public abstract void set(SpriteGroup group, Object... args);
	public abstract SpriteGroup get(Object... args);
	
	public static SpriteSet forAmbient(String name)
	{
		return new SpriteSetImpl_S(name);
	}
	
	public static SpriteSet forTrucks(String name)
	{
		return new SpriteSetImpl_ADC(name);
	}
	
	public static SpriteSet forVehicles(String name)
	{
		return new SpriteSetImpl_AD(name);
	}
	
	public static SpriteSet forTurrets(String name)
	{
		return new SpriteSetImpl_A(name);
	}
	
	public static SpriteSet forGuardPosts(String name)
	{
		return new SpriteSetImpl_A(name);
	}
	
	public static SpriteSet forStructures(String name)
	{
		return new SpriteSetImpl_A(name);
	}
	
	public static SpriteSet forParams(String name, Class<?>... argTypes)
	{
		boolean act = has(argTypes, Activity.class);
		boolean dir = has(argTypes, Direction.class);
		boolean cgo = has(argTypes, Cargo.Type.class);
		boolean str = has(argTypes, String.class);
		
		if (!act && !dir && !cgo && str) // ambient
		{
			return new SpriteSetImpl_S(name);
		}
		else if (act && dir && cgo && !str) // trucks
		{
			return new SpriteSetImpl_ADC(name);
		}
		else if (act && dir && !cgo && !str) // most units and guardposts
		{
			return new SpriteSetImpl_AD(name);
		}
		else if (act && !dir && !cgo && !str) // structures, turrets
		{
			return new SpriteSetImpl_A(name);
		}
		
		throw new IllegalArgumentException(
			"No SpriteSet configuration available for argTypes");
	}
	
	private static boolean has(Class<?>[] argTypes, Class<?> type)
	{
		for (Class<?> argType : argTypes)
			if (argType.equals(type))
				return true;
		
		return false;
	}
	
	private static class SpriteSetImpl_A extends SpriteSet
	{
		SpriteGroup[] groups;
		
		public SpriteSetImpl_A(String name)
		{
			super(name);
			groups = new SpriteGroup
				[Activity.values().length];
		}
		
		public Class<?>[] getParameterList()
		{
			return new Class<?>[]{Activity.class};
		}
		
		public Object[] getArgumentList()
		{
			List<Object> keys = new ArrayList<Object>();
			
			for (Activity act : Activity.values())
				if (get(act) != null)
				{
					keys.add(new Object[]{act});
				}
			
			return keys.toArray();
		}
		
		public void set(SpriteGroup group, Object... args)
		{
			checkArgCount(args, 1);
			set(group, getAct(args));
		}
		
		public SpriteGroup get(Object... args)
		{
			checkArgCount(args, 1);
			return get(getAct(args));
		}
		
		public void set(SpriteGroup group, Activity act)
		{
			groups[act.ordinal()] = group;
		}
		
		public SpriteGroup get(Activity act)
		{
			return groups[act.ordinal()];
		}
	}
	
	private static class SpriteSetImpl_AD extends SpriteSet
	{
		private SpriteGroup[][] groups;
		
		public SpriteSetImpl_AD(String name)
		{
			super(name);
			groups = new SpriteGroup
				[Activity.values().length]
				[Direction.values().length];
		}
		
		public Class<?>[] getParameterList()
		{
			return new Class<?>[]{Activity.class, Direction.class};
		}
		
		public Object[] getArgumentList()
		{
			List<Object> keys = new ArrayList<Object>();
			
			for (Activity act  : Activity.values())
			for (Direction dir : Direction.values())
				if (get(act, dir) != null)
				{
					keys.add(new Object[]{act, dir});
				}
			
			return keys.toArray();
		}
		
		public void set(SpriteGroup group, Object... args)
		{
			checkArgCount(args, 2);
			set(group, getAct(args), getDir(args));
		}
		
		public SpriteGroup get(Object... args)
		{
			checkArgCount(args, 2);
			return get(getAct(args), getDir(args));
		}
		
		public void set(SpriteGroup group, Activity act, Direction dir)
		{
			groups[act.ordinal()][dir.ordinal()] = group;
		}
		
		public SpriteGroup get(Activity act, Direction dir)
		{
			return groups[act.ordinal()][dir.ordinal()];
		}
	}
	
	private static class SpriteSetImpl_ADC extends SpriteSet
	{
		private SpriteGroup[][][] groups;
		
		public SpriteSetImpl_ADC(String name)
		{
			super(name);
			groups = new SpriteGroup
				[Cargo.Type.values().length]
				[Activity.values().length]
				[Direction.values().length];
		}
		
		public Class<?>[] getParameterList()
		{
			return new Class<?>[]{Cargo.Type.class, Activity.class, Direction.class};
		}
		
		public Object[] getArgumentList()
		{
			List<Object> keys = new ArrayList<Object>();

			for (Cargo.Type cargo : Cargo.Type.values())
			for (Activity act     : Activity.values())
			for (Direction dir    : Direction.values())
				if (get(cargo, act, dir) != null)
				{
					keys.add(new Object[]{cargo, act, dir});
				}
			
			return keys.toArray();
		}
		
		public void set(SpriteGroup group, Object... args)
		{
			checkArgCount(args, 3);
			set(group, getCargo(args), getAct(args), getDir(args));
		}
		
		public SpriteGroup get(Object... args)
		{
			checkArgCount(args, 3);
			return get(getCargo(args), getAct(args), getDir(args));
		}
		
		public void set(SpriteGroup group, Cargo.Type cargo, Activity act, Direction dir)
		{
			groups[cargo.ordinal()][act.ordinal()][dir.ordinal()] = group;
		}
		
		public SpriteGroup get(Cargo.Type cargo, Activity act, Direction dir)
		{
			return groups[cargo.ordinal()][act.ordinal()][dir.ordinal()];
		}
	}
	
	private static class SpriteSetImpl_S extends SpriteSet
	{
		Map<String, SpriteGroup> groups;
		
		public SpriteSetImpl_S(String name)
		{
			super(name);
			groups = new HashMap<String, SpriteGroup>();
		}
		
		public Class<?>[] getParameterList()
		{
			return new Class<?>[]{String.class};
		}
		
		public Object[] getArgumentList()
		{
			return groups.keySet().toArray();
		}
		
		public void set(SpriteGroup group, Object... args)
		{
			checkArgCount(args, 1);
			set(group, getStr(args));
		}
		
		public SpriteGroup get(Object... args)
		{
			checkArgCount(args, 1);
			return get(getStr(args));
		}
		
		public void set(SpriteGroup group, String str)
		{
			groups.put(str, group);
		}
		
		public SpriteGroup get(String str)
		{
			return groups.get(str);
		}
	}
	
	private static void checkArgCount(Object[] args, int count)
	{
		if (args.length != count)
			throw new IllegalArgumentException("Wrong number of args");
	}
	
	private static Activity getAct(Object... args)
	{
		for (Object arg : args)
			if (arg instanceof Activity)
				return (Activity) arg;
		
		throw new IllegalArgumentException("No Activity in arg list");
	}
	
	private static Direction getDir(Object... args)
	{
		for (Object arg : args)
			if (arg instanceof Direction)
				return (Direction) arg;
		
		throw new IllegalArgumentException("No Direction in arg list");
	}
	
	private static Cargo.Type getCargo(Object... args)
	{
		for (Object arg : args)
			if (arg instanceof Cargo.Type)
				return (Cargo.Type) arg;
		
		throw new IllegalArgumentException("No Cargo.Type in arg list");
	}
	
	private static String getStr(Object... args)
	{
		for (Object arg : args)
			if (arg instanceof String)
				return (String) arg;
		
		throw new IllegalArgumentException("No String in arg list");
	}
}