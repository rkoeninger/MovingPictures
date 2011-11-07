package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class LaserAnimation extends BeamAnimation
{
	public Stroke getStroke(int frame, int scale)
	{
		return new BasicStroke(
			scale > 0 ? 2 << scale : 2 >> -scale,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND
		);
	}
	
	public Color getColor(int frame)
	{
		switch ((frame / 4) % 4)
		{
		case 0: return new Color(255, 0,   64);
		case 1: return new Color(255, 64,  32);
		case 2: return new Color(255, 128, 0);
		case 3: return new Color(255, 64,  32);
		}
		
		throw new IllegalArgumentException("Invalid frame " + frame);
	}
	
	public LaserAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target, "laser", attacker.getType().getDamage());
	}
}
