package com.robbix.mp5.ui.ani;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.unit.Unit;

public class BeamFireAnimation extends WeaponFireAnimation
{
	private Point attackerStart;
	private Point targetStart;
	
	private int frame = 0;
	private final int frameLength = 20;
	
	private Color color;
	private Stroke stroke;
	private String soundBite;
	
	public BeamFireAnimation(Unit attacker, Unit target, Color color, Stroke stroke, String soundBite)
	{
		super(attacker, target);
		attackerStart = new Point(attacker.getAbsX(), attacker.getAbsY());
		targetStart = new Point(target.getAbsX(), target.getAbsY());
		this.color = color;
		this.stroke = stroke;
		this.soundBite = soundBite;
	}
	
	public void paint(Graphics g)
	{
		g.setColor(color);
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
					Mediator.playSound(soundBite, getAttacker().getPosition());
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
