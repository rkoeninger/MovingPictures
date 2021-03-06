package com.robbix.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.tree.DefaultMutableTreeNode;

public class RTreeNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = 1L;
	
	private Collection<Object> objects = new ArrayList<Object>();
	
	public RTreeNode(String name, Object... objects)
	{
		super(name);
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
