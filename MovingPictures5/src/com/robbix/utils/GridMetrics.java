package com.robbix.utils;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

import java.awt.Point;
import java.awt.Rectangle;

public class GridMetrics
{
	public int xOffset;
	public int yOffset;
	public int tileSize;
	public int scale;
	
	public GridMetrics(int xOffset, int yOffset, int tileSize, int scale)
	{
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.tileSize = tileSize;
		this.scale = scale;
	}
	
	public GridMetrics()
	{
		this(0, 0, 1, 0);
	}
	
	public Position getPosition(Point point)
	{
		int x = (point.x - xOffset) / tileSize;
		int y = (point.y - yOffset) / tileSize;
		return new Position(x, y);
	}
	
	public Point getPoint(Position pos)
	{
		int x = pos.x * tileSize + xOffset;
		int y = pos.y * tileSize + yOffset;
		return new Point(x, y);
	}
	
	public Point getCenteredPoint(Position pos)
	{
		int half = tileSize / 2;
		int x = pos.x * tileSize + half + xOffset;
		int y = pos.y * tileSize + half + yOffset;
		return new Point(x, y);
	}
	
	public Region getRegion(Rectangle rect)
	{
		int minX = (int) floor((rect.x - xOffset) / (double) tileSize);
		int minY = (int) floor((rect.y - yOffset) / (double) tileSize);
		int maxX = (int) ceil((rect.x - xOffset + rect.width) / (double) tileSize);
		int maxY = (int) ceil((rect.y - yOffset + rect.height) / (double) tileSize);
		return new Region(minX, minY, maxX - minX, maxY - minY);
	}
	
	public Region getEnclosedRegion(Rectangle rect)
	{
		int minX = (int) ceil((rect.x - xOffset) / (double) tileSize);
		int minY = (int) ceil((rect.y - yOffset) / (double) tileSize);
		int maxX = (int) floor((rect.x - xOffset + rect.width) / (double) tileSize);
		int maxY = (int) floor((rect.y - yOffset + rect.height) / (double) tileSize);
		return new Region(minX, minY, maxX - minX, maxY - minY);
	}
	
	public Rectangle getRectangle(Region region)
	{
		int x = region.x * tileSize + xOffset;
		int y = region.y * tileSize + yOffset;
		int w = region.w * tileSize;
		int h = region.h * tileSize;
		return new Rectangle(x, y, w, h);
	}
	
	public void setOffset(Point p)
	{
		xOffset = p.x;
		yOffset = p.y;
	}
	
	public void getOffset(Point p)
	{
		p.x = xOffset;
		p.y = yOffset;
	}
	
	public Point getOffset()
	{
		return new Point(xOffset, yOffset);
	}
}
