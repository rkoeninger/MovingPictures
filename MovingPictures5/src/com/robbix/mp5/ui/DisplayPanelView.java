package com.robbix.mp5.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;
import javax.swing.Timer;

import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.unit.Unit;

public class DisplayPanelView extends JScrollPane
{
	private static final long serialVersionUID = 2037569908237169908L;
	
	private DisplayPanel panel;
	private Timer scrollTimer;
	private int dx, dy;
	private int scrollSpeed = 16;
	
	public DisplayPanelView(DisplayPanel panel)
	{
		this.panel = panel;
		setViewportView(panel);
		panel.addKeyListener(new KeyScrollListener());
		AdjustmentListener viewListener = new ViewChangeListener();
		getHorizontalScrollBar().addAdjustmentListener(viewListener);
		getVerticalScrollBar().addAdjustmentListener(viewListener);
		scrollTimer = new Timer(20, new ScrollLoop());
	}
	
	public void setViewPosition(Point p)
	{
		Rectangle rect = panel.getVisibleRect();
		p.x = Math.max(0, Math.min(panel.getWidth()  - rect.width,  p.x));
		p.y = Math.max(0, Math.min(panel.getHeight() - rect.height, p.y));
		getViewport().setViewPosition(p);
	}
	
	public void center(Position pos)
	{
		if (!panel.getMap().getBounds().contains(pos))
			throw new IndexOutOfBoundsException(pos.toString());
		
		final int tileSize = panel.getMap().getTileSize();
		Rectangle rect = panel.getVisibleRect();
		setViewPosition(new Point(
			pos.x * tileSize + tileSize / 2 - rect.width  / 2,
			pos.y * tileSize + tileSize / 2 - rect.height / 2
		));
	}
	
	public void center(Unit unit)
	{
		if (unit.getContainer() != panel.getMap())
			throw new IllegalArgumentException("unit not on map");
		
		final int tileSize = panel.getMap().getTileSize();
		Rectangle rect = panel.getVisibleRect();
		setViewPosition(new Point(
			unit.getAbsX() + unit.getWidth()  * tileSize / 2 - rect.width  / 2,
			unit.getAbsY() + unit.getHeight() * tileSize / 2 - rect.height / 2
		));
	}
	
	public void include(Position pos)
	{
		include(pos, 0);
	}
	
	public void include(Position pos, int padding)
	{
		if (!panel.getMap().getBounds().contains(pos))
			throw new IndexOutOfBoundsException(pos.toString());
		else if (padding < 0)
			throw new IllegalArgumentException("padding can't be negative");
		
		final int tileSize = panel.getMap().getTileSize();
		Rectangle rect = panel.getVisibleRect();
		int x0 = pos.x * tileSize;
		int y0 = pos.y * tileSize;
		int x1 = x0 + tileSize;
		int y1 = y0 + tileSize;
		int x = rect.x;
		int y = rect.y;
		
		padding *= tileSize;
		x0 -= padding;
		y0 -= padding;
		x1 += padding;
		y1 += padding;
		
		if (x0 < rect.x)
		{
			x = x0;
		}
		else if (x1 > rect.x + rect.width)
		{
			x = x1 - rect.width;
		}
		
		if (y0 < rect.y)
		{
			y = y0;
		}
		else if (y1 > rect.y + rect.height)
		{
			y = y1 - rect.height;
		}
		
		setViewPosition(new Point(x, y));
	}
	
	public void include(Unit unit)
	{
		include(unit, 0);
	}
	
	public void include(Unit unit, int padding)
	{
		if (unit.getContainer() != panel.getMap())
			throw new IllegalArgumentException("unit not on map");
		else if (padding < 0)
			throw new IllegalArgumentException("padding can't be negative");
		
		final int tileSize = panel.getMap().getTileSize();
		Rectangle rect = panel.getVisibleRect();
		int x0 = unit.getAbsX();
		int y0 = unit.getAbsY();
		int x1 = x0 + unit.getWidth()  * tileSize;
		int y1 = y0 + unit.getHeight() * tileSize;
		int x = rect.x;
		int y = rect.y;
		
		padding *= tileSize;
		x0 -= padding;
		y0 -= padding;
		x1 += padding;
		y1 += padding;
		
		if (x0 < rect.x)
		{
			x = x0;
		}
		else if (x1 > rect.x + rect.width)
		{
			x = x1 - rect.width;
		}
		
		if (y0 < rect.y)
		{
			y = y0;
		}
		else if (y1 > rect.y + rect.height)
		{
			y = y1 - rect.height;
		}
		
		setViewPosition(new Point(x, y));
	}
	
	private class ScrollLoop implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Point viewPosition = panel.getVisibleRect().getLocation();
			viewPosition.translate(dx, dy);
			setViewPosition(viewPosition);
		}
	}
	
	private class KeyScrollListener extends KeyAdapter
	{
		public void keyPressed(KeyEvent e)
		{
			if (e.getModifiers() == 0)
			{
				if (e.getKeyCode() == KeyEvent.VK_UP)
				{
					dy = -scrollSpeed;
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
				{
					dy = scrollSpeed;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_LEFT)
				{
					dx = -scrollSpeed;
				}
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					dx = scrollSpeed;
				}
			}
			
			scrollTimer.start();
		}
		
		public void keyReleased(KeyEvent e)
		{
			if (e.getModifiers() == 0)
			{
				if (e.getKeyCode() == KeyEvent.VK_UP)
				{
					dy = 0;
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
				{
					dy = 0;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_LEFT)
				{
					dx = 0;
				}
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					dx = 0;
				}
			}
			
			if (dx == 0 && dy == 0)
			{
				scrollTimer.stop();
			}
		}
	}
	
	private class ViewChangeListener implements AdjustmentListener
	{
		Region prevRegion = panel.getVisibleRegion();
		
		public void adjustmentValueChanged(AdjustmentEvent e)
		{
			Region currentRegion = panel.getVisibleRegion();
			
			if (!currentRegion.equals(prevRegion))
			{
				panel.refresh();
				prevRegion = currentRegion;
			}
		}
	}
}
