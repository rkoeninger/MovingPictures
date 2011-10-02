package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;


public class SpriteSequenceAnimation extends AmbientAnimation
{
	private SpriteGroup sprites;
	private Point point;
	
	private int frame = 0;
	
	private int hue = -1;
	
	public SpriteSequenceAnimation(SpriteGroup group, Point point)
	{
		this(group, -1, point, 1);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, Point point, int delay)
	{
		this(group, -1, point, delay);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, int hue, Point point)
	{
		this(group, hue, point, 1);
	}
	
	public SpriteSequenceAnimation(SpriteGroup group, int hue, Point point, int delay)
	{
		this.sprites = group;
		this.hue = hue;
		this.point = point;
	}
	
	public SpriteSequenceAnimation(List<Sprite> sprites, Point point)
	{
		this(sprites, -1, point, 1);
	}
	
	public SpriteSequenceAnimation(List<Sprite> sprites, Point point, int delay)
	{
		this(sprites, -1, point, delay);
	}
	
	public SpriteSequenceAnimation(List<Sprite> sprites, int hue, Point point)
	{
		this(sprites, hue, point, 1);
	}
	
	public SpriteSequenceAnimation(List<Sprite> sprites, int hue, Point point, int delay)
	{
		this.sprites = new SpriteGroup(sprites, delay);
		this.hue = hue;
		this.point = point;
	}
	
	public Rectangle getBounds()
	{
		Sprite sprite = sprites.getFrame(frame);
		int x = point.x + sprite.getXOffset();
		int y = point.y + sprite.getYOffset();
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
		Sprite sprite = sprites.getFrame(frame);
		g.drawImage(
			hue < 0 ? sprite.getImage() : sprite.getImage(hue),
			point.x + sprite.getXOffset(),
			point.y + sprite.getYOffset(),
			null
		);
	}
	
	public boolean isDone()
	{
		return frame >= sprites.getFrameCount();
	}
}
