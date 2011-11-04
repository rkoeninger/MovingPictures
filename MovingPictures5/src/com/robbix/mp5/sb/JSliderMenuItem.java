package com.robbix.mp5.sb;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class JSliderMenuItem extends JSlider implements MenuElement
{
	private static final long serialVersionUID = 1L;
	
	public JSliderMenuItem(int minValue, int maxValue, int value)
	{
		super(JSlider.VERTICAL, minValue, maxValue, value);
		setAutoscrolls(false);
		setSnapToTicks(false);
	}
	
	public MenuElement[] getSubElements()
	{
		return new MenuElement[0];
	}
	
	public Component getComponent()
	{
		return this;
	}
	
	public void processKeyEvent(KeyEvent e, MenuElement path[], MenuSelectionManager manager)
	{
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_1: setValue(0.1); break;
		case KeyEvent.VK_2: setValue(0.2); break;
		case KeyEvent.VK_3: setValue(0.3); break;
		case KeyEvent.VK_4: setValue(0.4); break;
		case KeyEvent.VK_5: setValue(0.5); break;
		case KeyEvent.VK_6: setValue(0.6); break;
		case KeyEvent.VK_7: setValue(0.7); break;
		case KeyEvent.VK_8: setValue(0.8); break;
		case KeyEvent.VK_9: setValue(0.9); break;
		case KeyEvent.VK_0: setValue(1.0); break;
		}
	}
	
	private void setValue(double percent)
	{
		int min = getMinimum();
		int max = getMaximum();
		setValue(min + (int) ((max - min) * percent));
	}
	
	public void processMouseEvent(MouseEvent e, MenuElement path[], MenuSelectionManager manager)
	{
	}
	
	public void menuSelectionChanged(boolean isIncluded)
	{
	}
}