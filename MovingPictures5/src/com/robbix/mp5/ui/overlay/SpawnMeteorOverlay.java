package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;

public class SpawnMeteorOverlay extends InputOverlay
{
	public void init()
	{
		panel.setAnimatedCursor("attack");
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.drawString("Left Click to Spawn Meteor", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
	}
	
	public void onLeftClick(int x, int y)
	{
		Mediator.doSpawnMeteor(panel.getPosition(x, y));
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
