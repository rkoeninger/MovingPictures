package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;

public class SpriteSequenceAnimation extends AmbientAnimation
{
	private SpriteGroup sprites;
	private Point2D point;
	private int frame = 0;
	private Player player;
	
	public SpriteSequenceAnimation(SpriteGroup group, Point2D point)
	{
		this(group, null, point, 1);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, Point2D point, int delay)
	{
		this(group, null, point, delay);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, Player player, Point2D point)
	{
		this(group, player, point, 1);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, Player player, Point2D point, int delay)
	{
		super(Mediator.game.getSpriteLibrary());
		this.sprites = group;
		this.player = player;
		this.point = point;
	}
	
	public Rectangle2D getBounds()
	{
		Sprite sprite = sprites.getFrame(frame);
		int x = (int) point.getX() + sprite.getXOffset();
		int y = (int) point.getY() + sprite.getYOffset();
		int w = sprite.getImage().getWidth(null);
		int h = sprite.getImage().getHeight(null);
		
		return new Rectangle(x, y, w, h);
	}
	
	public void step(AtomicReference<Runnable> ref)
	{
		frame++;
	}
	
	public void paint(Graphics g)
	{
		panel.draw(g, sprites.getFrame(frame), point, player);
	}
	
	public boolean isDone()
	{
		return frame >= sprites.getFrameCount();
	}
}
