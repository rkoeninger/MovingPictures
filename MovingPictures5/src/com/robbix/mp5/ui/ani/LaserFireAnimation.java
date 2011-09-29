package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import com.robbix.mp5.Utils;
import com.robbix.mp5.unit.Unit;

public class LaserFireAnimation extends BeamFireAnimation
{
	private static Color color = Utils.getTranslucency(Color.RED, 193);
	private static Stroke stroke = new BasicStroke(
		2,
		BasicStroke.CAP_ROUND,
		BasicStroke.JOIN_ROUND
	);
	
	public LaserFireAnimation(Unit attacker, Unit target)
	{
		super(attacker, target, color, stroke, "laser");
	}
}
