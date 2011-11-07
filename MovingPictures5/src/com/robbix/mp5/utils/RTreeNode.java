package com.robbix.mp5.utils;

import javax.swing.tree.DefaultMutableTreeNode;

public class RTreeNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = 1L;
	
	private Object[] objects;
	
	public RTreeNode(String name)
	{
		super(name);
	}
	
	public void set(Object... objects)
	{
		this.objects = objects;
	}
	
	public boolean has()
	{
		return objects != null && objects.length > 0;
	}
	
	public Object[] get()
	{
		return objects;
	}
	
	public <T> boolean has(Class<T> clazz)
	{
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
