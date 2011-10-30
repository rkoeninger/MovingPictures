package com.robbix.mp5.sb.demo;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.UnitFactory;

public class MeteorDemo extends Demo
{
	public MeteorDemo()
	{
		super(
			"30-20-plain",
			Utils.asSet(
				"pScout",
				"pResidence",
				"eVehicleFactory",
				"eStructureFactory",
				"eCommonSmelter",
				"eCommandCenter",
				"aDeath",
				"aMeteor",
				"aStructureStatus"
			),
			Utils.asSet(
				"meteor"
			),
			Utils.asSet(
				new Player(1, "Targets", 45)
			),
			1
		);
	}
	
	public void placeUnits(LayeredMap map, UnitFactory factory)
	{
		for (int x = 0; x < 12; ++x)
		for (int y = 0; y < 20; ++y)
		{
			map.putUnit(factory.newUnit("pScout"), Mediator.getPosition(x, y));
		}
		
		for (int x = 12; x < 30; x += 2)
		for (int y = 13; y < 19; y += 2)
		{
			map.putUnit(factory.newUnit("pResidence"), Mediator.getPosition(x, y));
		}
		
		map.putUnit(factory.newUnit("eVehicleFactory"),   Mediator.getPosition(12, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   Mediator.getPosition(16, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   Mediator.getPosition(20, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   Mediator.getPosition(24, 1));
		map.putUnit(factory.newUnit("eStructureFactory"), Mediator.getPosition(12, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), Mediator.getPosition(16, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), Mediator.getPosition(20, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), Mediator.getPosition(24, 5));
		map.putUnit(factory.newUnit("eCommonSmelter"),    Mediator.getPosition(12, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"),    Mediator.getPosition(16, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"),    Mediator.getPosition(20, 9));
		map.putUnit(factory.newUnit("eCommandCenter"),    Mediator.getPosition(24, 9));
	}
}
