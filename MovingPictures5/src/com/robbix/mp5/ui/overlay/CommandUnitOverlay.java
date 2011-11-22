package com.robbix.mp5.ui.overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.DockTask;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;
import com.robbix.utils.JListDialog;
import com.robbix.utils.Position;

public class CommandUnitOverlay extends InputOverlay
{
	private Unit unit;
	
	public CommandUnitOverlay(Unit unit)
	{
		super((!unit.isStructure() && !unit.getType().isGuardPostType()) ? "move" : null);
		this.unit = unit;
		this.requiresPaintOnGrid = false;
	}
	
	public void init()
	{
		super.init();
		panel.showStatus(unit);
	}
	
	public void dispose()
	{
		super.dispose();
		panel.showStatus((Unit)null);
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (unit.getPosition() == null)
			return;
		
		drawSelectedUnitBox(g, unit);
	}
	
	public void onCommand(Command command)
	{
		if (command == Command.ATTACK)
		{
			push(new SelectAttackTargetOverlay(unit.getTurret()));
		}
		else if (command == Command.STOP)
		{
			unit.cancelAssignments();
		}
		else if (command == Command.SELF_DESTRUCT)
		{
			Game.game.selfDestruct(unit);
			complete();
		}
		else if (command == Command.BULLDOZE && unit.is("Dozer"))
		{
			push(new SelectBulldozeOverlay(unit));
		}
		else if (command == Command.BUILD_TUBE && unit.is("Earthworker"))
		{
			push(new BuildTubeOverlay(unit));
		}
		else if (command == Command.CONSTRUCT && unit.is("ConVec"))
		{
			convecConstruct();
		}
		else if (command == Command.CONSTRUCT && unit.isMiner())
		{
			minerConstruct();
		}
		else if (command == Command.BULLDOZE && unit.is("Dozer"))
		{
			push(new SelectBulldozeOverlay(unit));
		}
		else if (command == Command.TRANSFER)
		{
			Collection<Player> players = Game.game.getPlayers();
			players = new ArrayList<Player>(players);
			players.remove(unit.getOwner());
			
			if (players.isEmpty())
				return;
			
			Object result = JListDialog.showDialog(players.toArray());
			
			if (result == null)
				return;
			
			Player player = (Player) result;
			unit.setOwner(player);
			complete();
		}
		else if (command == Command.KILL)
		{
			Game.game.kill(unit);
			complete();
		}
		else if (command == Command.BUILD && unit.is("VehicleFactory"))
		{
			List<UnitType> vehicleTypes = Game.game.getUnitFactory().getVehicleTypes();
			Object option = JListDialog.showDialog(vehicleTypes.toArray());
			
			if (option == null)
				return;
			
			UnitType type = (UnitType) option;
			Player owner = unit.getOwner();
			
			if (type.getCost() == null)
				return;
			
			if (!owner.canAfford(type.getCost()))
			{
				JOptionPane.showMessageDialog(
					panel,
					"can't afford it",
					"not enough monies",
					JOptionPane.ERROR_MESSAGE
				);
				
				return;
			}
			
			owner.spend(type.getCost());
			Unit newVehicle = Game.game.getUnitFactory().newUnit(type, owner);
			
			for (Position exitPos : unit.getFootprint().getFactoryExits(unit.getPosition()))
			{
				if (panel.getMap().canPlaceUnit(exitPos))
				{
					panel.getMap().putUnit(newVehicle, exitPos);
					return;
				}
			}
			
			JOptionPane.showMessageDialog(panel, "can't exit");
		}
		else if (command == Command.BUILD && unit.is("StructureFactory"))
		{
			List<UnitType> structTypes = Game.game.getUnitFactory().getStructureTypes();
			Object option = JListDialog.showDialog(structTypes.toArray());
			
			if (option == null)
				return;
			
			UnitType type = (UnitType) option;
			Player owner = unit.getOwner();
			
			if (type.getCost() == null)
				return;
			
			if (!owner.canAfford(type.getCost()))
			{
				JOptionPane.showMessageDialog(
					panel,
					"can't afford it",
					"not enough monies",
					JOptionPane.ERROR_MESSAGE
				);
				
				return;
			}
			
			owner.spend(type.getCost());
			unit.setStructureKit(type.getName());
		}
		else if (command == Command.DOCK && unit.is("ConVec"))
		{
			Position adj = unit.getPosition().shift(0, -1);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(adj))
			{
				Unit sFactory = map.getUnit(adj);
				
				if (sFactory != null
			&& sFactory.is("StructureFactory")
			&& !sFactory.isDead()
			&& !sFactory.isDisabled())
				{
					String kit = sFactory.getStructureKit();
					sFactory.setStructureKit(null);
					Cargo cargo = kit != null
						? Cargo.newConVecCargo(kit)
						: Cargo.EMPTY;
					unit.assignNow(new DockTask(sFactory, cargo));
				}
			}
		}
		else if (command == Command.IDLE && unit.isStructure())
		{
			if (unit.isIdle())
			{
				unit.activate();
			}
			else
			{
				unit.idle();
			}
		}
	}
	
	public void onLeftClick()
	{
		if (!(unit.isStructure() || unit.getType().isGuardPostType()))
		{
			Game.game.doMove(unit, getCursorPosition());
			Game.game.playSound("beep2");
		}
		else
		{
			complete();
		}
	}
	
	private void convecConstruct()
	{
		if (unit.isCargoEmpty())
			return;
		
		Player owner = unit.getOwner();
		String structTypeName = unit.getCargo().getStructureType();
		Unit struct = Game.game.getUnitFactory().newUnit(structTypeName, owner);
		push(new BuildStructureOverlay(unit, struct));
	}
	
	private void minerConstruct()
	{
		Player owner = unit.getOwner();
		Unit mine = Game.game.getUnitFactory().newUnit("eCommonMine", owner);
		push(new BuildMineOverlay(unit, mine));
	}
}
