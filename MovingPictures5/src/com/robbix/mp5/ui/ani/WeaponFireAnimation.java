package com.robbix.mp5.ui.ani;

import java.awt.Point;
import java.awt.Rectangle;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public abstract class WeaponFireAnimation extends AmbientAnimation
{
	private Unit attacker;
	private Unit target;
	private Point start;
	private Point end;
	private Rectangle bounds;
	
	public WeaponFireAnimation(Unit attacker, Unit target)
	{
		this.attacker = attacker;
		this.target = target;
		
		Unit turret = attacker;
		Unit chassis = attacker.getChassis();
		LayeredMap map = chassis.getMap();
		
		int tileSize = map.getDisplayPanel().getTileSize();
		
		SpriteLibrary lib = map.getDisplayPanel().getSpriteLibrary();
		
		Point hotspot = lib.getHotspot(turret, Direction.getDirection(
			attacker.getPosition(), target.getPosition()));
		
		Sprite turretSprite = turret.getSprite(lib);
		
		int xSpriteOffset = turretSprite.getXOffset() + hotspot.x;
		int ySpriteOffset = turretSprite.getYOffset() + hotspot.y;
		int startX = attacker.getAbsX() + xSpriteOffset;
		int startY = attacker.getAbsY() + ySpriteOffset;
		start = new Point(startX, startY);
		
		int w = target.getWidth();
		int h = target.getHeight();
		int spreadW = tileSize / 2 + tileSize * (w - 1);
		int spreadH = tileSize / 2 + tileSize * (h - 1);
		int xTargetOffset = (tileSize * w / 2) + Utils.randInt(-spreadW, spreadH);
		int yTargetOffset = (tileSize * h / 2) + Utils.randInt(-spreadW, spreadH);
		int endX = target.getAbsX() + xTargetOffset;
		int endY = target.getAbsY() + yTargetOffset;
		end = new Point(endX, endY);
		
		bounds = new Rectangle(
			startX,
			startY,
			endX - startX,
			endY - startY
		);
		
		if (bounds.width < 0)
		{
			bounds.x += bounds.width;
			bounds.width = -bounds.width;
		}
		
		if (bounds.height < 0)
		{
			bounds.y += bounds.height;
			bounds.height = -bounds.height;
		}
		
		bounds.x -= tileSize;
		bounds.y -= tileSize;
		bounds.width  += tileSize * 2;
		bounds.height += tileSize * 2;
	}
	
	public Unit getAttacker()
	{
		return attacker;
	}
	
	public Unit getTarget()
	{
		return target;
	}
	
	public Point getFireOrigin()
	{
		return start;
	}
	
	public Point getFireImpact()
	{
		return end;
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	/**
	 * Returns true if the animation is at the frame where damage would be done.
	 */
	public abstract boolean atHotPoint();
}
