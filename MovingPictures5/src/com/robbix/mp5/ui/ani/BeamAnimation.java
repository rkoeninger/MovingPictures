package com.robbix.mp5.ui.ani;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public abstract class BeamAnimation extends WeaponAnimation
{
	private int frame = 0;
	private final int frameLength = 20;
	
	private Color color;
	private String soundBite;
	private double damage;
	
	public BeamAnimation(SpriteLibrary lib, Unit attacker, Unit target, Color color, String soundBite, double damage)
	{
		super(lib, attacker, target);
		this.color = color;
		this.soundBite = soundBite;
		this.damage = damage;
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(color);
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(getStroke(panel.getScale()));
		panel.draw(g, getTrackedFireOrigin(), getTrackedFireImpact());
		g2d.setStroke(oldStroke);
	}
	
	public abstract Stroke getStroke(int scale);
	
	public void step(AtomicReference<Runnable> callback)
	{
		if (frame == 0)
		{
			playSoundLater(soundBite, getAttacker().getPosition());
		}
		
		if (atHotPoint())
		{
			doDamageLater(getAttacker(), getTarget(), damage);
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
