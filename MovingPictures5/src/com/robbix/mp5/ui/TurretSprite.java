package com.robbix.mp5.ui;

import java.awt.Image;
import java.awt.geom.Point2D;

import com.robbix.mp5.basics.Offset;

public class TurretSprite extends Sprite
{
	private Point2D[] hotspots;
	
	public TurretSprite(Image image, int baseHue, Offset offset, Point2D hotspot)
	{
		super(image, baseHue, offset);
		this.hotspots = new Point2D[]{hotspot};
	}
	
	public TurretSprite(Image image, int baseHue, Offset offset, Point2D[] hotspots)
	{
		super(image, baseHue, offset);
		
		if (hotspots.length == 0)
			throw new IllegalArgumentException("Turret must have at least 1 hotspot");
		
		this.hotspots = hotspots;
	}
	
	public Point2D getHotspot()
	{
		return hotspots[0];
	}
}
