package com.robbix.mp5.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.Timer;

import com.robbix.mp5.Utils;

public class CommandButton extends JButton
{
	private static final long serialVersionUID = 1045366242909393107L;
	
	private List<ImageIcon> icons;
	private ImageIcon disabled;
	private Timer timer;
	private int frame;
	private int frameCount;
	
	public CommandButton(List<ImageIcon> icons)
	{
		super(icons.get(0));
		
		this.icons = icons;
		this.disabled = new ImageIcon(Utils.getGrayscale(icons.get(0)));
		this.timer = new Timer(60, new RotateIcons());
		
		addMouseListener(new Listener());
		
		frame = 0;
		frameCount = icons.size();
	}
	
	public void setEnabled(boolean enabled)
	{
		setIcon(enabled ? icons.get(0) : disabled);
		
		super.setEnabled(enabled);
	}
	
	private class RotateIcons implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			frame++;
			setIcon(icons.get(frame % frameCount));
		}
	}
	
	private class Listener extends MouseAdapter
	{
		public void mouseEntered(MouseEvent e)
		{
			if (isEnabled())
			{
				timer.start();
			}
		}
		
		public void mouseExited(MouseEvent e)
		{
			if (isEnabled())
			{
				timer.stop();
				frame = 0;
				setIcon(icons.get(0));
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			if (isEnabled())
			{
				timer.stop();
			}
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (isEnabled())
			{
				frame = 0;
				setIcon(icons.get(0));
				timer.start();
			}
		}
	}
}
