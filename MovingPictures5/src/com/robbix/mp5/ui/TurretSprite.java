package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.geom.Point2D;

import com.robbix.utils.Offset;
import com.robbix.utils.RImage;

public class TurretSprite extends Sprite
{
	private Point2D[] hotspots;
	
	public TurretSprite(RImage image, Color color, Offset offset, Point2D hotspot)
	{
		super(image, color, offset);
		this.hotspots = new Point2D[]{hotspot};
	}
	
	public TurretSprite(RImage image, Color color, Offset offset, Point2D[] hotspots)
	{
		super(image, color, offset);
		
		if (hotspots.length == 0)
			throw new IllegalArgumentException("Turret must have at least 1 hotspot");
		
		this.hotspots = hotspots;
	}
	
	public Point2D getHotspot()
	{
		return hotspots[0];
	}
}
