package com.robbix.mp5.ui.ani;

import java.awt.Color;
import java.awt.Stroke;

import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.RGraphics;

public abstract class BeamAnimation extends WeaponAnimation
{
	private int frame = 0;
	private final int frameLength = 20;
	
	private String soundBite;
	private double damage;
	
	public BeamAnimation(SpriteLibrary lib, Unit attacker, Unit target, String soundBite, double damage)
	{
		super(lib, attacker, target);
		this.soundBite = soundBite;
		this.damage = damage;
	}
	
	public void paint(RGraphics g)
	{
		g.setColor(getColor(frame));
		Stroke oldStroke = g.getStroke();
		g.setStroke(getStroke(frame, panel.getScale()));
		g.drawLine(getTrackedFireOrigin(), getTrackedFireImpact());
		g.setStroke(oldStroke);
	}
	
	public abstract Stroke getStroke(int frame, int scale);
	
	public abstract Color getColor(int frame);
	
	public void step()
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
