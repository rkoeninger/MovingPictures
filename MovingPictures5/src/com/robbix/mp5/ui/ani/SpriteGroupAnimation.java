package com.robbix.mp5.ui.ani;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.utils.RColor;

public class SpriteGroupAnimation extends AmbientAnimation
{
	private SpriteGroup sprites;
	private Point2D point;
	private int frame = 0;
	private Player player;
	
	public SpriteGroupAnimation(SpriteGroup group, Point2D point)
	{
		this(group, null, point, 1);
	}
	
	public SpriteGroupAnimation(SpriteGroup group, Point2D point, int delay)
	{
		this(group, null, point, delay);
	}
	
	public SpriteGroupAnimation(SpriteGroup group, Player player, Point2D point)
	{
		this(group, player, point, 1);
	}
	
	public SpriteGroupAnimation(SpriteGroup group, Player player, Point2D point, int delay)
	{
		super(Game.game.getSpriteLibrary());
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
	
	public void step()
	{
		frame++;
	}
	
	public void paint(DisplayGraphics g)
	{
		RColor color = player != null ? player.getColor() : null;
		g.draw(sprites.getFrame(frame), point, color);
	}
	
	public boolean isDone()
	{
		return frame >= sprites.getFrameCount();
	}
}
