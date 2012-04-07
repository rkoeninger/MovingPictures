package com.robbix.mp5.mapper;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventListener;

import javax.swing.event.MouseInputListener;

public class Tool implements MouseInputListener, KeyListener, EventListener
{
	private String name;
	
	public Tool(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void mouseClicked(MouseEvent arg0)
	{
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}

	public void mousePressed(MouseEvent arg0)
	{
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

	public void mouseDragged(MouseEvent arg0)
	{
	}

	public void mouseMoved(MouseEvent arg0)
	{
	}

	public void keyPressed(KeyEvent arg0)
	{
	}

	public void keyReleased(KeyEvent arg0)
	{
	}

	public void keyTyped(KeyEvent arg0)
	{
	}
}
