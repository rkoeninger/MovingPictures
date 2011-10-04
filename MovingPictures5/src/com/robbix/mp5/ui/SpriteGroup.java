package com.robbix.mp5.ui;

import java.util.Arrays;
import java.util.List;

public class SpriteGroup
{
	private Sprite[] sprites;
	private boolean looped;
	private int delay;
	
	public SpriteGroup(Sprite sprite)
	{
		this(sprite, false);
	}
	
	public SpriteGroup(Sprite sprite, boolean looped)
	{
		this.sprites = new Sprite[]{sprite};
		this.looped = looped;
		this.delay = 1;
	}
	
	public SpriteGroup(List<Sprite> sprites)
	{
		this(sprites, false, 1);
	}
	
	public SpriteGroup(List<Sprite> sprites, boolean looped)
	{
		this(sprites, looped, 1);
	}
	
	public SpriteGroup(List<Sprite> sprites, int delay)
	{
		this(sprites, false, delay);
	}
	
	public SpriteGroup(List<Sprite> sprites, boolean looped, int delay)
	{
		this.sprites = sprites.toArray(new Sprite[sprites.size()]);
		this.looped = looped;
		this.delay = delay;
	}
	
	public boolean isLooped()
	{
		return looped;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public int getFrameCount()
	{
		return sprites.length * delay;
	}
	
	public Sprite getFrame(int frame)
	{
		frame /= delay;
		
		if (looped)
		{
			int len = sprites.length;
			frame = (frame % len + len) % len;
		}
		
		return sprites[frame];
	}
	
	public int getSpriteCount()
	{
		return sprites.length;
	}
	
	public Sprite getSprite(int index)
	{
		return sprites[index];
	}
	
	public Sprite getFirst()
	{
		return sprites[0];
	}
	
	public List<Sprite> getSprites()
	{
		return Arrays.asList(sprites);
	}
}
