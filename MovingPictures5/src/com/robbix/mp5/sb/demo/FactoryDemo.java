package com.robbix.mp5.sb.demo;

import java.io.IOException;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;

public class FactoryDemo extends Demo
{
	public FactoryDemo()
	{
		super(
			"30-20-plain",
			new String[]{
				"eConVec",
				"eCargoTruck",
				"eCommandCenter",
				"eCommonSmelter",
				"eStructureFactory",
				"eVehicleFactory",
				"eEarthworker",
				"pRoboDozer"
			},
			new String[]{
				"structureBuild"
			},
			new Player[]{
				new Player(1, "Factories", RColor.getHue(275))
			},
			1
		);
	}
	
	public void setup(Game game) throws IOException
	{
		super.setup(game);
		
		Player player1 = game.getPlayer(1);
		player1.addResource(ResourceType.COMMON_ORE, 50000);
		player1.addResource(ResourceType.RARE_ORE,   50000);
		player1.addResource(ResourceType.FOOD,       50000);
	}
	
	public void placeUnits(LayeredMap map, UnitFactory factory)
	{
		Unit convec1 = factory.newUnit("eConVec", 1);
		Unit convec2 = factory.newUnit("eConVec", 1);
		Unit convec3 = factory.newUnit("eConVec", 1);
		Unit convec4 = factory.newUnit("eConVec", 1);
		Unit crane   = factory.newUnit("eEarthworker", 1);
		Unit dozer   = factory.newUnit("pRoboDozer", 1);
		Unit truck1  = factory.newUnit("eCargoTruck", 1);
		Unit truck2  = factory.newUnit("eCargoTruck", 1);
		Unit truck3  = factory.newUnit("eCargoTruck", 1);
		
		convec1.setCargo(Cargo.newConVecCargo("eVehicleFactory"));
		convec2.setCargo(Cargo.newConVecCargo("eStructureFactory"));
		convec3.setCargo(Cargo.newConVecCargo("eCommonSmelter"));
		convec4.setCargo(Cargo.newConVecCargo("eCommandCenter"));
		
		ResourceDeposit deposit1 = ResourceDeposit.get2BarCommon();
		ResourceDeposit deposit2 = ResourceDeposit.get3BarCommon();
		
		map.putUnit(convec1, new Position(9,  7));
		map.putUnit(convec2, new Position(10, 7));
		map.putUnit(convec3, new Position(11, 7));
		map.putUnit(convec4, new Position(12, 7));
		map.putUnit(crane,   new Position(10, 9));
		map.putUnit(dozer,   new Position(11, 9));
		map.putUnit(truck1,  new Position(9,  11));
		map.putUnit(truck2,  new Position(10, 11));
		map.putUnit(truck3,  new Position(11, 11));
		map.putResourceDeposit(deposit1, new Position(28, 18));
		map.putResourceDeposit(deposit2, new Position(5, 16));
	}
}
