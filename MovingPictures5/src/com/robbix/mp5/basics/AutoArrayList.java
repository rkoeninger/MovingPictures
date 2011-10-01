package com.robbix.mp5.basics;

import java.util.ArrayList;

/**
 * An extension of java.util.ArrayList that automatically resizes to
 * accommodate an element being set or inserted using
 * {@code set(int, E)} or {@code add(int, E)}.
 */
public class AutoArrayList<E> extends ArrayList<E>
{
	private static final long serialVersionUID = 2683149508996716586L;
	
	public E set(int index, E element)
	{
		int newSize = index + 1;
		ensureCapacity(newSize);
		
		while (size() < newSize)
			add(null);
		
		return super.set(index, element);
	}
	
	public void add(int index, E element)
	{
		if (index <= size())
		{
			super.add(index, element);
		}
		else
		{
			set(index, element);
		}
	}
}
