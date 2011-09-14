package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JOptionPane;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.BulldozeTask;
import com.robbix.mp5.ai.task.DockTask;
import com.robbix.mp5.ai.task.DumpTask;
import com.robbix.mp5.ai.task.EarthworkerConstructTask;
import com.robbix.mp5.ai.task.MineTask;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.JListDialog;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

public class CommandUnitOverlay extends InputOverlay
{
	private Unit unit;
	
	public CommandUnitOverlay(Unit unit)
	{
		this.unit = unit;
	}
	
	public void init()
	{
		if (!unit.isStructure() && !unit.getType().isGuardPostType())
			panel.setAnimatedCursor("move");
	}
	
	public void dispose()
	{
		panel.showStatus((Unit)null);
		panel.setAnimatedCursor(null);
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
	
	public static void paintSelectedUnitBox(Graphics g, Unit unit)
	{
		if (unit.isDead() || unit.isFloating()) return;
		
		int tileSize = unit.getMap().getDisplayPanel().getTileSize();
		int absWidth = unit.getWidth() * tileSize;
		int absHeight = unit.getHeight() * tileSize;
		
		/*
		 * Draw borders
		 */
		int nwCornerX = unit.getAbsX();
		int nwCornerY = unit.getAbsY();
		int neCornerX = nwCornerX + absWidth;
		int neCornerY = nwCornerY;
		int swCornerX = nwCornerX;
		int swCornerY = nwCornerY + absHeight;
		int seCornerX = nwCornerX + absWidth;
		int seCornerY = nwCornerY + absHeight;
		
		g.setColor(Color.WHITE);
		g.drawLine(nwCornerX, nwCornerY, nwCornerX + 4, nwCornerY);
		g.drawLine(nwCornerX, nwCornerY, nwCornerX,     nwCornerY + 4);
		g.drawLine(neCornerX, neCornerY, neCornerX - 4, neCornerY);
		g.drawLine(neCornerX, neCornerY, neCornerX,     neCornerY + 4);
		g.drawLine(swCornerX, swCornerY, swCornerX + 4, swCornerY);
		g.drawLine(swCornerX, swCornerY, swCornerX,     swCornerY - 4);
		g.drawLine(seCornerX, seCornerY, seCornerX - 4, seCornerY);
		g.drawLine(seCornerX, seCornerY, seCornerX,     seCornerY - 4);
		
		/*
		 * Draw health bar
		 */
		double hpFactor = unit.getHP() / (double) unit.getType().getMaxHP();
		hpFactor = Math.min(hpFactor, 1.0f);
		hpFactor = Math.max(hpFactor, 0.0f);
		
		boolean isRed = unit.getHealthBracket() == HealthBracket.RED;
		
		int hpBarLength = absWidth - 14;
		int hpLength = (int) (hpBarLength * hpFactor);
		
		double hpHue = 1.0 - hpFactor;
		hpHue *= 0.333;
		hpHue = 0.333 - hpHue;
		
		double hpAlpha = 2.0 - hpFactor;
		hpAlpha *= 127.0;
		
		Color hpColor = Color.getHSBColor((float) hpHue, 1.0f, 1.0f);
		hpColor = new Color(
			hpColor.getRed(),
			hpColor.getGreen(),
			hpColor.getBlue(),
			(int) hpAlpha
		);
		
		g.setColor(Color.BLACK);
		g.fillRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
		
		if (Utils.getTimeBasedSwitch(300, 2) || !isRed)
		{
			g.setColor(hpColor);
			g.fillRect(nwCornerX + 8, nwCornerY - 1, hpLength - 1, 3);
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (unit.getPosition() == null)
			return;
		
		paintSelectedUnitBox(g, unit);
		
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		final int h = rect.height;
		g.setColor(Color.RED);
		
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Move", w / 2 - 25, 30);
		g.drawString("Middle Click on Command", w / 2 - 25, 50);
		g.drawString("Right Click to Cancel", w / 2 - 25, 70);
		
		g.setFont(Font.decode("Arial-bold-20"));
		g.drawString("Kill", 20, h - 30);
		g.drawString("SD", 20, h / 2 + 10);
		
		if (unit.isStructure())
		{
			g.drawString("Idle", w / 2 - 20, h - 30);
		}
		
		if (unit.getType().getName().contains("Truck"))
		{
			g.drawString("Dump", 20, 25);
			g.drawString("Mine", w - 50, 25);
			g.drawString("Route", w - 70, h / 2 - 30);
			g.drawString("Dock", w - 50, h - 30);
		}
		else if (unit.getType().getName().contains("ConVec"))
		{
			g.drawString("Construct", w - 100, 25);
			g.drawString("Dock", w - 50, h - 30);
		}
		else if (unit.getType().getName().contains("Earthworker"))
		{
			g.drawString("Build Wall", w - 100, 25);
			g.drawString("Build Tube", w - 100, h / 2 - 30);
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
			panel.pushOverlay(new SelectAttackTargetOverlay(unit.getTurret()));
		}
		else if (command.equals("stop"))
		{
			unit.cancelAssignments();
		}
		else if (command.equals("selfDestruct"))
		{
			Mediator.selfDestruct(unit);
			panel.completeOverlay(this);
		}
		else if (command.equals("dump") && unit.isTruck() && !unit.isCargoEmpty())
		{
			Mediator.sounds.play("dump");
			unit.interrupt(new DumpTask());
		}
		else if (command.equals("bulldoze") && unit.getType().getName().contains("Dozer"))
		{
			unit.interrupt(new BulldozeTask(19 * 4)); // Four strokes
		}
	}
	
	public void onMiddleClick(int x, int y)
	{
		final int w = panel.getVisibleRect().width;
		final int h = panel.getVisibleRect().height;
		final int x0 = panel.getVisibleRect().x;
		final int y0 = panel.getVisibleRect().y;
		
		int edge = ((x - x0) / (w / 3)) + (((y - y0) / (h / 3)) * 3);
		
		if (edge == 6)
		{
			Mediator.kill(unit);
			panel.completeOverlay(this);
			return;
		}
		else if (edge == 3)
		{
			Mediator.selfDestruct(unit);
			panel.completeOverlay(this);
			return;
		}
		
		if (unit.isStructure() && edge == 7)
		{
			if (unit.isIdle())
			{
				unit.activate();
			}
			else
			{
				unit.idle();
			}
			
			return;
		}
		
		if (unit.isTruck())
		{
			if (edge == 0)
			{
				if (unit.getCargo() != Cargo.EMPTY)
				{
					Mediator.sounds.play("dump");
					unit.interrupt(new DumpTask());
				}
			}
			else if (edge == 5)
			{
				panel.pushOverlay(new SelectMineRouteOverlay(unit));
			}
			else if (edge == 8)
			{
				if (unit.isCargoEmpty())
					return;
				
				Position adj = unit.getPosition().shift(0, -1);
				LayeredMap map = panel.getMap();
				
				if (map.getBounds().contains(adj))
				{
					Unit smelter = map.getUnit(adj);
					
					if (smelter != null && smelter.getType().getName().contains("Smelter"))
					{
						if (!smelter.isDead() && !smelter.isDisabled())
						{
							unit.assignNow(new DockTask(smelter, Cargo.EMPTY));
						}
					}
				}
			}
			else if (edge == 2)
			{
				Position adj = unit.getPosition().shift(1, 0);
				LayeredMap map = panel.getMap();
				
				if (map.getBounds().contains(adj))
				{
					Unit mine = map.getUnit(adj);
					ResourceDeposit deposit = map.getResourceDeposit(adj);
					
					if (mine != null && mine.getType().getName().contains("Mine"))
					{
						if (deposit == null)
							throw new IllegalStateException("mine doesn't have deposit");
						
						if (!mine.isDead() && !mine.isDisabled())
						{
							unit.assignNow(new MineTask(deposit.getLoad()));
						}
					}
				}
			}
		}
		else if (unit.getType().getName().contains("Earthworker"))
		{
			if (edge == 2)
			{
				Direction dir = unit.getDirection();
				Position buildPos = dir.apply(unit.getPosition());
				unit.assignNow(new EarthworkerConstructTask(
					buildPos,
					LayeredMap.Fixture.WALL,
					48
				));
			}
			else if (edge == 5)
			{
				panel.pushOverlay(new BuildTubeOverlay(unit));
			}
		}
		else if (unit.getType().getName().contains("ConVec"))
		{
			if (edge == 8)
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
			else if (edge == 2)
			{
				Mediator.doConVecConstruct(unit);
			}
		}
		else if (unit.hasTurret())
		{
			if (edge == 2)
			{
				panel.pushOverlay(new SelectAttackTargetOverlay(unit.getTurret()));
			}
		}
		else if (unit.isMiner())
		{
			if (edge == 2)
			{
				if (Mediator.doBuildMine(unit))
				{
					panel.completeOverlay(this);
				}
			}
		}
		else if (unit.getType().getName().contains("Dozer"))
		{
			if (edge == 2)
			{
				panel.pushOverlay(new SelectBulldozeOverlay(unit));
			}
		}
		else if (unit.getType().getName().contains("VehicleFactory"))
		{
			if (edge == 2)
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
			if (edge == 2)
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
		if (unit.isStructure() || unit.getType().isGuardPostType())
		{
			panel.completeOverlay(this);
			return;
		}
		
		Mediator.doMove(unit, panel.getPosition(x, y));
		Mediator.sounds.play("beep2");
	}
}
