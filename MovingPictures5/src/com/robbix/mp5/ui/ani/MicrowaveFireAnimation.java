package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class MicrowaveFireAnimation extends BeamFireAnimation
{
	private static Color color = Utils.getTranslucency(Color.WHITE, 193);
	private static Stroke stroke = new BasicStroke(
			2,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND,
			10,
			new float[]{10},
			0
		);
	
	public MicrowaveFireAnimation(SpriteLibrary lib, Unit attacker, Unit target)
	{
		super(lib, attacker, target, color, stroke, "microwave");
	}
}
