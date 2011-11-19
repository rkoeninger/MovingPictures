package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;

import com.robbix.mp5.Game;
import com.robbix.mp5.unit.Unit;

public class SelectAttackTargetOverlay extends InputOverlay
{
	private Unit attacker;
	
	public SelectAttackTargetOverlay(Unit unit)
	{
		super("attack");
		
		this.attacker = unit;
	}
	
	public void paintImpl(Graphics g)
	{
		drawSelectedUnitBox(g, attacker.getChassis());
	}
	
	public void onLeftClick()
	{
		Unit target = panel.getMap().getUnit(getCursorPosition());
		
		if (target != null && !target.isAt(attacker.getPosition()))
		{
			Game.game.doAttack(attacker, target);
			complete();
		}
	}
}
