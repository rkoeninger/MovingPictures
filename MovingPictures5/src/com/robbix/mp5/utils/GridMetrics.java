package com.robbix.mp5.utils;

import java.awt.Point;

public class GridMetrics
{
	public final int xOffset;
	public final int yOffset;
	public final int tileSize;
	public final int scale;
	
	public GridMetrics(int xOffset, int yOffset, int tileSize, int scale)
	{
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.tileSize = tileSize;
		this.scale = scale;
	}
	
	public GridMetrics()
	{
		this(0, 0, 1, 1);
	}
	
	public Point getOffset() { return new Point(xOffset, yOffset); }
	public int getXOffset()  { return xOffset; }
	public int getYOffset()  { return yOffset; }
	public int getTileSize() { return tileSize; }
}
