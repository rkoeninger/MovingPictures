package com.robbix.mp5.ui;

import java.util.Collections;
import java.util.List;

public class EnumSpriteGroup<K extends Enum<K>> extends SpriteGroup
{
	private Class<K> enumType;
	
	/**
	 * Same sprite used for each element in the enum.
	 */
	public EnumSpriteGroup(Class<K> enumType, Sprite sprite)
	{
		super(Collections.nCopies(length(enumType), sprite));
		this.enumType = enumType;
	}
	
	/**
	 * Sprites in the list should be in the same order as the enum.
	 */
	public EnumSpriteGroup(Class<K> enumType, List<Sprite> sprites)
	{
		super(sprites);
		this.enumType = enumType;
		
		if (length(enumType) != sprites.size())
			throw new IllegalArgumentException("wrong number of sprites");
	}
	
	public Class<K> getEnumType()
	{
		return enumType;
	}
	
	public Sprite getFrame(K key)
	{
		return super.getFrame(key.ordinal());
	}
	
	private static <T extends Enum<T>> int length(Class<T> enumType)
	{
		return enumType.getEnumConstants().length;
	}
}
