package com.robbix.mp5.ui;

import static com.robbix.mp5.unit.Activity.TURRET;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Modular;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.AutoArrayList;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

/**
 * Async module loading is not enabled by default.
 */
public class SpriteLibrary implements Modular
{
	public static SpriteLibrary load(File rootDir, boolean lazy) throws IOException
	{
		return lazy ? loadLazy(rootDir) : preload(rootDir);
	}
	
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
	
	private List<SpriteSet> unitSets; // indexed by UnitType.serial
	private HashMap<String, SpriteSet> ambientSets; // indexed by eventName
	
	private File rootDir;
	
	private Set<String> modulesBeingLoaded;
	private Set<String> loadedModules;
	private ModuleListener.Helper listenerHelper;
	
	private boolean asyncMode = false;
	private Object asyncLock = new Object();
	private AsyncLoader loader;
	
	public SpriteLibrary()
	{
		loadedModules = new HashSet<String>(64);
		modulesBeingLoaded = new HashSet<String>(64);
		unitSets = new AutoArrayList<SpriteSet>();
		ambientSets = new HashMap<String, SpriteSet>(256);
		listenerHelper = new ModuleListener.Helper();
		loader = new AsyncLoader();
	}
	
	public void setAsyncModeEnabled(boolean asyncMode)
	{
		this.asyncMode = asyncMode;
	}
	
	public boolean isAsyncModeEnabled()
	{
		return asyncMode;
	}
	
	public void addModuleListener(ModuleListener listener)
	{
		listenerHelper.add(listener);
	}
	
	public void removeModuleListener(ModuleListener listener)
	{
		listenerHelper.remove(listener);
	}
	
	public void loadModuleSync(String moduleName) throws IOException
	{
		loadModuleSync(new File(rootDir, moduleName));
	}
	
	public void loadModuleSync(File xmlFile) throws IOException
	{
		String moduleName = 
			xmlFile.isDirectory()
			? xmlFile.getName()
			: xmlFile.getParentFile().getName();
		
		if (loadedModules.contains(moduleName)
		 || modulesBeingLoaded.contains(moduleName))
			return;
		
		SpriteSet set = new SpriteSetXMLLoader(xmlFile).load();
		Class<?>[] params = set.getParameterList();
		
		if (params.length == 1 && params[0].equals(String.class))
		{
			ambientSets.put(set.getName(), set);
		}
		else
		{
			UnitType type = Mediator.factory.getType(set.getName());
			unitSets.set(type.getSerial(), set);
		}
		
		loadedModules.add(set.getName());
		listenerHelper.fireModuleLoaded(new ModuleEvent(this, set.getName()));
	}
	
	public void loadModuleAsync(String name) throws IOException
	{
		loadModuleAsync(new File(rootDir, name));
	}
	
	public void loadModuleAsync(File xmlFile) throws IOException
	{
		String moduleName = 
			xmlFile.isDirectory()
			? xmlFile.getName()
			: xmlFile.getParentFile().getName();
		
		synchronized (asyncLock)
		{
			if (loadedModules.contains(moduleName)
			 || modulesBeingLoaded.contains(moduleName))
				return;
			
			AsyncLoader.Callback callback = new AsyncLoader.Callback()
			{
				public void loadFailed(File file, Exception exc)
				{
				}
				
				public void loadComplete(SpriteSet set)
				{
					synchronized (asyncLock)
					{
						Class<?>[] params = set.getParameterList();
						
						if (params.length == 1 && params[0].equals(String.class))
						{
							ambientSets.put(set.getName(), set);
						}
						else
						{
							UnitType type = Mediator.factory.getType(set.getName());
							unitSets.set(type.getSerial(), set);
						}
						
						modulesBeingLoaded.remove(set.getName());
						loadedModules.add(set.getName());
						listenerHelper.fireModuleLoaded(new ModuleEvent(this, set.getName()));
					}
				}
			};
			
			modulesBeingLoaded.add(moduleName);
			loader.load(xmlFile, callback);
		}
	}
	
	public void loadModule(String name) throws IOException
	{
		loadModule(new File(rootDir, name));
	}
	
	public void loadModule(File xmlFile) throws IOException
	{
		if (asyncMode)
		{
			loadModuleAsync(xmlFile);
		}
		else
		{
			loadModuleSync(xmlFile);
		}
	}
	
	public boolean isLoaded(String module)
	{
		synchronized (asyncLock)
		{
			return loadedModules.contains(module);
		}
	}
	
	public boolean isBeingLoaded(String module)
	{
		synchronized (asyncLock)
		{
			return modulesBeingLoaded.contains(module);
		}
	}
	
	public boolean unloadModule(String name)
	{
		synchronized (asyncLock)
		{
			if (ambientSets.containsKey(name))
			{
				ambientSets.remove(name);
				loadedModules.remove(name);
				listenerHelper.fireModuleUnloaded(new ModuleEvent(this, name));
				return true;
			}
			
			UnitType type = Mediator.factory.getType(name);
			
			if (type != null)
			{
				unitSets.set(type.getSerial(), null);
				loadedModules.remove(name);
				listenerHelper.fireModuleUnloaded(new ModuleEvent(this, name));
				return true;
			}
			
			return false;
		}
	}
	
	public Set<String> getLoadedModules()
	{
		synchronized (asyncLock)
		{
			Set<String> modules = getLoadedUnitModules();
			modules.addAll(getLoadedAmbientModules());
			return modules;
		}
	}
	
