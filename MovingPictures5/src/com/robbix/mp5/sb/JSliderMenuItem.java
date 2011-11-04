package com.robbix.mp5.sb;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JSliderMenuItem extends JFrame
{
	public static void main(String args[])
	{
		new JSliderMenuItem().setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	public JSliderMenuItem()
	{
		super("slider menu item");
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Engine");
		JMenu throttleMenu = new JMenu("Throttle");
		menu.add(throttleMenu);
		throttleMenu.add(new SliderMenuItem());
		menuBar.add(menu);
		setJMenuBar(menuBar);
		setSize(300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// Inner class that defines our special slider menu item
	private class SliderMenuItem extends JSlider implements MenuElement
	{
		private static final long serialVersionUID = 1L;
		
		public SliderMenuItem()
		{
			super(0, 100, 50);
			addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					System.out.println(getValue());
				}
			});
			setOrientation(SwingConstants.VERTICAL);
		}
		
		public void processMouseEvent(MouseEvent e, MenuElement path[],
				MenuSelectionManager manager)
		{
		}
		
		public void processKeyEvent(KeyEvent e, MenuElement path[],
				MenuSelectionManager manager)
		{
		}
		
		public void menuSelectionChanged(boolean isIncluded)
		{
		}
		
		public MenuElement[] getSubElements()
		{
			return new MenuElement[0];
		}
		
		public Component getComponent()
		{
			return this;
		}
	}
}