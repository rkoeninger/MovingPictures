package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.unit.Unit;

public class SelectAttackTargetOverlay extends InputOverlay
{
	private Unit unit;
	
	public SelectAttackTargetOverlay(Unit unit)
	{
		this.unit = unit;
	}
	
	public void init()
	{
		panel.setAnimatedCursor("attack");
	}
	
	public void dispose()
	{
		panel.setAnimatedCursor(null);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		CommandUnitOverlay.paintSelectedUnitBox(g, unit.getChassis());
		
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Select Target", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
	}
	
	public void onLeftClick(int x, int y)
	{
		Unit target = panel.getMap().getUnitAbsolute(x, y);
		
		if (target != null && !target.isAt(unit.getPosition()))
		{
			Mediator.doAttack(unit, target);
			panel.completeOverlay(this);
		}
	}
	
	public void onRightClick(int x, int y)
	{
		panel.completeOverlay(this);
	}
}
