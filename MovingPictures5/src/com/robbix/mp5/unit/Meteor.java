package com.robbix.mp5.unit;

import java.awt.geom.Point2D;

import com.robbix.mp5.map.Entity;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.utils.Position;

public class Meteor extends Entity
{
	private Position pos;
	
	public boolean isAlive()
	{
		return false;
	}
	
	public int getFormationTime()
	{
		return 0;
	}
	
	public int getImpactTime()
	{
		return 0;
	}
	
	public Position getPosition()
	{
		return pos;
	}
	
	public Point2D getAbsPoint()
	{
		return null;
	}
	
	public LayeredMap getContainer()
	{
		return null;
	}
	
	public void step()
	{
		
	}
}
