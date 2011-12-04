package com.robbix.mp5.sb.demo;

import java.io.IOException;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.SteerTask;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;

public class CombatDemo extends Demo
{
	public CombatDemo()
	{
		super(
			"48-48-plain",
			new String[]{
				"eLynxChassis",
				"pLynxChassis",
				"eLaserSingleTurret",
				"pMicrowaveSingleTurret",
				"eRailGunSingleTurret",
				"pRPGSingleTurret",
				"ePantherChassis",
				"pPantherChassis",
				"aRocket",
				"aAcidCloud",
				"eAcidCloudSingleTurret",
				"aDeath"
			},
			new String[]{
				"laser",
				"microwave",
				"railGunFire",
				"railGunHit",
				"rocketLaunch"
			},
			new Player[]{
				new Player(1, "Axen", RColor.getHue(320)),
				new Player(2, "Emma", RColor.getHue(200)),
				new Player(3, "Nguyen", RColor.getHue(40)),
				new Player(4, "Frost", RColor.getHue(160)),
				new Player(5, "Brook", RColor.getHue(95))
			},
			5
		);
	}
	
	public void setup(Game game) throws IOException
	{
		super.setup(game);
		
		Position center = game.getMap().getBounds().getCenter();
		game.getDisplay().setViewCenterPosition(center);
	}
	
	public void placeUnits(LayeredMap map, UnitFactory factory)
	{
		Position center = map.getBounds().getCenter();
		
		for (int x = 1; x <= 15; ++x)
		for (int y = 1; y <= 11; ++y)
		{
			Unit tank = factory.newUnit("pMicrowaveLynx", 1);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}
		
		for (int x = 36; x <= 46; ++x)
		for (int y = 1;  y <= 15;  ++y)
		{
			Unit tank = factory.newUnit("eLaserLynx", 2);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}

		for (int x = 1;  x <= 11; ++x)
		for (int y = 31; y <= 46; ++y)
		{
			Unit tank = factory.newUnit("pRPGLynx", 3);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}

		for (int x = 31; x <= 46; ++x)
		for (int y = 36; y <= 46; ++y)
		{
			Unit tank = factory.newUnit("eRailGunLynx", 4);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}
		
		for (int x = 20; x <= 29; ++x)
		for (int y = 20; y <= 29; ++y)
		{
			Unit tank = factory.newUnit("eAcidCloudPanther", 5);
			map.putUnit(tank, new Position(x, y));
		}
	}
}
