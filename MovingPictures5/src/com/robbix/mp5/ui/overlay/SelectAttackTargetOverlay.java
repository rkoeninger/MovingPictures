package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.unit.Unit;

public class SelectAttackTargetOverlay extends InputOverlay
{
	private Unit attacker;
	
	public SelectAttackTargetOverlay(Unit unit)
	{
		super("attack");
		
		this.attacker = unit;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		InputOverlay.paintSelectedUnitBox(g, attacker.getChassis());
		
		drawInstructions(g, rect, "Select Target", "Cancel");
	}
	
	public void onLeftClick(int x, int y)
	{
		Unit target = panel.getMap().getUnitAbsolute(x, y);
		
		if (target != null && !target.isAt(attacker.getPosition()))
		{
			Mediator.doAttack(attacker, target);
			complete();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
