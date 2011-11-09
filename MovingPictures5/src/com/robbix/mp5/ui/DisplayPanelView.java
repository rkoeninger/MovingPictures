package com.robbix.mp5.ui;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class DisplayPanelView extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	private DisplayPanel panel;
	private Timer scrollTimer;
	private int sx;
	private int sy;
	private int scrollSpeed = 16;
	private JScrollBar vScrollBar;
	private JScrollBar hScrollBar;
	private boolean scrollBarsVisible = false;
	
	public DisplayPanelView(DisplayPanel panel)
	{
		this.panel = panel;
		setLayout(new BorderLayout());
		add(panel, SwingConstants.CENTER);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new KeyEvents());
		panel.addMouseListener(new MouseEvents());
		panel.addMouseMotionListener(new MouseEvents());
		panel.addMouseWheelListener(new MouseEvents());
		scrollTimer = new Timer(20, new DoScroll());
		vScrollBar = new JScrollBar(SwingConstants.VERTICAL);
		hScrollBar = new JScrollBar(SwingConstants.HORIZONTAL);
		AdjustmentListener scrollBarMoved = new ScrollBarMoved();
		vScrollBar.addAdjustmentListener(scrollBarMoved);
		hScrollBar.addAdjustmentListener(scrollBarMoved);
	}
	
	public DisplayPanel getDisplayPanel()
	{
		return panel;
	}
	
	public void showScrollBars(boolean show)
	{
		if (show == scrollBarsVisible)
			return;
		
		if (show)
		{
			add(vScrollBar, BorderLayout.EAST);
			add(hScrollBar, BorderLayout.SOUTH);
		}
		else
		{
			remove(vScrollBar);
			remove(hScrollBar);
		}
		
		scrollBarsVisible = show;
	}
	
	public boolean areScrollBarsVisible()
	{
		return scrollBarsVisible;
	}
	
	public void setScrollSpeed(int speed)
	{
		if (speed <= 0)
			throw new IllegalArgumentException("scroll speed must be > 0");
		
		this.scrollSpeed = speed;
	}
	
	public int getScrollSpeed()
	{
		return scrollSpeed;
	}
	
	private class ScrollBarMoved implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent arg0)
		{
			
		}
	}
	
	private class KeyEvents extends KeyAdapter
	{
		public void keyPressed(KeyEvent e)
		{
			if (e.getModifiers() == 0)
			{
				switch (e.getKeyCode())
				{
				case VK_UP:     sy = +scrollSpeed; break;
				case VK_DOWN:   sy = -scrollSpeed; break;
				case VK_LEFT:   sx = +scrollSpeed; break;
				case VK_RIGHT:  sx = -scrollSpeed; break;
				case VK_MINUS:  panel.zoomOut();   break;
				case VK_EQUALS: panel.zoomIn();    break;
				case VK_0:      panel.zoomNormal();break;
				}
				
				if (sx != 0 || sy != 0)
					scrollTimer.start();
			}
		}
		
		public void keyReleased(KeyEvent e)
		{
			if (e.getModifiers() == 0)
			{
				switch (e.getKeyCode())
				{
				case VK_UP:   case VK_DOWN:  sy = 0; break;
				case VK_LEFT: case VK_RIGHT: sx = 0; break;
				}
				
				if (sx == 0 && sy == 0)
					scrollTimer.stop();
			}
		}
	}
	
	public class MouseEvents extends MouseAdapter
	{
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			if (e.isControlDown())
			{
				Point zoomPoint = e.getPoint();
				zoomPoint.x -= panel.getViewX();
				zoomPoint.y -= panel.getViewY();
				
				if (e.getWheelRotation() > 0) panel.zoomIn(zoomPoint);
				else                          panel.zoomOut(zoomPoint);
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			panel.requestFocus();
		}
		
		public void mouseClicked(MouseEvent e)
		{
			if (e.isControlDown() && e.getButton() == MouseEvent.BUTTON2)
			{
				if (panel.getScale() == 0)
				{
					panel.zoomGlobal();
				}
				else
				{
					Point zoomPoint = e.getPoint();
					zoomPoint.x -= panel.getViewX();
					zoomPoint.y -= panel.getViewY();
					panel.zoomNormal(zoomPoint);
				}
			}
		}
	}
	
	private class DoScroll implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			panel.shiftViewPoint(sx, sy);
			panel.repaint();
		}
	}
	
}
