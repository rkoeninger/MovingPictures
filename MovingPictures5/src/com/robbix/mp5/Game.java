package com.robbix.mp5;

import static com.robbix.mp5.unit.Activity.BUILD;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.robbix.mp5.ai.AStar;
import com.robbix.mp5.ai.task.AttackTask;
import com.robbix.mp5.ai.task.BuildTask;
import com.robbix.mp5.ai.task.EarthworkerConstructTask;
import com.robbix.mp5.ai.task.PathTask;
import com.robbix.mp5.ai.task.RotateTask;
import com.robbix.mp5.ai.task.SteerTask;
import com.robbix.mp5.map.Entity;
import com.robbix.mp5.map.Fixture;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.TileSet;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.CursorSet;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.ani.AcidCloudAnimation;
import com.robbix.mp5.ui.ani.LaserAnimation;
import com.robbix.mp5.ui.ani.MeteorAnimation;
import com.robbix.mp5.ui.ani.MicrowaveAnimation;
import com.robbix.mp5.ui.ani.RPGAnimation;
import com.robbix.mp5.ui.ani.RailGunAnimation;
import com.robbix.mp5.ui.ani.WeaponAnimation;
import com.robbix.mp5.ui.obj.UnitDeathDisplayObject;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.utils.CostMap;
import com.robbix.utils.Direction;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.RIterator;
import com.robbix.utils.Region;
import com.robbix.utils.Utils;

public class Game
{
	public static Game game;
	
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
			game.add(thing);
		
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
	private List<Runnable> pendingDoLaters;
	
	private Collection<Entity> entities;
	
	private int frame = 0;
	
	private GameListener.Helper listenerHelper = new GameListener.Helper();
	
	private boolean soundOn;
	
	private Game()
	{
		displays = Collections.synchronizedList(new ArrayList<DisplayPanel>());
		defaultPlayer = new Player(0, "Default", RColor.getGray(96));
		players = new HashMap<Integer, Player>();
		players.put(0, defaultPlayer);
		triggers = Collections.synchronizedSet(new HashSet<Trigger>());
		pendingDoLaters = new ArrayList<Runnable>();
		entities = new ArrayList<Entity>();
		Game.game = this;
	}
	
	public void add(Object... stuff)
	{
		for (Object thing : stuff)
		{
			if      (thing instanceof SpriteLibrary)spriteLib = (SpriteLibrary) thing;
			else if (thing instanceof SoundBank)    sounds = (SoundBank) thing;
			else if (thing instanceof UnitFactory)  factory = (UnitFactory) thing;
			else if (thing instanceof TileSet)      tileSet = (TileSet) thing;
			else if (thing instanceof CursorSet)    cursorSet = (CursorSet) thing;
			else if (thing instanceof LayeredMap)   map = (LayeredMap) thing;
			else if (thing instanceof DisplayPanel) addDisplay((DisplayPanel) thing);
		}
	}
	
	public void setFrame(int frame)
	{
		this.frame = frame;
	}
	
	public int getFrame()
	{
		return frame;
	}
	
	public void doLater(Runnable doRun)
	{
		pendingDoLaters.add(doRun);
	}
	
	public RIterator<Runnable> getAndClearDoLaters()
	{
		try
		{
			return RIterator.iterate(new ArrayList<Runnable>(pendingDoLaters));
		}
		finally
		{
			pendingDoLaters.clear();
		}
	}
	
	public void addEntity(Entity e)
	{
		entities.add(e);
	}
	
	public RIterator<Entity> getFreeEntities()
	{
		return RIterator.iterate(new ArrayList<Entity>(entities));
	}
	
	public RIterator<Runnable> getDoLaters()
	{
		return RIterator.iterate(pendingDoLaters);
	}
	
	public void clearDoLaters()
	{
		pendingDoLaters.clear();
	}
	
	public void setSoundOn(boolean soundOn)
	{
		this.soundOn = soundOn;
		
		if (soundOn)
			sounds.start();
	}
	
	public boolean isSoundOn()
	{
		return soundOn;
	}
	
	public void playSound(String name)
	{
		if (soundOn)
			sounds.play(name);
	}
	
	public void playSound(String name, Position pos)
	{
		playSound(name, pos.x, pos.y);
	}
	