	public Set<String> getLoadedUnitModules()
	{
		synchronized (asyncLock)
		{
			Set<String> moduleNames = new HashSet<String>();
			
			for (int i = 0; i < unitSets.size(); ++i)
				if (unitSets.get(i) != null)
					moduleNames.add(unitSets.get(i).getName());
			
			return moduleNames;
		}
	}
	
	public Set<String> getLoadedAmbientModules()
	{
		synchronized (asyncLock)
		{
			return ambientSets.keySet();
		}
	}
	
	public SpriteSet getSpriteSet(String name)
	{
		synchronized (asyncLock)
		{
			if (ambientSets.containsKey(name))
				return ambientSets.get(name);
			
			UnitType type = Mediator.factory.getType(name);
			
			if (type == null)
				return null;
			
			return unitSets.get(type.getSerial());
		}
	}
	
	public SpriteSet getUnitSpriteSet(UnitType type)
	{
		synchronized (asyncLock)
		{
			if (unitSets.get(type.getSerial()) == null)
			{
				if (isBeingLoaded(type.getName()))
				{
					return SpriteSet.BLANK;
				}
				
				try
				{
					loadModule(type.getName());
					return SpriteSet.BLANK;
				}
				catch (IOException ioe)
				{
					throw new Error(ioe);
				}
			}
			
			return unitSets.get(type.getSerial());
		}
	}
	
	public SpriteSet getAmbientSpriteSet(String eventName)
	{
		synchronized (asyncLock)
		{
			if (! ambientSets.containsKey(eventName))
			{
				if (isBeingLoaded(eventName))
				{
					return SpriteSet.BLANK;
				}
				
				try
				{
					loadModule(eventName);
					return SpriteSet.BLANK;
				}
				catch (IOException ioe)
				{
					throw new Error(ioe);
				}
			}
			
			return ambientSets.get(eventName);
		}
	}
	
	public Point2D getHotspot(Unit turret)
	{
		return getHotspot(turret.getType(), turret.getDirection());
	}
	
	public Point2D getHotspot(UnitType turretType, Direction dir)
	{
		SpriteSet set = getUnitSpriteSet(turretType);
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
		return getAmbientSpriteGroup("aResource", res.toString());
	}
	
	public Sprite getSprite(LayeredMap.Fixture fixture)
	{
		switch (fixture)
		{
		case TUBE:          return getSprite("oTerrainFixture", "tube");
		case WALL:          return getSprite("oTerrainFixture", "wall");
		case GEYSER:        return getSprite("aGeyser",         "geyser");
		case MINE_PLATFORM: return getSprite("aCommonMine",     "platform");
		}
		
		throw new IllegalArgumentException("invalid fixture " + fixture);
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
	
	public int getBuildGroupLength(UnitType unitType)
	{
		if (!unitType.isStructureType() && !unitType.isGuardPostType())
			throw new IllegalArgumentException(unitType + " has no build sequence");
		
		SpriteSet set = getUnitSpriteSet(unitType);
		SpriteGroup buildGroup = set.get(Activity.BUILD);
		return buildGroup.getFrameCount();
	}
	
	public int getDumpGroupLength(Unit unit)
	{
		if (!unit.isTruck())
			throw new IllegalArgumentException("Must be truck");
		
		SpriteSet set = getUnitSpriteSet(unit.getType());
		SpriteGroup dumpGroup = set.get(unit.getCargo().getType(), Activity.DUMP, unit.getDirection());
		return dumpGroup.getFrameCount();
	}
	
	public Sprite getDefaultSprite(UnitType unitType)
	{
		SpriteSet set = getUnitSpriteSet(unitType);
		Object[] spriteArgs = unitType.getDefaultSpriteArgs();
		return spriteArgs == null ? null : set.get(spriteArgs).getFirst();
	}
	
	public Sprite getDefaultAmbientSprite(String setName)
	{
		SpriteSet set = getAmbientSpriteSet(setName);
		SpriteGroup group = set.get(set.getArgumentList()[0]);
		return group.getFirst();
	}
	
	public Sprite getDefaultSprite(Unit unit)
	{
		return getDefaultSprite(unit.getType());
	}
	
	/**
	 * Does not recolor for owner.
	 */
	public Sprite getSprite(Unit unit)
	{
		SpriteSet set = getUnitSpriteSet(unit.getType());
		SpriteGroup group = set.get(unit.getSpriteArgs());
		
		if (group instanceof EnumSpriteGroup)
		{
			EnumSpriteGroup<?> enumGroup = (EnumSpriteGroup<?>) group;
			Class<?> enumClass = enumGroup.getEnumType();
			
			if (enumClass.equals(Direction.class))
			{
				return enumGroup.getFrame(unit.getDirection().ordinal());
			}
			else if (enumClass.equals(HealthBracket.class))
			{
				return enumGroup.getFrame(unit.getHealthBracket().ordinal());
			}
			else
			{
				throw new Error("Unsupported enum type" + enumClass);
			}
		}
		else
		{
			return group.getFrame(unit.getAnimationFrame()
				% group.getFrameCount());
			//FIXME: temporary, group looping should take care of looping
			//
			//         - linear sequences are running one,two frame over.
		}
	}
	
	public Sprite getShadow(Unit unit)
	{
		return null;
	}
}
