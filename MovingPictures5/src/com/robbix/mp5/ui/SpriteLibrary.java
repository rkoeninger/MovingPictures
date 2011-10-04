package com.robbix.mp5.ui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.AutoArrayList;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.unit.Activity;
import static com.robbix.mp5.unit.Activity.*;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

public class SpriteLibrary
{
	static boolean useSets = false;
	
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
		
		new SpriteSetXMLLoader(sprites, groupInfo, groups, unitSets, ambientSets).load(xmlFile);
		
		loadedModules.add(moduleName);
	}
	
	public void loadModule(String name) throws IOException
	{
		loadModule(new File(rootDir, name));
	}
	
	private Set<String> loadedModules;
	
	private HashMap<String, Sprite> sprites;
	private HashMap<String, Integer> groupInfo;
	private HashMap<String, SpriteGroup> groups;
	
	private List<SpriteSet> unitSets; // indexed by UnitType.serial
	private HashMap<String, SpriteSet> ambientSets; // indexed by eventName
	
	private File rootDir;
	
	public SpriteLibrary()
	{
		loadedModules = new HashSet<String>(64);
		
		sprites = new HashMap<String, Sprite>(2048);
		groups = new HashMap<String, SpriteGroup>(256);
		groupInfo = new HashMap<String, Integer>(256);
		
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
		return unitSets.get(type.getSerial());
	}
	
	public SpriteSet getAmbientSpriteSet(String eventName)
	{
		return ambientSets.get(eventName);
	}
	
	public Point getHotspot(Unit turret)
	{
		return getHotspot(turret, turret.getDirection());
	}
	
	public synchronized Point getHotspot(Unit turret, Direction dir)
	{
		if (!turret.isTurret() && !turret.getType().isGuardPostType())
			throw new IllegalArgumentException("not a turret or guard post");
		
		String parentSpritePath =
			turret.getType().getName() + "/" + TURRET + "/" +
			dir + "/0";
		
		Integer x = groupInfo.get(Utils.getPath(parentSpritePath, "x"));
		Integer y = groupInfo.get(Utils.getPath(parentSpritePath, "y"));
		
		if (x == null || y == null)
		{
			throw new IllegalArgumentException("hotspot not found");
		}
		
		return new Point(x, y);
	}
	
	public synchronized Sprite getDefaultSprite(ResourceDeposit res)
	{
		return getSequence(res).getFirst();
	}
	
	public synchronized Sprite getUnknownDepositSprite()
	{
		SpriteGroup seq = getSequence("aResource/unknown");
		return seq.getSprite(Utils.getTimeBasedIndex(100, seq.getSpriteCount()));
	}
	
	public Sprite getSprite(ResourceDeposit res)
	{
		SpriteGroup seq = getSequence(res);
		return seq.getSprite(Utils.getTimeBasedIndex(100, seq.getSpriteCount()));
	}
	
	public synchronized SpriteGroup getSequence(ResourceDeposit res)
	{
		String resName =
			res.getType() == ResourceType.COMMON_ORE
				? "common"
				: "rare";
		
		resName += res.getYieldRange().ordinal() + 1;
		
		String path = "aResource/" + resName;
		
		return getSequence(path);
	}
	
	public synchronized int getSequenceLength(String path)
	{
		if (!groupInfo.containsKey(path))
		{
			String parentPath = path.substring(0, path.indexOf('/'));

			try
			{
				loadModule(new File(rootDir, parentPath));
			}
			catch (IOException io)
			{
				throw new IllegalArgumentException(path + " does not exist");
			}
		}
		
		Integer frameCount = groupInfo.get(path);
		
		if (frameCount == null)
			throw new IllegalArgumentException(path + " does not exist");
		
		return frameCount;
	}
	
	public synchronized Sprite getSprite(String path)
	{
		Sprite sprite = sprites.get(path);
        
        if (sprite == null)
        {
        	try
	        {
	        	String parentPath = path.substring(0, path.indexOf('/'));
                loadModule(new File(rootDir, parentPath));
                sprite = sprites.get(path);
	        }
	        catch (IOException exc)
	        {
	        	return randomSprite();
	        }
        }
        
        if (sprite != null)
        {
        	groups.put(path, new SpriteGroup(Arrays.asList(sprite)));
        	return sprite;
        }
        
        SpriteGroup seq = getSequence(path);
        
        if (seq == null)
                return null;
        
        return seq.getFirst();
	}
	
	public synchronized SpriteGroup getSequence(String path)
	{
		if (!groupInfo.containsKey(path))
		{
			String parentPath = path.substring(0, path.indexOf('/'));

			try
			{
				loadModule(new File(rootDir, parentPath));
			}
			catch (IOException e)
			{
				// Failover to random sprite
				e.printStackTrace();
				return new SpriteGroup(randomSprite());
			}
		}
		
		Integer frameCount = groupInfo.get(path);
		
		if (frameCount == null)
			throw new IllegalArgumentException(path + " does not exist");
		
		ArrayList<Sprite> spriteList = new ArrayList<Sprite>();
		
		for (int i = 0; i < frameCount; ++i)
			spriteList.add(sprites.get(path + "/" + i));
		
		SpriteGroup group = new SpriteGroup(spriteList);
		groups.put(path, group);
		return group;
	}
	
	public synchronized SpriteGroup getSequence(Unit unit)
	{
		if (useSets)
		{
			SpriteSet set = unitSets.get(unit.getType().getSerial());
			
			if (unit.isStructure())
			{
				return set.get(unit.getActivity());
			}
			else if (unit.isGuardPost())
			{
				return set.get(unit.getActivity());
			}
			else if (unit.isTurret())
			{
				return set.get(unit.getActivity());
			}
			else if (unit.isTruck())
			{
				return set.get(unit.getActivity(), unit.getDirection(), unit.getCargo().getType());
			}
			else // vehicle
			{
				return set.get(unit.getActivity(), unit.getDirection());
			}
		}
		
		if (unit.isStructure()
		|| (unit.getType().isGuardPostType() && unit.getActivity() == BUILD))
		{
			String parentPath = getStructureSequencePath(unit);
			SpriteGroup seq = groups.get(parentPath);
			
			if (seq != null)
				return seq;
			
			return unit.getActivity() == STILL
				? new SpriteGroup(getSprite(parentPath))
				: getSequence(parentPath);
		}
		else
		{
			String parentPath = getVehicleSequencePath(unit);
			SpriteGroup seq = groups.get(parentPath);
			return seq != null ? seq : getSequence(parentPath);
		}
	}
	
	public synchronized Sprite getDefaultSprite(Unit unit)
	{
		return loadUnitSpriteAsNeeded(unit, getDefaultUnitSpritePath(unit));
	}
	
	public synchronized Sprite getSprite(Unit unit)
	{
		return loadUnitSpriteAsNeeded(unit, getUnitSpritePath(unit));
	}
	
	public Sprite getShadow(Unit unit)
	{
		return null;
	}
	
	/*-----------------------------------------------------------------------*
	 * Load-Retry Helpers
	 */
	
	private Sprite loadUnitSpriteAsNeeded(Unit unit, String spritePath)
	{
		Sprite sprite = sprites.get(spritePath);
		
		if (sprite != null)
			return sprite;
		
		try
		{
			if (rootDir != null)
			{
				loadModule(new File(rootDir, unit.getType().getName()));
				
				sprite = sprites.get(spritePath);
				
				if (sprite != null)
					return sprite;
			}
		}
		catch (IOException e)
		{
			// Ignore and failover to blank image
			e.printStackTrace();
		}
		
		return randomSprite(unit.getWidth(), unit.getHeight());
	}

	/*-----------------------------------------------------------------------*
	 * Get Default Sprite Path
	 */
	
	private String getVehicleSequencePath(Unit unit)
	{
		Activity activity = unit.getActivity();
		boolean dependsCargo = unit.isTruck();
		boolean dependsDirection =
			activity == DOCKUP
		 || activity == DOCKDOWN
		 || activity == MINELOAD
		 || activity == CONSTRUCT;
		
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			dependsCargo ? unit.getCargo().getType() : null,
			dependsDirection ? null : unit.getDirection()
		);
	}
	
	private String getStructureSequencePath(Unit unit)
	{
		Activity activity = unit.getActivity();
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			activity == STILL ? unit.getHealthBracket().name().toLowerCase() : null
		);
	}
	
	/*-----------------------------------------------------------------------*
	 * Get Default Sprite Path
	 */
	
	private String getDefaultUnitSpritePath(Unit unit)
	{
		if (unit.isStructure())
		{
			return getDefaultStructureSpritePath(unit);
		}
		else if (unit.getType().isGuardPostType())
		{
			return getDefaultGuardPostSpritePath(unit);
		}
		else
		{
			return getDefaultVehicleSpritePath(unit);
		}
	}
	
	private String getDefaultGuardPostSpritePath(Unit unit)
	{
		return Utils.getPath(
			unit.getType().getName(),
			TURRET,
			Direction.E,
			0
		);
	}
	
	private String getDefaultStructureSpritePath(Unit unit)
	{
		return Utils.getPath(
			unit.getType().getName(),
			STILL,
			"green"
		);
	}
	
	private String getDefaultVehicleSpritePath(Unit unit)
	{
		String parentPath = Utils.getPath(
			unit.getType().getName(),
			MOVE,
			unit.isTruck() ? Cargo.EMPTY.getType() : null,
			Direction.E
		);
		
		Integer frameCount = groupInfo.get(parentPath);
		
		if (frameCount == null)
			frameCount = 1;
		
		return Utils.getPath(
			parentPath,
			unit.getAnimationFrame() % frameCount
		);
	}
	
	/*-----------------------------------------------------------------------*
	 * Get Sprite Path
	 */
	
	private String getUnitSpritePath(Unit unit)
	{
		if (unit.isStructure()
		|| (unit.getType().isGuardPostType() && unit.getActivity() == BUILD))
		{
			return getStructureSpritePath(unit);
		}
		else
		{
			return getVehicleSpritePath(unit);
		}
	}
	
	private String getStructureSpritePath(Unit unit)
	{
		Activity activity = unit.getActivity();
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			activity == STILL ? unit.getHealthBracket().name().toLowerCase() : null,
			activity == BUILD ? unit.getAnimationFrame() : null
		);
	}
	
	private String getVehicleSpritePath(Unit unit)
	{
		Activity activity = unit.getActivity();
		String parentPath;
		boolean dependsCargo = unit.isTruck();
		boolean constDirection =
			activity == DOCKUP
		 || activity == DOCKDOWN
		 || activity == MINELOAD
		 || activity == CONSTRUCT;
		
		parentPath = Utils.getPath(
			unit.getType().getName(),
			activity,
			dependsCargo ? unit.getCargo().getType() : null,
			constDirection ? null : unit.getDirection()
		);
		
		Integer frameCount = groupInfo.get(parentPath);
		
		if (frameCount == null)
			return parentPath;
		
		return Utils.getPath(
			parentPath,
			unit.getAnimationFrame() % frameCount
		);
	}
	
	private static Sprite randomSprite()
	{
		return randomSprite(1, 1);
	}
	
	private static Sprite randomSprite(int w, int h)
	{
		// This was changed so sprite load errors
		// are fail-fast and not obscured
		
		// This would also be a good spot for a break point!
		throw new Error();
		
//		return new Sprite(Utils.getBlankImage(
//			Utils.getRandomPrimaryColor(), w * 32, h * 32),0,0,0);
	}
}