	public void playSound(String name, int x, int y)
	{
		if (!soundOn)
			return;
		
		Region displayRegion = getDisplay().getDisplayRegion();
		
		if (displayRegion.contains(x, y))
		{
			Position center = displayRegion.getCenter();
			float spread = (x - center.x) / (float) displayRegion.w;
			spread = Math.min(Math.max(spread, -1.0f), 1.0f);
			float volume = 0.5f + Math.abs(spread) * 0.5f;
			sounds.play(name, volume, spread, null);
		}
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
	
	public DisplayPanel newDisplay()
	{
		DisplayPanel panel = new DisplayPanel(this);
		displays.add(panel);
		return panel;
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
	
	public void doAttack(Unit attacker, Unit target)
	{
		if (attacker.getCharge() < attacker.getType().getWeaponChargeCost())
			return;
		else if (attacker.getPosition().getDistance(target.getPosition())
				> attacker.getType().getAttackRange())
			return;
		
		attacker.discharge();
		
		WeaponAnimation fireAnimation = null;
		
		if (attacker.getType().getName().contains("Laser"))
		{
			fireAnimation = new LaserAnimation(game.getSpriteLibrary(), attacker, target);
		}
		else if (attacker.getType().getName().contains("Microwave"))
		{
			fireAnimation = new MicrowaveAnimation(game.getSpriteLibrary(), attacker, target);
		}
		else if (attacker.getType().getName().contains("RailGun"))
		{
			fireAnimation = new RailGunAnimation(game.getSpriteLibrary(), attacker, target);
		}
		else if (attacker.getType().getName().contains("RPG"))
		{
			fireAnimation = new RPGAnimation(game.getSpriteLibrary(), attacker, target);
		}
		else if (attacker.getType().getName().contains("AcidCloud"))
		{
			fireAnimation = new AcidCloudAnimation(game.getSpriteLibrary(), attacker, target);
		}
		
		attacker.assignNow(new AttackTask(target, fireAnimation));
		
		getDisplay().cueAnimation(fireAnimation);
	}
	
	public void doDamage(Unit attacker, Unit target, double amount)
	{
		int hp = target.getHP();
		
		if (hp == 0)
		{
			return;
		}
		
		HealthBracket bracket = target.getHealthBracket();
		
		amount += Utils.randInt((int)-(amount/8), (int)amount/8);
		
		hp -= (int) amount;
		
		if (hp < 0)
		{
			hp = 0;
		}
		
		target.setHP(hp);
		
		if (hp == 0)
		{
			kill(target);
		}
		else if (target.isStructure() && !target.getType().isGuardPostType())
		{
			HealthBracket newBracket = target.getHealthBracket();
			
			if (newBracket == HealthBracket.YELLOW
			 && bracket != HealthBracket.YELLOW)
			{
				Game.game.playSound("structureCollapse1", target.getPosition());
			}
			else if (newBracket == HealthBracket.RED
			 && bracket != HealthBracket.RED)
			{
				Game.game.playSound("structureCollapse2", target.getPosition());
			}
		}
	}
	
	public void doSplashDamage(Position pos, double amount, double range)
	{
		if (range <= 0 || amount <= 0)
			return;
		
		int spotSize = map.getSpotSize();
		
		int absX = pos.x * spotSize;
		int absY = pos.y * spotSize;
		
		int rangeInt = (int)Math.ceil(range);
		
		int xMin = Math.max(pos.x - rangeInt, 0);
		int yMin = Math.max(pos.y - rangeInt, 0);
		int xMax = Math.min(pos.x + rangeInt, map.getWidth() - 1);
		int yMax = Math.min(pos.y + rangeInt, map.getHeight() - 1);
		
		Set<Unit> affectedUnits = new HashSet<Unit>();
		
		for (int x = xMin; x <= xMax; ++x)
		for (int y = yMin; y <= yMax; ++y)
		{
			Position current = new Position(x, y);
			Unit unit = map.getUnit(current);
			
			if (unit != null)
			{
				int unitAbsX = unit.getX() * spotSize + unit.getXOffset();
				int unitAbsY = unit.getY() * spotSize + unit.getYOffset();
				
				double absDist = Math.hypot(unitAbsX - absX, unitAbsY - absY);
				
				if (absDist <= (range * spotSize))
				{
					affectedUnits.add(unit);
				}
			}
			
			double absDist = current.getDistance(pos);
			
			if (absDist <= (range * spotSize))
			{
				if (map.hasWall(current) || map.hasTube(current))
				{
					int fixtureHP = map.getFixtureHP(current);
					fixtureHP -= (int) amount;
					
					if (fixtureHP <= 0)
					{
						map.bulldoze(current);
					}
					else
					{
						map.setFixtureHP(current, fixtureHP);
					}
				}
			}
		}
		
		for (Unit unit : affectedUnits)
		{
			doDamage(null, unit, amount);
		}
		
		getDisplay().refresh();
	}
	
	public void doEarthworkerBuildRow(Unit unit, List<Position> row, Fixture fixture)
	{
		unit.cancelAssignments();
		ListIterator<Position> itr = row.listIterator(row.size());
		
		while (itr.hasPrevious())
		{
			Position pos = itr.previous();
			
			if (map.hasFixture(pos))
				continue;
			
			unit.interrupt(new EarthworkerConstructTask(pos, fixture, 48));
			unit.interrupt(new SteerTask(pos));
		}
	}
	
	public void doEarthworkerBuild(Unit unit, Position pos, Fixture fixture)
	{
		doMove(unit, pos, false);
		unit.assignLater(new EarthworkerConstructTask(pos, fixture, 48));
	}
	
	public void doMove(Unit unit, Position pos)
	{
		doMove(unit, pos, true, 0);
	}
	
	public void doMove(Unit unit, Position pos, boolean interrupt)
	{
		doMove(unit, pos, interrupt, 0);
	}

	public void doMove(Unit unit, Position pos, double distance)
	{
		doMove(unit, pos, true, distance);
	}
	
	public void doMove(Unit unit, Position pos, boolean interrupt, double distance)
	{
		if (unit.isStructure() || unit.getType().isGuardPostType() || unit.isDead())
			return;
		
		CostMap costMap = map.getTerrainCostMap();
		
		if (costMap.isInfinite(pos))
			return;
		
		List<Position> path = new AStar().getPath(costMap, unit.getPosition(), pos, distance);
		
		if (path == null)
			return;
		
		PathTask task = new PathTask(path);
		
		if (interrupt) unit.assignNow(task);
		          else unit.assignNext(task);
	}
	
	public void doApproach(Unit unit, Position pos)
	{
		if (unit.isStructure() || unit.getType().isGuardPostType() || unit.isDead())
			return;
		
		List<Position> path = new AStar().getPath(
			map.getTerrainCostMap(),
			unit.getPosition(),
			pos,
			1
		);
		
		if (path == null)
			return;
		
		if (path.size() > 0)
		{
			unit.assignNext(new PathTask(path));
		}
		
		if (path.size() > 1)
		{
			Position last = path.get(path.size() - 1);
			Direction dir = Direction.getMoveDirection(last, pos);
			unit.assignLater(new RotateTask(dir));
		}
	}
	
	public void doGroupMove(Set<Unit> units, Position pos)
	{
		for (Unit unit : units)
		{
			doMove(unit, pos);
		}
	}
	
	public void doBuild(Unit unit, Position pos)
	{
		int buildFrames = spriteLib.getUnitSpriteSet(unit.getType()).get(BUILD).getFrameCount();
		unit.setActivity(BUILD);
		unit.assignNow(new BuildTask(buildFrames, 100));
		map.putUnit(unit, pos);
		Game.game.playSound("structureBuild", pos);
	}
	
	public void kill(Unit unit)
	{
		unit.setHP(0);
		Position pos = unit.getPosition();
		getDisplay().addDisplayObject(new UnitDeathDisplayObject(unit, frame));
		map.remove(unit);
		
		if (unit.getType().isGuardPostType())
		{
			Game.game.playSound("structureExplosion", pos);
			doSplashDamage(pos, 100, 1);
		}
		else if (unit.isStructure())
		{
			Game.game.playSound("structureCollapse3", pos);
		}
		else if (unit.isArachnid())
		{
			Game.game.playSound("smallExplosion1", pos);
		}
		else
		{
			Game.game.playSound("smallExplosion1", pos);
		}
	}
	
	public void selfDestruct(Unit unit)
	{
		unit.setHP(0);
		getDisplay().addDisplayObject(new UnitDeathDisplayObject(unit, frame, true));
		
		if (unit.isStructure() || unit.getType().isGuardPostType())
		{
			kill(unit);
		}
		else
		{
			if (unit.isTurret())
				unit = unit.getChassis();
			
			Position pos = unit.getPosition();
			
			double damage = 0;
			double range = 0;
			
			if (unit.isStarflare())
			{
				Game.game.playSound("smallExplosion2", pos);
				damage = unit.getTurret().getType().getDamage();
				range = 1.5;
			}
			else if (unit.isSupernova())
			{
				Game.game.playSound("smallExplosion3", pos);
				damage = unit.getTurret().getType().getDamage();
				range = 3;
			}
			else
			{
				Game.game.playSound("smallExplosion1", pos);
				damage = 50;
				range = 1.5;
			}
			
			map.remove(unit);
			doSplashDamage(pos, damage, range);
		}
	}
	
	public void doSpawnMeteor(Position pos)
	{
		getDisplay().cueAnimation(new MeteorAnimation(spriteLib, pos));
		Game.game.playSound("meteor", pos);
	}
}
