package com.robbix.mp5.ui;

import java.awt.Image;
import java.awt.Point;

import com.robbix.mp5.basics.Offset;

public class TurretSprite extends Sprite
{
	private Point[] hotspots;
	
	public TurretSprite(Image image, int baseHue, Offset offset, Point hotspot)
	{
		super(image, baseHue, offset);
		this.hotspots = new Point[]{hotspot};
	}
	
	public TurretSprite(Image image, int baseHue, Offset offset, Point[] hotspots)
	{
		super(image, baseHue, offset);
		
		if (hotspots.length == 0)
			throw new IllegalArgumentException("Turret must have at least 1 hotspot");
		
		this.hotspots = hotspots;
	}
	
	public Point getHotspot()
	{
		return hotspots[0];
	}
}
