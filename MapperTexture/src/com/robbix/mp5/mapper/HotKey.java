package com.robbix.mp5.mapper;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.KeyStroke;

public class HotKey
{
	private Collection<KeyStroke> strokes;
	
	public HotKey(KeyStroke... strokes)
	{
		this.strokes = Arrays.asList(strokes);
	}
	
	public Collection<KeyStroke> getKeyStrokes()
	{
		return strokes;
	}
	
	public boolean respondsTo(KeyStroke stroke)
	{
		return strokes.contains(stroke);
	}
	
	public void hotkeyTyped()
	{
		
	}
}
