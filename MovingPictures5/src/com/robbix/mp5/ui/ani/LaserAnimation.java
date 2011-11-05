package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class LaserAnimation extends BeamAnimation
{
	private static Color color = Utils.getTranslucency(Color.RED, 193);
	
	public Stroke getStroke(int scale)
	{
		return new BasicStroke(
			scale > 0 ? 2 << scale : 2 >> -scale,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND
		);
	}
	
	public LaserAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target, color, "laser", attacker.getType().getDamage());
	}
}
