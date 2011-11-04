package com.robbix.mp5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.TileSet;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.CursorSet;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.UnitFactory;

public class Game
{
	public static Game load(
		File root,
		String mapName,
		String tileSetName,
		boolean lazySprites,
		boolean lazySounds)
	throws IOException
	{
		Game game = new Game();
		
		game.factory = UnitFactory.load(new File(root, "units"));
		game.tileSet = TileSet.load(new File(root, "tileset"), tileSetName);
		game.map = LayeredMap.load(new File(root, "terrain"), mapName, game.tileSet);
		game.spriteLib = SpriteLibrary.load(new File(root, "sprites"), lazySprites);
		game.sounds = SoundBank.load(new File(root, "sounds"), lazySounds);
		game.cursorSet = CursorSet.load(new File(root, "cursors"));
		game.addDisplay(new DisplayPanel(game));
		
		return game;
	}
	
	public static Game of(Object... stuff)
	{
		Game game = new Game();
		
		for (Object thing : stuff)
		{
			if (thing instanceof SpriteLibrary)
			{
				game.spriteLib = (SpriteLibrary) thing;
			}
			else if (thing instanceof SoundBank)
			{
				game.sounds = (SoundBank) thing;
			}
			else if (thing instanceof UnitFactory)
			{
				game.factory = (UnitFactory) thing;
			}
			else if (thing instanceof TileSet)
			{
				game.tileSet = (TileSet) thing;
			}
			else if (thing instanceof CursorSet)
			{
				game.cursorSet = (CursorSet) thing;
			}
			else if (thing instanceof LayeredMap)
			{
				game.map = (LayeredMap) thing;
			}
			else if (thing instanceof DisplayPanel)
			{
				game.addDisplay((DisplayPanel) thing);
			}
		}
		
		return game;
	}
	
	private Map<Integer, Player> players;
	private Player defaultPlayer;
	
	private List<DisplayPanel> displays;
	private LayeredMap map;
	private SpriteLibrary spriteLib;
	private SoundBank sounds;
	private TileSet tileSet;
	private UnitFactory factory;
	private CursorSet cursorSet;
	private Set<Trigger> triggers;
	
	private GameListener.Helper listenerHelper = new GameListener.Helper();
	
	private Game()
	{
		displays = Collections.synchronizedList(new ArrayList<DisplayPanel>());
		defaultPlayer = new Player(0, "Default", 240);
		players = new HashMap<Integer, Player>();
		players.put(0, defaultPlayer);
		triggers = Collections.synchronizedSet(new HashSet<Trigger>());
	}
	
	public void addGameListener(GameListener listener)
	{
		listenerHelper.add(listener);
	}
	
	public Set<Trigger> getTriggers()
	{
		return triggers;
	}
	
	public LayeredMap getMap()
	{
		return map;
	}
	
	public void removeDisplay(DisplayPanel panel)
	{
		displays.remove(panel);
	}
	
	public void addDisplay(DisplayPanel panel)
	{
		displays.add(panel);
	}
	
	public List<DisplayPanel> getDisplays()
	{
		return new ArrayList<DisplayPanel>(displays);
	}
	
	public DisplayPanel getDisplay(int index)
	{
		return displays.get(index);
	}
	
	public DisplayPanel getDisplay()
	{
		return getDisplay(0);
	}
	
	public SpriteLibrary getSpriteLibrary()
	{
		return spriteLib;
	}
	
	public SoundBank getSoundBank()
	{
		return sounds;
	}
	
	public TileSet getTileSet()
	{
		return tileSet;
	}
	
	public UnitFactory getUnitFactory()
	{
		return factory;
	}
	
	public CursorSet getCursorSet()
	{
		return cursorSet;
	}
	
	public Player getDefaultPlayer()
	{
		return defaultPlayer;
	}
	
	public void addPlayer(Player player)
	{
		players.put(player.getID(), player);
		listenerHelper.firePlayerAdded(player);
	}
	
	public Collection<Player> getPlayers()
	{
		return players.values();
	}
	
	public Player getPlayer(int id)
	{
		return players.get(id);
	}
}
