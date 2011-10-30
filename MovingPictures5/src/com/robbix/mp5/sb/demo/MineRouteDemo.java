package com.robbix.mp5.sb.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.MineRouteTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;

public class MineRouteDemo extends Demo
{
	public MineRouteDemo()
	{
		super(
			"48-48-plain",
			Utils.asSet(
				"eCargoTruck",
				"eCommandCenter",
				"eCommonSmelter",
				"aCommonMine",
				"eCommonMine",
				"aResource",
				"aStructureStatus"
			),
			Utils.asSet(
				"dockGrab",
				"dockLower",
				"dockOpen"
			),
			Utils.asSet(
				new Player(1, "Mining Operation", 200)
			),
			1
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
		List<Unit> mines    = new ArrayList<Unit>();
		List<Unit> smelters = new ArrayList<Unit>();
		
		for (int x = 2; x < 44; x += 6)
		for (int y = 2; y < 20; y += 6)
		{
			Unit smelter = factory.newUnit("eCommonSmelter", 1);
			map.putUnit(smelter, Mediator.getPosition(x, y));
			map.putTube(Mediator.getPosition(x + 5, y + 1));
			map.putTube(Mediator.getPosition(x + 2, y + 4));
			map.putTube(Mediator.getPosition(x + 2, y + 5));
			smelters.add(smelter);
		}
		
		map.putUnit(factory.newUnit("eCommandCenter", 1), Mediator.getPosition(44, 2));
		
		for (int x = 2;  x < 46; x += 4)
		for (int y = 38; y < 45; y += 4)
		{
			ResourceDeposit res = ResourceDeposit.get2BarCommon();
			map.putResourceDeposit(res, Mediator.getPosition(x + 1, y));
			Unit mine = factory.newUnit("eCommonMine", 1);
			map.putUnit(mine, Mediator.getPosition(x, y));
			mines.add(mine);
		}
		
		Unit truck;
		int truckIndex = 0;
		
		Collections.shuffle(mines);
		Collections.shuffle(smelters);
		
		for (Unit mine : mines)
		for (Unit smelter : smelters)
		{
			if (Utils.randInt(0, 5) % 6 == 0)
			{
				Position pos = Mediator.getPosition(
					truckIndex % 42 + 2,
					truckIndex / 42 + 24
				);
				
				truck = factory.newUnit("eCargoTruck", 1);
				truck.assignNow(new MineRouteTask(mine, smelter));
				map.putUnit(truck, pos);
			}
			
			truckIndex++;
		}
		
		// FIXME: how to set starting view position
		// should be centered for this demo
	}
}
