package com.robbix.mp5.ui.ani;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.unit.Unit;

public class MicrowaveFireAnimation extends WeaponFireAnimation
{
	private Point attackerStart;
	private Point targetStart;
	
	private int frame = 0;
	private final int frameLength = 20;
	
	public MicrowaveFireAnimation(Unit attacker, Unit target)
	{
		super(attacker, target);
		attackerStart = new Point(attacker.getAbsX(), attacker.getAbsY());
		targetStart = new Point(target.getAbsX(), target.getAbsY());
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
		Point attackerCurrent = new Point(getAttacker().getAbsX(), getAttacker().getAbsY());
		Point targetCurrent = new Point(getTarget().getAbsX(), getTarget().getAbsY());
		g.drawLine(
			getFireOrigin().x - attackerStart.x + attackerCurrent.x,
			getFireOrigin().y - attackerStart.y + attackerCurrent.y,
			getFireImpact().x - targetStart.x + targetCurrent.x,
			getFireImpact().y - targetStart.y + targetCurrent.y
		);
		((Graphics2D) g).setStroke(oldStroke);
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
