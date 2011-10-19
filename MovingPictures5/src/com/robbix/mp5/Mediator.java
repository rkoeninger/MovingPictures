package com.robbix.mp5;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robbix.mp5.ai.AStar;
import com.robbix.mp5.ai.task.AttackTask;
import com.robbix.mp5.ai.task.BuildTask;
import com.robbix.mp5.ai.task.EarthworkerConstructTask;
import com.robbix.mp5.ai.task.PathTask;
import com.robbix.mp5.ai.task.RotateTask;
import com.robbix.mp5.ai.task.SteerTask;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.PositionCache;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.LayeredMap.Fixture;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.ani.AcidCloudFireAnimation;
import com.robbix.mp5.ui.ani.LaserFireAnimation;
import com.robbix.mp5.ui.ani.MeteorAnimation;
import com.robbix.mp5.ui.ani.MicrowaveFireAnimation;
import com.robbix.mp5.ui.ani.RPGFireAnimation;
import com.robbix.mp5.ui.ani.RailGunFireAnimation;
import com.robbix.mp5.ui.ani.SpriteSequenceAnimation;
import com.robbix.mp5.ui.ani.WeaponFireAnimation;
import static com.robbix.mp5.unit.Activity.*;

import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;

public class Mediator
{
	public static LayeredMap map;
	public static DisplayPanel panel;
	public static SoundBank sounds;
	public static UnitFactory factory;
	
	public static Game game;
	
	private static PositionCache cache;
	
	private static boolean soundOn = false;
	
	public static void initMediator(Game game)
	{
		Mediator.map = game.getMap();
		Mediator.panel = game.getDisplay();
		Mediator.factory = game.getUnitFactory();
		Mediator.sounds = game.getSoundBank();
		Mediator.game = game;
	}
	
	public static void initCache(int w, int h)
	{
		Mediator.cache = new PositionCache(w, h);
		Mediator.cache.prepare();
	}
	
	public static Position getPosition(int x, int y)
	{
		return cache.get(x, y);
	}
	
	public static void soundOn(boolean soundOn)
	{
		Mediator.soundOn = soundOn;
		
		if (soundOn)
			sounds.start();
	}
	
	public static boolean soundOn()
	{
		return soundOn;
	}
	
	public static void playSound(String name)
	{
		if (soundOn)
			sounds.play(name);
	}
	
	public static void playSound(String name, Position pos)
	{
		if (game.getDisplay().getDisplayRegion().contains(pos))
			playSound(name);
	}
	
	public static void playSound(String name, int x, int y)
	{
		playSound(name, getPosition(x, y));
	}
	
	public static void doAttack(Unit attacker, Unit target)
	{
		if (attacker.getCharge() < attacker.getType().getWeaponChargeCost())
			return;
		else if (attacker.getPosition().getDistance(target.getPosition())
				> attacker.getType().getAttackRange())
			return;
		
		attacker.discharge();
		
		WeaponFireAnimation fireAnimation = null;
		
		if (attacker.getType().getName().contains("Laser"))
		{
			fireAnimation = new LaserFireAnimation(attacker, target);
		}
		else if (attacker.getType().getName().contains("Microwave"))
		{
			fireAnimation = new MicrowaveFireAnimation(attacker, target);
		}
		else if (attacker.getType().getName().contains("RailGun"))
		{
			fireAnimation = new RailGunFireAnimation(attacker, target);
		}
		else if (attacker.getType().getName().contains("RPG"))
		{
			fireAnimation = new RPGFireAnimation(attacker, target);
		}
		else if (attacker.getType().getName().contains("AcidCloud"))
		{
			fireAnimation = new AcidCloudFireAnimation(attacker, target);
		}
		
		attacker.assignNow(new AttackTask(target, fireAnimation));
		
		panel.cueAnimation(fireAnimation);
	}
	
