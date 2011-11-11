package com.robbix.mp5.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.Timer;

public class AnimatedButton extends JButton
{
	private static final long serialVersionUID = 1045366242909393107L;
	
	private List<ImageIcon> icons;
	private ImageIcon disabled;
	private Timer timer;
	private int frame;
	private int frameCount;
	private boolean adopted = false;
	
	public AnimatedButton(String name)
	{
		super(name);
		setActionCommand(name);
	}
	
	public AnimatedButton(String name, List<ImageIcon> icons)
	{
		super(icons.get(0));
		setToolTipText(name);
		
		this.icons = icons;
		this.disabled = new ImageIcon(Utils.getGrayscale(icons.get(0)));
		this.timer = new Timer(60, new RotateIcons());
		this.timer.setCoalesce(true);
		
		setActionCommand(name);
		addMouseListener(new RolloverListener());
		
		frame = 0;
		frameCount = icons.size();
	}
	
	public void removeNotify()
	{
		super.removeNotify();
		adopted = false;
		stopTimer();
	}
	
	public void addNotify()
	{
		super.addNotify();
		adopted = true;
	}
	
	public boolean hasIcons()
	{
		return icons != null && !icons.isEmpty();
	}
	
	public void setDelay(int millis)
	{
		if (timer != null)
			timer.setDelay(millis);
	}
	
	public int getDelay()
	{
		return timer == null ? -1 : timer.getDelay();
	}
	
	public void setVisible(boolean visible)
	{
		if (!visible)
			stopTimer();
		
		super.setVisible(visible);
	}
	
	public void setEnabled(boolean enabled)
	{
		if (!enabled)
			stopTimer();
		
		if (icons != null)
			setIcon(enabled ? icons.get(0) : disabled);
		
		super.setEnabled(enabled);
	}
	
	private class RotateIcons implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (isVisible() && isEnabled())
			{
				frame++;
				setIcon(icons.get(frame % frameCount));
			}
			else
			{
				stopTimer();
			}
		}
	}
	
	private class RolloverListener extends MouseAdapter
	{
		public void mouseEntered(MouseEvent e)
		{
			if (isEnabled())
			{
				startTimer();
			}
		}
		
		public void mouseExited(MouseEvent e)
		{
			if (isEnabled())
			{
				stopTimer();
				frame = 0;
				setIcon(icons.get(0));
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			if (isEnabled())
			{
				stopTimer();
			}
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (isEnabled())
			{
				frame = 0;
				setIcon(icons.get(0));
				startTimer();
			}
		}
	}
	
	private void startTimer()
	{
		if (timer != null && isVisible() && isEnabled() && adopted)
		{
			if (timer.getActionListeners().length == 0)
				timer.addActionListener(new RotateIcons());
			
			timer.start();
		}
	}
	
	private void stopTimer()
	{
		if (timer != null)
		{
			timer.stop();
			
			for (ActionListener listener : timer.getActionListeners())
				timer.removeActionListener(listener);
		}
	}
}
