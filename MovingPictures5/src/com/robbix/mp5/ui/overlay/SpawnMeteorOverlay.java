package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;

public class SpawnMeteorOverlay extends InputOverlay
{
	public SpawnMeteorOverlay()
	{
		super("meteor");
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawInstructions(g, rect, "Spawn Meteor", "Cancel");
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
