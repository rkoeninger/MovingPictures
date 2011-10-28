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
	
	public CommandButton(String name)
	{
		super(name);
		setActionCommand(name);
	}
	
	public CommandButton(String name, List<ImageIcon> icons)
	{
		super(icons.get(0));
		setToolTipText(name);
		
		this.icons = icons;
		this.disabled = new ImageIcon(Utils.getGrayscale(icons.get(0)));
		this.timer = new Timer(60, new RotateIcons());
		
		setActionCommand(name);
		addMouseListener(new RolloverListener());
		
		frame = 0;
		frameCount = icons.size();
	}
	
	public void setDelay(int millis)
	{
		timer.setDelay(millis);
	}
	
	public int getDelay()
	{
		return timer.getDelay();
	}
	
	public void setEnabled(boolean enabled)
	{
		if (icons != null)
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
	
	private class RolloverListener extends MouseAdapter
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
