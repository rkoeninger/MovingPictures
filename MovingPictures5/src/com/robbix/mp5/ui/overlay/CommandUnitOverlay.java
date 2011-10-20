package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JOptionPane;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.ai.task.BulldozeTask;
import com.robbix.mp5.ai.task.DockTask;
import com.robbix.mp5.basics.JListDialog;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

public class CommandUnitOverlay extends InputOverlay
{
	private Unit unit;
	
	public CommandUnitOverlay(Unit unit)
	{
		super((!unit.isStructure() && !unit.getType().isGuardPostType()) ? "move" : null);
		
		this.unit = unit;
	}
	
	public void dispose()
	{
		panel.showStatus((Unit)null);
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (unit.getPosition() == null)
			return;
		
		InputOverlay.paintSelectedUnitBox(g, unit);
		
		drawInstructions(g, rect, "Move", "Command", "Cancel");
		
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		int w = rect.width;
		int h = rect.height;
		
		g.setFont(Font.decode("Arial-bold-20"));
		g.drawString("Kill", 20, h - 30);
		g.drawString("SD", 20, h / 2 + 10);
		
		if (unit.isStructure())
		{
			g.drawString("Idle", w / 2 - 20, h - 30);
		}
		
		if (unit.getType().getName().contains("ConVec"))
		{
			g.drawString("Construct", w - 100, 25);
			g.drawString("Dock", w - 50, h - 30);
		}
		else if (unit.getType().getName().contains("Earthworker"))
		{
			g.drawString("Build Tube", w - 115, h / 2 - 30);
		}
		else if (unit.hasTurret())
		{
			g.drawString("Attack", w - 60, 25);
		}
		else if (unit.getType().getName().contains("Miner"))
		{
			g.drawString("Build Mine", w - 100, 25);
		}
		else if (unit.getType().getName().contains("Dozer"))
		{
			g.drawString("Bulldoze", w - 100, 25);
		}
		else if (unit.getType().getName().contains("Factory"))
		{
			g.drawString("Build", w - 100, 25);
		}
		
		g.translate(-rect.x, -rect.y);
	}
	
	public void onCommand(String command)
	{
		if (command.equals("attack"))
		{
			push(new SelectAttackTargetOverlay(unit.getTurret()));
		}
		else if (command.equals("stop"))
		{
			unit.cancelAssignments();
		}
		else if (command.equals("selfDestruct"))
		{
			Mediator.selfDestruct(unit);
			complete();
		}
		else if (command.equals("bulldoze") && unit.getType().getName().contains("Dozer"))
		{
			unit.interrupt(new BulldozeTask(19 * 4)); // Four strokes
		}
	}
	
	public void onMiddleClick(int x, int y)
	{
		Point p = panel.subtractViewOffset(new Point(x, y));
		Edge edge = getPointEdge(p.x, p.y);
		
		if (edge == Edge.SW)
		{
			Mediator.kill(unit);
			complete();
		}
		else if (edge == Edge.W)
		{
			Mediator.selfDestruct(unit);
			complete();
		}
		else if (unit.isStructure() && edge == Edge.S)
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
		else if (unit.getType().getName().contains("Earthworker"))
		{
			if (edge == Edge.E)
			{
				push(new BuildTubeOverlay(unit));
			}
		}
		else if (unit.getType().getName().contains("ConVec"))
		{
			if (edge == Edge.SE)
			{
				Position adj = unit.getPosition().shift(0, -1);
				LayeredMap map = panel.getMap();
				
				if (map.getBounds().contains(adj))
				{
					Unit sFactory = map.getUnit(adj);
					
					if (sFactory != null
				&& sFactory.getType().getName().contains("StructureFactory")
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
			else if (edge == Edge.NE)
			{
				if (unit.isCargoEmpty())
					return;
				
				Player owner = unit.getOwner();
				String structTypeName = unit.getCargo().getStructureType();
				Unit struct = Mediator.factory.newUnit(structTypeName, owner);
				push(new BuildStructureOverlay(unit, struct));
			}
		}
		else if (unit.hasTurret())
		{
			if (edge == Edge.NE)
			{
				push(new SelectAttackTargetOverlay(unit.getTurret()));
			}
		}
		else if (unit.isMiner())
		{
			if (edge == Edge.NE)
			{
				Player owner = unit.getOwner();
				Unit mine = Mediator.factory.newUnit("eCommonMine", owner);
				push(new BuildMineOverlay(unit, mine));
			}
		}
		else if (unit.getType().getName().contains("Dozer"))
		{
			if (edge == Edge.NE)
			{
				push(new SelectBulldozeOverlay(unit));
			}
		}
		else if (unit.getType().getName().contains("VehicleFactory"))
		{
			if (edge == Edge.NE)
			{
				List<UnitType> vehicleTypes = Mediator.factory.getVehicleTypes();
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
				Unit newVehicle = Mediator.factory.newUnit(type, owner);
				
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
		}
		else if (unit.getType().getName().contains("StructureFactory"))
		{
			if (edge == Edge.NE)
			{
				List<UnitType> structTypes = Mediator.factory.getStructureTypes();
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
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		if (!(unit.isStructure() || unit.getType().isGuardPostType()))
		{
			Mediator.doMove(unit, panel.getPosition(x, y));
			Mediator.playSound("beep2");
		}
		else
		{
			complete();
		}
	}
}
