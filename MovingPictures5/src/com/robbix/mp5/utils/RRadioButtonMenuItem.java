package com.robbix.mp5.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JMenuItem;

public class RRadioButtonMenuItem extends JMenuItem
{
	private static final long serialVersionUID = 1L;
	
	private Collection<Object> objects = new ArrayList<Object>();
	
	public RRadioButtonMenuItem(String text, Object... objects)
	{
		super(text);
		add(objects);
	}
	
	public void set(Object... objects)
	{
		this.objects = Arrays.asList(objects);
	}
	
	public void add(Object... objects)
	{
		for (Object object : objects)
			this.objects.add(object);
	}
	
	public void remove(Object... objects)
	{
		for (Object object : objects)
			this.objects.remove(object);
	}
	
	public boolean has()
	{
		return !objects.isEmpty();
	}
	
	public Object[] get()
	{
		return objects.toArray();
	}
	
	public <T> boolean has(Class<T> clazz)
	{
		if (objects == null)
			return false;
		
		for (Object object : objects)
			if (object != null && clazz.isInstance(object))
				return true;
		
		return false;
	}
	
	public <T> T get(Class<T> clazz)
	{
		for (Object object : objects)
			if (object != null && clazz.isInstance(object))
				return clazz.cast(object);
		
		return null;
	}
}
