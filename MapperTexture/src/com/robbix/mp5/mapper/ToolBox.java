package com.robbix.mp5.mapper;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.HashMap;

import javax.swing.event.MouseInputListener;

public class ToolBox implements MouseInputListener, KeyListener, EventListener
{
	private java.util.Map<String, Tool> tools;
	private Tool current = null;
	
	public ToolBox()
	{
		tools = new HashMap<String, Tool>();
	}
	
	public void add(String name, Tool tool)
	{
		tools.put(name, tool);
	}
	
	public void select(String name)
	{
		current = tools.get(name);
	}
	
	public void mouseClicked(MouseEvent arg0)
	{
		if (current != null) current.mouseClicked(arg0);
	}
	
	public void mouseEntered(MouseEvent arg0)
	{
		if (current != null) current.mouseEntered(arg0);
	}
	
	public void mouseExited(MouseEvent arg0)
	{
		if (current != null) current.mouseExited(arg0);
	}
	
	public void mousePressed(MouseEvent arg0)
	{
		if (current != null) current.mousePressed(arg0);
	}
	
	public void mouseReleased(MouseEvent arg0)
	{
		if (current != null) current.mouseReleased(arg0);
	}
	
	public void mouseDragged(MouseEvent arg0)
	{
		if (current != null) current.mouseDragged(arg0);
	}
	
	public void mouseMoved(MouseEvent arg0)
	{
		if (current != null) current.mouseMoved(arg0);
	}
	
	public void keyPressed(KeyEvent arg0)
	{
		if (current != null) current.keyPressed(arg0);
	}
	
	public void keyReleased(KeyEvent arg0)
	{
		if (current != null) current.keyReleased(arg0);
	}
	
	public void keyTyped(KeyEvent arg0)
	{
		if (current != null) current.keyTyped(arg0);
	}
}
