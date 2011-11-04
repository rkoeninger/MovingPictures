package com.robbix.mp5.ui.ani;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;

public class BeamFireAnimation extends WeaponFireAnimation
{
	private int frame = 0;
	private final int frameLength = 20;
	
	private Color color;
	private Stroke stroke;
	private String soundBite;
	
	public BeamFireAnimation(SpriteLibrary lib, Unit attacker, Unit target, Color color, Stroke stroke, String soundBite)
	{
		super(lib, attacker, target);
		this.color = color;
		this.stroke = stroke;
		this.soundBite = soundBite;
	}
	
	public void paint(Graphics g)
	{
		g.setColor(color);
		Stroke oldStroke = ((Graphics2D) g).getStroke();
		((Graphics2D) g).setStroke(stroke);
		panel.draw(g, getTrackedFireOrigin(), getTrackedFireImpact());
		((Graphics2D) g).setStroke(oldStroke);
	}
	
	public void step(AtomicReference<Runnable> callback)
	{
		if (frame == 0)
		{
			playSoundLater(soundBite, getAttacker().getPosition());
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
