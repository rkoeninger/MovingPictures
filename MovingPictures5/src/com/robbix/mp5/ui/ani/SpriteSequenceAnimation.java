package com.robbix.mp5.ui.ani;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import com.robbix.mp5.ui.Sprite;


public class SpriteSequenceAnimation extends AmbientAnimation
{
	private List<Sprite> sprites;
	private Point point;
	
	private int frame = 0;
	private int delay;
	
	private int hue = -1;
	
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
		this.sprites = sprites;
		this.hue = hue;
		this.point = point;
		this.delay = delay;
	}
	
	public Rectangle getBounds()
	{
		Sprite sprite = sprites.get(frame / delay);
		int x = point.x + sprite.getXOffset();
		int y = point.y + sprite.getYOffset();
		int w = sprite.getImage().getWidth(null);
		int h = sprite.getImage().getHeight(null);
		
		return new Rectangle(x, y, w, h);
	}
	
	public void step()
	{
		frame++;
	}
	
	public void paint(Graphics g)
	{
		Sprite sprite = sprites.get(frame / delay);
		g.drawImage(
			hue < 0 ? sprite.getImage() : sprite.getImage(hue),
			point.x + sprite.getXOffset(),
			point.y + sprite.getYOffset(),
			null
		);
	}
	
	public boolean isDone()
	{
		return (frame / delay) >= sprites.size();
	}
}
