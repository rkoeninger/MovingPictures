package com.robbix.mp5.basics.sound;

import java.util.Collection;

public class StringHashedSet extends ArraySet<Object>
{
	private static final long serialVersionUID = 1L;

	public StringHashedSet()
	{
		super();
	}
	
	public StringHashedSet(Collection<? extends Object> c)
	{
		super(c);
	}
	
	public boolean add(Object element)
	{
		return element == null ? false : super.add(element);
	}
	
	public boolean contains(Object element)
	{
		if (element == null)
			return false;
		
		String string = element.toString();
		
		for (Object obj : this)
			if (string.equals(obj.toString()))
				return true;
		
		return false;
	}
	
	public Object get(Object element)
	{
		if (element == null)
			return null;
		
		String string = element.toString();
		
		for (Object obj : this)
			if (string.equals(obj.toString()))
				return obj;
		
		return null;
	}
	
	public void add(int index, Object element)
	{
		throw new UnsupportedOperationException("add");
	}
	
	public Object set(int index, Object element)
	{
		throw new UnsupportedOperationException("set");
	}
}
