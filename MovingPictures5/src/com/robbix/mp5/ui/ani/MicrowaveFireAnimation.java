package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class MicrowaveFireAnimation extends WeaponFireAnimation
{
	private Unit unit, target;
	private Point hotspot;
	private int xSpriteOffset, ySpriteOffset;
	private int xTargetOffset, yTargetOffset;
	
	private Rectangle bounds;
	
	private int frame = 0;
	private int tileSize;
	private final int frameLength = 20;
	
	public MicrowaveFireAnimation(Unit unit, Unit target)
	{
		super(unit, target);
		
		Unit turret = unit;
		Unit chassis = unit.getChassis();
		LayeredMap map = chassis.getMap();
		
		tileSize = map.getDisplayPanel().getTileSize();
		
		SpriteLibrary lib = map.getDisplayPanel().getSpriteLibrary();
		
		hotspot = lib.getHotspot(turret, Direction.getDirection(
			unit.getPosition(), target.getPosition()));
		
		this.unit = turret;
		this.target = target;
		
		Sprite turretSprite = lib.getSprite(turret);
		
		xSpriteOffset = turretSprite.getXOffset() + hotspot.x;
		ySpriteOffset = turretSprite.getYOffset() + hotspot.y;
		
		int startX = unit.getAbsX() + xSpriteOffset;
		int startY = unit.getAbsY() + ySpriteOffset;
		
		int w = target.getWidth();
		int h = target.getHeight();
		
		xTargetOffset = (tileSize * w / 2) + Utils.randInt(-5, 5);
		yTargetOffset = (tileSize * h / 2) + Utils.randInt(-5, 5);
		
		int endX = target.getAbsX() + xTargetOffset;
		int endY = target.getAbsY() + yTargetOffset;
		
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
	}
	
	public Rectangle getBounds()
	{
		return bounds;
	}

	private static final Stroke stroke = new BasicStroke(
		2,
		BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND,
		10,
		new float[]{10},
		0
	);
	
	public void paint(Graphics g)
	{
		g.setColor(Utils.getTranslucency(Color.WHITE, 193));
		Stroke oldStroke = ((Graphics2D) g).getStroke();
		((Graphics2D) g).setStroke(stroke);
		g.drawLine(
			unit.getAbsX() + xSpriteOffset,
			unit.getAbsY() + ySpriteOffset,
			target.getAbsX() + xTargetOffset,
			target.getAbsY() + yTargetOffset);
		((Graphics2D) g).setStroke(oldStroke);
	}

	public boolean hasCallback()
	{
		return true;
	}
	
	public void step()
	{
		throw new Error();
	}
	
	public void step(AtomicReference<Runnable> callback)
	{
		if (frame == 0)
		{
			callback.set(new Runnable()
			{
				public void run()
				{
					Mediator.sounds.play("microwave");
				}
			});
		}
		
		frame++;
	}
	
	public boolean atHotPoint()
	{
		return frame == frameLength / 2;
	}

	public boolean isDone()
	{
		return frame >= frameLength;
	}
}
