package com.robbix.mp5.sb.demo;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.utils.Position;

public class MeteorDemo extends Demo
{
	public MeteorDemo()
	{
		super(
			"30-20-plain",
			new String[]{
				"pScout",
				"pResidence",
				"eVehicleFactory",
				"eStructureFactory",
				"eCommonSmelter",
				"eCommandCenter",
				"aDeath",
				"aMeteor",
				"aStructureStatus"
			},
			new String[]{
				"meteor"
			},
			new Player[]{
				new Player(1, "Targets", 45)
			},
			1
		);
	}
	
	public void placeUnits(LayeredMap map, UnitFactory factory)
	{
		for (int x = 0; x < 12; ++x)
		for (int y = 0; y < 20; ++y)
		{
			map.putUnit(factory.newUnit("pScout"), new Position(x, y));
		}
		
		for (int x = 12; x < 30; x += 2)
		for (int y = 13; y < 19; y += 2)
		{
			map.putUnit(factory.newUnit("pResidence"), new Position(x, y));
		}
		
		map.putUnit(factory.newUnit("eVehicleFactory"),   new Position(12, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   new Position(16, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   new Position(20, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"),   new Position(24, 1));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(12, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(16, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(20, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(24, 5));
		map.putUnit(factory.newUnit("eCommonSmelter"),    new Position(12, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"),    new Position(16, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"),    new Position(20, 9));
		map.putUnit(factory.newUnit("eCommandCenter"),    new Position(24, 9));
	}
}
