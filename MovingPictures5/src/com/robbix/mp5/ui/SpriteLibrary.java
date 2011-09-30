package com.robbix.mp5.ui;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;

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
		
		new SpriteSetXMLLoader(sprites, groupInfo).load(xmlFile);
		
		loadedModules.add(moduleName);
	}
	
	public void loadModule(String name) throws IOException
	{
		loadModule(new File(rootDir, name));
	}
	
	private Set<String> loadedModules;
	
	private HashMap<String, Sprite> sprites;
	private HashMap<String, Integer> groupInfo;
	private HashMap<String, List<Sprite>> groups;
	
	private File rootDir;
	
	public SpriteLibrary()
	{
		loadedModules = new HashSet<String>(64);
		
		sprites = new HashMap<String, Sprite>(2048);
		groups = new HashMap<String, List<Sprite>>(256);
		groupInfo = new HashMap<String, Integer>(256);
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
			turret.getType().getName() + "/turret/" +
			dir.getShortName() + "/0";
		
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
		return getSequence(res).get(0);
	}
	
	public synchronized Sprite getUnknownDepositSprite()
	{
		List<Sprite> seq = getSequence("aResource/unknown");
		return seq.get(Utils.getTimeBasedIndex(100, seq.size()));
	}
	
	public Sprite getSprite(ResourceDeposit res)
	{
		List<Sprite> seq = getSequence(res);
		return seq.get(Utils.getTimeBasedIndex(100, seq.size()));
	}
	
	public synchronized List<Sprite> getSequence(ResourceDeposit res)
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
        	groups.put(path, Arrays.asList(sprite));
        	return sprite;
        }
        
        List<Sprite> seq = getSequence(path);
        
        if (seq == null)
                return null;
        
        return seq.get(0);
	}
	
	public synchronized List<Sprite> getSequence(String path)
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
				return Collections.singletonList(randomSprite());
			}
		}
		
		Integer frameCount = groupInfo.get(path);
		
		if (frameCount == null)
			throw new IllegalArgumentException(path + " does not exist");
		
		ArrayList<Sprite> spriteList = new ArrayList<Sprite>();
		
		for (int i = 0; i < frameCount; ++i)
			spriteList.add(sprites.get(path + "/" + i));
		
		groups.put(path, spriteList);
		return spriteList;
	}
	
	public synchronized List<Sprite> getSequence(Unit unit)
	{
		if (unit.isStructure()
		|| (unit.getType().isGuardPostType() && unit.getActivity().equals("build")))
		{
			String parentPath = getStructureSequencePath(unit);
			List<Sprite> seq = groups.get(parentPath);
			
			if (seq != null)
				return seq;
			
			return unit.getActivity().equals("still")
				? Arrays.asList(getSprite(parentPath))
				: getSequence(parentPath);
		}
		else
		{
			String parentPath = getVehicleSequencePath(unit);
			List<Sprite> seq = groups.get(parentPath);
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
		String activity = unit.getActivity();
		String dir = unit.getDirection().getShortName();
		boolean dependsCargo = unit.isTruck();
		boolean dependsDirection =
			activity.contains("dock")
		 || activity.contains("mine")
		 || activity.contains("construct");
		
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			dependsCargo ? unit.getCargo().getType() : null,
			dependsDirection ? null : dir
		);
	}
	
	private String getStructureSequencePath(Unit unit)
	{
		String activity = unit.getActivity();
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			activity.equals("still") ? unit.getHealthBracket().name().toLowerCase() : null
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
			"turret",
			Direction.E,
			0
		);
	}
	
	private String getDefaultStructureSpritePath(Unit unit)
	{
		return Utils.getPath(
			unit.getType().getName(),
			"still",
			"green"
		);
	}
	
	private String getDefaultVehicleSpritePath(Unit unit)
	{
		String parentPath = Utils.getPath(
			unit.getType().getName(),
			"move",
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
		|| (unit.getType().isGuardPostType() && unit.getActivity().equals("build")))
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
		String activity = unit.getActivity();
		return Utils.getPath(
			unit.getType().getName(),
			activity,
			activity.equals("still") ? unit.getHealthBracket().name().toLowerCase() : null,
			activity.equals("build") ? unit.getAnimationFrame() : null
		);
	}
	
	private String getVehicleSpritePath(Unit unit)
	{
		String activity = unit.getActivity();
		String parentPath;
		String dir = unit.getDirection().getShortName();
		
		boolean dependsCargo = unit.isTruck();
		
		boolean dependsDirection =
			activity.contains("dock")
		 || activity.contains("mine")
		 || activity.contains("construct");
		
		parentPath = Utils.getPath(
			unit.getType().getName(),
			activity,
			dependsCargo ? unit.getCargo().getType() : null,
			dependsDirection ? null : dir
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
