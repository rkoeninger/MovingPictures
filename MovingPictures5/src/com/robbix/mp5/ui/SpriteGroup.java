package com.robbix.mp5.ui;

import java.util.Arrays;
import java.util.List;

public class SpriteGroup
{
	private Sprite[] sprites;
	private boolean looped;
	private int delay;
	private Class<? extends Enum<?>> enumType;
	
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
	
	public <E extends Enum<E>> SpriteGroup(Sprite sprite, Class<E> enumType)
	{
		this.sprites = new Sprite[enumType.getEnumConstants().length];
		Arrays.fill(sprites, sprite);
		this.looped = false;
		this.delay = 1;
		this.enumType = enumType;
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
	
	public <E extends Enum<E>> SpriteGroup(List<Sprite> sprites, Class<E> enumType)
	{
		if (sprites.size() != enumType.getEnumConstants().length)
			throw new IllegalArgumentException("Wrong number of sprites");
		
		this.sprites = sprites.toArray(new Sprite[sprites.size()]);
		this.looped = false;
		this.delay = 1;
		this.enumType = enumType;
	}
	
	public boolean isEnumGroup()
	{
		return enumType != null;
	}
	
	public Class<? extends Enum<?>> getEnumType()
	{
		return enumType;
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
	
	public <E extends Enum<E>> Sprite getFrame(E enumValue)
	{
		return getSprite(enumValue);
	}
	
	public int getSpriteCount()
	{
		return sprites.length;
	}
	
	public Sprite getSprite(int index)
	{
		return sprites[index];
	}
	
	public <E extends Enum<E>> Sprite getSprite(E enumValue)
	{
		if (!enumType.isInstance(enumValue))
			throw new IllegalArgumentException(enumValue + " is not of type " + enumType);
		
		return sprites[enumValue.ordinal()];
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
