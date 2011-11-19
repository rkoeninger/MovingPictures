package com.robbix.mp5.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	private Collection<Object> objects = new ArrayList<Object>();
	
	public AnimatedButton(String name, Object... objects)
	{
		super(name);
		add(objects);
	}
	
	public AnimatedButton(String name, List<ImageIcon> icons, Object... objects)
	{
		super(icons.get(0));
		add(objects);
		setToolTipText(name);
		
		this.icons = icons;
		this.disabled = new ImageIcon(Utils.getGrayscale(icons.get(0)));
		this.timer = new Timer(60, new RotateIcons());
		this.timer.setCoalesce(true);
		
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
