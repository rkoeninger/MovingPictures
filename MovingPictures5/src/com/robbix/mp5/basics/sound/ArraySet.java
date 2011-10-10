package com.robbix.mp5.basics.sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ArraySet<E> extends ArrayList<E> implements Set<E>
{
	private static final long serialVersionUID = 1L;

	public ArraySet()
	{
		super();
	}
	
	public ArraySet(Collection<? extends E> c)
	{
		this();
		addAll(c);
	}
	
	public boolean add(E element)
	{
		return contains(element) ? false : super.add(element);
	}
}
