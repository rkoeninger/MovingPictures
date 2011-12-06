package com.robbix.mp5.map;

import java.awt.geom.Point2D;

import com.robbix.utils.Position;

/**
 * An object on the map. May be a player unit or an environmental object.
 */
public abstract class Entity
{
	public abstract Position getPosition();
	public abstract Point2D getAbsPoint();
	public abstract LayeredMap getContainer();
	public abstract void step();
	public abstract boolean isAlive();
}