	public static void doDamage(Unit attacker, Unit target, double amount)
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
				playSound("structureCollapse1", target.getPosition());
			}
			else if (newBracket == HealthBracket.RED
			 && bracket != HealthBracket.RED)
			{
				playSound("structureCollapse2", target.getPosition());
			}
		}
	}
	
	public static void doSplashDamage(Position pos, double amount, double range)
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
			Position current = cache.get(x, y);
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
		
		panel.refresh();
	}
	
	public static void doEarthworkerBuildRow(Unit unit, List<Position> row, Fixture fixture)
	{
		unit.cancelAssignments();
		
		for (Position pos : row)
		{
			unit.interrupt(new EarthworkerConstructTask(pos, fixture, 48));
			unit.interrupt(new SteerTask(pos));
		}
	}
	
	public static void doEarthworkerBuild(Unit unit, Position pos, Fixture fixture)
	{
		doMove(unit, pos, false);
		unit.assignLater(new EarthworkerConstructTask(pos, fixture, 48));
	}
	
	public static void doMove(Unit unit, Position pos)
	{
		doMove(unit, pos, true, 0);
	}
	
	public static void doMove(Unit unit, Position pos, boolean interrupt)
	{
		doMove(unit, pos, interrupt, 0);
	}

	public static void doMove(Unit unit, Position pos, double distance)
	{
		doMove(unit, pos, true, distance);
	}
	
	public static void doMove(Unit unit, Position pos, boolean interrupt, double distance)
	{
		if (unit.isStructure() || unit.getType().isGuardPostType() || unit.isDead()) return;
		
		List<Position> path = new AStar().getPath(
			map.getTerrainCostMap(),
			unit.getPosition(),
			pos,
			distance
		);
		
		if (path == null) return;
		
		if (interrupt)
		{
			unit.assignNow(new PathTask(path));
		}
		else
		{
			unit.assignNext(new PathTask(path));
		}
	}
	
	public static void doApproach(Unit unit, Position pos)
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
	
	public static void doGroupMove(Set<Unit> units, Position pos)
	{
		for (Unit unit : units)
		{
			doMove(unit, pos);
		}
	}
	
	public static void doBuild(Unit unit, Position pos)
	{
		int buildFrames = panel.getSpriteLibrary().getUnitSpriteSet(unit.getType()).get(BUILD).getFrameCount();
		unit.setActivity(BUILD);
		unit.assignNow(new BuildTask(buildFrames, 100));
		map.putUnit(unit, pos);
		playSound("structureBuild", pos);
	}
	
	public static boolean doBuildMine(Unit miner)
	{
		if (!miner.isMiner())
			throw new IllegalArgumentException();
		
		if (miner.isBusy())
			return false;
		
		Position pos = miner.getPosition();
		
		if (!map.hasResourceDeposit(pos))
			return false;
		
		Position minePos = pos.shift(-1, 0);
		
		if (!map.getBounds().contains(minePos))
			return false;

		map.remove(miner);
		Unit mine = Mediator.factory.newUnit("eCommonMine", miner.getOwner());
		Mediator.doBuild(mine, minePos);
		return true;
	}
	
	public static void kill(Unit unit)
	{
		unit.setHP(0);
		
		if (unit.getType().isGuardPostType())
		{
			Position pos = unit.getPosition();
			Point point = panel.getPoint(pos);
			SpriteGroup seq = panel.getSpriteLibrary().getAmbientSpriteGroup("aDeath", "guardPostKilled");
			panel.cueAnimation(new SpriteSequenceAnimation(
				seq,
				point,
				2
			));
			map.remove(unit);
			playSound("structureExplosion", pos);
			doSplashDamage(pos, 100, 1);
		}
		else if (unit.isStructure())
		{
			Position pos = unit.getPosition();
			Point point = panel.getPoint(pos);
			SpriteGroup seq = panel.getSpriteLibrary().getUnitSpriteSet(unit.getType()).get(COLLAPSE);
			panel.cueAnimation(new SpriteSequenceAnimation(
				seq,
				unit.getOwner().getColorHue(),
				point,
				2
			));
			
			String eventName = null;
			int fpWidth = unit.getFootprint().getWidth();
			int fpHeight = unit.getFootprint().getHeight();
			
			if (fpWidth == 4 && fpHeight == 3)
			{
				eventName = "collapseSmoke3";
			}
			else if (fpWidth == 3 && fpHeight == 3)
			{
				eventName = "collapseSmoke2";
			}
			else if (fpWidth == 5 && fpHeight == 4)
			{
				eventName = "collapseSmoke5";
			}
			else if (fpWidth == 3 && fpHeight == 2)
			{
				eventName = "collapseSmoke1";
			}
			else if (fpWidth == 2 && fpHeight == 2)
			{
				eventName = "collapseSmoke6";
			}
			else if (fpWidth == 2 && fpHeight == 1)
			{
				eventName = "collapseSmoke5";
			}
			else if (fpWidth == 1 && fpHeight == 2)
			{
				eventName = "collapseSmoke5";
			}
			else if (fpWidth == 1 && fpHeight == 1)
			{
				eventName = "collapseSmoke5";
			}
			
			seq = panel.getSpriteLibrary().getAmbientSpriteGroup("aDeath", eventName);
			panel.cueAnimation(new SpriteSequenceAnimation(
				seq,
				point,
				2
			));
			map.remove(unit);
			playSound("structureCollapse3");
		}
		else
		{
			String eventName = unit.isArachnid() ? "arachnidKilled" : "vehicleKilled";
			map.remove(unit);
			panel.cueAnimation(new SpriteSequenceAnimation(
				panel.getSpriteLibrary().getAmbientSpriteGroup("aDeath", eventName),
				new Point(unit.getAbsX(), unit.getAbsY()),
				2
			));
			playSound("smallExplosion1");
		}
	}
	
	public static void selfDestruct(Unit unit)
	{
		unit.setHP(0);
		
		if (unit.isStructure() || unit.getType().isGuardPostType())
		{
			kill(unit);
		}
		else
		{
			if (unit.isTurret())
				unit = unit.getChassis();
			
			Position pos = unit.getPosition();
			
			String setName = null;
			String eventName = null;
			double damage = 0;
			double range = 0;
			
			if (unit.isStarflare())
			{
				playSound("smallExplosion2", pos);
				setName = "aStarflareExplosion";
				eventName = "explosion";
				damage = unit.getTurret().getType().getDamage();
				range = 1.5;
			}
			else if (unit.isSupernova())
			{
				playSound("smallExplosion3", pos);
				setName = "aSupernovaExplosion";
				eventName = "explosion";
				damage = unit.getTurret().getType().getDamage();
				range = 3;
			}
			else
			{
				playSound("smallExplosion1", pos);
				setName = "aDeath";
				eventName = "vehicleSelfDestruct";
				damage = 50;
				range = 1.5;
			}
			
			map.remove(unit);
			panel.cueAnimation(new SpriteSequenceAnimation(
				panel.getSpriteLibrary().getAmbientSpriteGroup(setName, eventName),
				new Point(unit.getAbsX(), unit.getAbsY()),
				2
			));
			doSplashDamage(pos, damage, range);
		}
	}
	
	public static void doSpawnMeteor(Position pos)
	{
		panel.cueAnimation(new MeteorAnimation(pos, panel.getSpriteLibrary()));
		playSound("meteor", pos);
		playSound("savant_meteorApproaching");
	}
}
