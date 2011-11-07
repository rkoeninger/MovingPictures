package com.robbix.mp5.ui.ani;

import static java.lang.Math.max;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class MicrowaveAnimation extends BeamAnimation
{
	public Stroke getStroke(int frame, int scale)
	{
		return new BasicStroke(
			scale > 0 ? 2 << scale : 2 >> -scale,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND,
			max(1.0f, scale > 0 ? 10 << scale : 10 >> -scale),
			new float[]{max(1, scale > 0 ? 4 << scale : 4 >> -scale)},
			frame / 4
		);
	}
	
	public Color getColor(int frame)
	{
		return new Color(224, 224, 224);
	}
	
	public MicrowaveAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target, "microwave", attacker.getType().getDamage());
	}
}
