package com.robbix.mp5.ui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.AutoArrayList;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.ResourceDeposit;
import static com.robbix.mp5.unit.Activity.*;

import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

public class SpriteLibrary
{
	public static SpriteLibrary loadLazy(File rootDir)
	{
		SpriteLibrary library = new SpriteLibrary();
		library.rootDir = rootDir;
		return library;
	}
	
	public static SpriteLibrary preload(File rootDir) throws IOException
	{
		SpriteLibrary library = new SpriteLibrary();
		library.rootDir = rootDir;
		
		File[] dirs = rootDir.listFiles();
		
		for (File dir : dirs) // For each directory under the spriteset dir
		{
			File infoFile = new File(dir, "info.xml"); // Load xml file in each
			
			if (!infoFile.exists())
				continue;
			
			library.loadModule(infoFile);
		}
		
		return library;
	}
	
	public void loadModule(File xmlFile) throws IOException
	{
		String moduleName = 
			xmlFile.isDirectory()
			? xmlFile.getName()
			: xmlFile.getParentFile().getName();
		
		if (loadedModules.contains(moduleName))
			return;
		
		new SpriteSetXMLLoader(unitSets, ambientSets).load(xmlFile);
		
		loadedModules.add(moduleName);
	}
	
	public void loadModule(String name) throws IOException
	{
		loadModule(new File(rootDir, name));
	}
	
	private Set<String> loadedModules;
	
	private List<SpriteSet> unitSets; // indexed by UnitType.serial
	private HashMap<String, SpriteSet> ambientSets; // indexed by eventName
	
	private File rootDir;
	
	public SpriteLibrary()
	{
		loadedModules = new HashSet<String>(64);
		unitSets = new AutoArrayList<SpriteSet>();
		ambientSets = new HashMap<String, SpriteSet>(256);
	}
	
	public Set<String> getLoadedUnitModules()
	{
		Set<String> moduleNames = new HashSet<String>();
		
		for (int i = 0; i < unitSets.size(); ++i)
			if (unitSets.get(i) != null)
				moduleNames.add(unitSets.get(i).getName());
		
		return moduleNames;
	}
	
	public Set<String> getLoadedAmbientModules()
	{
		return ambientSets.keySet();
	}
	
	public SpriteSet getUnitSpriteSet(UnitType type)
	{
		if (unitSets.get(type.getSerial()) == null)
		{
			try
			{
				loadModule(type.getName());
			}
			catch (IOException ioe)
			{
				throw new Error(ioe);
			}
		}
		
		return unitSets.get(type.getSerial());
	}
	
	public SpriteSet getAmbientSpriteSet(String eventName)
	{
		if (! ambientSets.containsKey(eventName))
		{
			try
			{
				loadModule(eventName);
			}
			catch (IOException ioe)
			{
				throw new Error(ioe);
			}
		}
		
		return ambientSets.get(eventName);
	}
	
	public Point getHotspot(Unit turret)
	{
		return getHotspot(turret, turret.getDirection());
	}
	
	public Point getHotspot(Unit turret, Direction dir)
	{
		SpriteSet set = getUnitSpriteSet(turret.getType());
		SpriteGroup group = set.get(TURRET);
		TurretSprite sprite = (TurretSprite) group.getSprite(dir.ordinal());
		return sprite.getHotspot();
	}
	
	public Sprite getDefaultSprite(ResourceDeposit res)
	{
		return getSpriteGroup(res).getFirst();
	}
	
	public Sprite getUnknownDepositSprite()
	{
		SpriteGroup seq = getAmbientSpriteGroup("aResource", "unknown");
		return seq.getSprite(Utils.getTimeBasedIndex(100, seq.getSpriteCount()));
	}
	
	public Sprite getSprite(ResourceDeposit res)
	{
		SpriteGroup seq = getSpriteGroup(res);
		return seq.getSprite(Utils.getTimeBasedIndex(100, seq.getSpriteCount()));
	}
	
	public SpriteGroup getSpriteGroup(ResourceDeposit res)
	{
		String resName = res.getType() == ResourceType.COMMON_ORE
			? "common"
			: "rare";
		resName += (res.getYieldRange().ordinal() + 1);
		return getAmbientSpriteGroup("aResource", resName);
	}
	
	public Sprite getSprite(String setName, String eventName)
	{
		SpriteGroup group = getAmbientSpriteGroup(setName, eventName);
		
		if (group == null)
			return null;
		
		return group.getFirst();
	}
	
	public SpriteGroup getAmbientSpriteGroup(String setName, String eventName)
	{
		return getAmbientSpriteSet(setName).get(eventName);
	}
	
	public Sprite getDefaultSprite(Unit unit)
	{
		SpriteSet set = getUnitSpriteSet(unit.getType());
		
		int argCount = 1;
		
		if (unit.isTruck())
		{
			argCount = 3;
		}
		else if (unit.getType().getFootprint() == Footprint.VEHICLE)
		{
			argCount = 2;
		}
		else
		{
			argCount = 1;
		}
		
		Object[] spriteArgs = new Object[argCount];
		spriteArgs[0] = unit.getActivity();
		
		if (unit.getType().getFootprint() == Footprint.VEHICLE)
		{
			spriteArgs[1] = unit.getDirection();
			
			if (unit.isTruck())
			{
				spriteArgs[2] = unit.getCargo().getType();
			}
		}
		
		return set.get(spriteArgs).getFirst();
	}
	
	public Sprite getShadow(Unit unit)
	{
		return null;
	}
}
