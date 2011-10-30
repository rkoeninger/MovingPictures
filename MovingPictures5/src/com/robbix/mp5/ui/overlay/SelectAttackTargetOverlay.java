package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;

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
	
	public void paintOverUnits(Graphics g)
	{
		drawSelectedUnitBox(g, attacker.getChassis());
	}
	
	public void onLeftClick()
	{
		Unit target = panel.getMap().getUnit(getCursorPosition());
		
		if (target != null && !target.isAt(attacker.getPosition()))
		{
			Mediator.doAttack(attacker, target);
			complete();
		}
	}
}
