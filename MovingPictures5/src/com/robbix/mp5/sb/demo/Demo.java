package com.robbix.mp5.sb.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.UnitFactory;

public abstract class Demo
{
	public static Map<String, Demo> getDemos()
	{
		try
		{
			Map<String, Demo> demos = new HashMap<String, Demo>();
			
			for (Class<?> clazz : getSubclasses(Demo.class))
			{
				Demo demo = (Demo) clazz.newInstance();
				demos.put(demo.getName(), demo);
			}
			
			return demos;
		}
		catch (Exception exc)
		{
			throw new Error("Could not load Demos", exc);
		}
	}
	
	private String mapName;
	private String[] spriteModules;
	private String[] soundModules;
	private Player[] players;
	private int startingPlayerID;
	
	/**
	 * Resources specified for preload will be loaded syncrhonously:
	 * they will be ready when the DisplayPanel is first shown.
	 */
	public Demo(
		String mapName,
		String[] spriteModules,
		String[] soundModules,
		Player[] players,
		int startingPlayerID)
	{
		this.mapName = mapName;
		this.startingPlayerID = startingPlayerID;
		this.spriteModules = spriteModules == null ? new String[0] : spriteModules;
		this.soundModules  = soundModules  == null ? new String[0] : soundModules;
		this.players       = players       == null ? new Player[0] : players;
	}
	
	public String getName()
	{
		return getClass().getSimpleName();
	}
	
	public String getMapName()
	{
		return mapName;
	}
	
	public String[] getRequiredSpriteModuleNames()
	{
		return spriteModules;
	}
	
	public String[] getRequiredSoundModulesNames()
	{
		return soundModules;
	}
	
	public Player[] getPlayers()
	{
		return players;
	}
	
	public int getStartingPlayerID()
	{
		return startingPlayerID;
	}
	
	public void setup(Game game) throws IOException
	{
		SpriteLibrary sprites = game.getSpriteLibrary();
		SoundBank sounds = game.getSoundBank();
		
		for (String spriteModule : spriteModules)
			sprites.loadModuleSync(spriteModule);
		
		for (String soundModule : soundModules)
			sounds.loadModule(soundModule);
		
		for (Player player : players)
			game.addPlayer(player);
		
		placeUnits(game.getMap(), game.getUnitFactory());
	}
	
	protected abstract void placeUnits(LayeredMap map, UnitFactory factory);
	
	/**
	 * Returns all subclasses of the given superclass in it's package.
	 * 
	 * This method probably has a variety of vulnerabilites as it's limited
	 * to searching the filesystem for .class files. Reflection of pacakages
	 * to look for classes is not possible in Java.
	 */
	private static List<Class<?>> getSubclasses(Class<?> superclass)
	throws ClassNotFoundException, IOException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		if (classLoader == null)
			classLoader = superclass.getClassLoader();
		
		String packageName = superclass.getPackage().getName();
		String packagePath = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(packagePath);
		List<File> dirs = new ArrayList<File>();
		
		while (resources.hasMoreElements())
		{
			URL resource = resources.nextElement();
			String filePath = resource.getFile().replace("%20", " ");
			dirs.add(new File(filePath));
		}
		
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		for (File directory : dirs)
		{
			if (!directory.exists())
				continue;
			
			File[] files = directory.listFiles();
			
			for (File file : files)
			if (file.isFile() && file.getName().endsWith(".class"))
			{
				int suffixIndex = file.getName().length() - 6;
				String trimmedFilename = file.getName().substring(0, suffixIndex);
				String className = packageName + '.' + trimmedFilename;
				Class<?> clazz = Class.forName(className);
				
				if (superclass.isAssignableFrom(clazz)
				&& !clazz.equals(superclass))
					classes.add(clazz);
			}
		}
		
		return classes;
	}
}
