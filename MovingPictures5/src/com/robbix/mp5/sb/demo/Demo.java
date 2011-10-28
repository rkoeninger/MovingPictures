package com.robbix.mp5.sb.demo;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.UnitFactory;

public abstract class Demo
{
	private String mapName;
	private Set<String> spriteModules;
	private Set<String> soundModules;
	private Set<Player> players;
	
	public Demo(
		String mapName,
		Set<String> spriteModules,
		Set<String> soundModules,
		Set<Player> players)
	{
		this.mapName = mapName;
		
		if (spriteModules == null)
			this.spriteModules = Collections.emptySet();
		else
			this.spriteModules = Collections.unmodifiableSet(spriteModules);
		
		if (soundModules == null)
			this.soundModules = Collections.emptySet();
		else
			this.soundModules = Collections.unmodifiableSet(soundModules);
		
		if (players == null)
			this.players = Collections.emptySet();
		else
			this.players = Collections.unmodifiableSet(players);
	}
	
	public String getMapName()
	{
		return mapName;
	}
	
	public Set<String> getRequiredSpriteModuleNames()
	{
		return spriteModules;
	}
	
	public Set<String> getRequiredSoundModulesNames()
	{
		return soundModules;
	}
	
	public Set<Player> getPlayers()
	{
		return players;
	}
	
	public void setup(Game game) throws IOException
	{
		SpriteLibrary sprites = game.getSpriteLibrary();
		SoundBank sounds = game.getSoundBank();
		
		for (String spriteModule : spriteModules)
			sprites.loadModule(spriteModule);
		
		for (String soundModule : soundModules)
			sounds.loadModule(soundModule);
		
		for (Player player : players)
			game.addPlayer(player);
		
		placeUnits(game.getMap(), game.getUnitFactory());
	}
	
	protected abstract void placeUnits(LayeredMap map, UnitFactory factory);
}
