package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;

public abstract class InputOverlay
implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	public static final Font OVERLAY_FONT = Font.decode("Arial-12");
	public static final Color TRANS_RED = new Color(255, 0, 0, 127);
	
	private static int DRAG_THRESHOLD = 16;
	
	protected DisplayPanel panel;
	private Point currentPoint = null;
	private Point pressedPoint = null;
	
	public void paintOverTerrian(Graphics g, Rectangle rect){}
	public void paintOverUnits(Graphics g, Rectangle rect){}
	
	public void setDisplay(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplay()
	{
		return panel;
	}
	
	public boolean isCursorOnGrid()
	{
		return panelContains(currentPoint);
	}
	
	public Point getCursorPoint()
	{
		return currentPoint;
	}
	
	public Position getCursorPosition()
	{
		return panel.getPosition(currentPoint);
	}
	
	public void init(){}
	public void dispose(){}
	
	public void onLeftClick(int x, int y){}
	public void onRightClick(int x, int y){}
	public void onMiddleClick(int x, int y){}
	public void onAreaDragged(int x, int y, int w, int h){}
	public void onAreaDragging(int x, int y, int w, int h){}
	public void onAreaDragCancelled(){}
	public void onCommand(String command){}
	
	public final void mousePressed(MouseEvent e)
	{
		pressedPoint = e.getPoint();
	}
	
	public final void mouseReleased(MouseEvent e)
	{
		if (pressedPoint != null)
		{
			if (pressedPoint.distanceSq(e.getPoint()) < DRAG_THRESHOLD)
			{				
				switch (e.getButton())
				{
					case MouseEvent.BUTTON1:
						onLeftClick(e.getX(), e.getY());
						break;
					case MouseEvent.BUTTON2:
						onMiddleClick(e.getX(), e.getY());
						break;
					case MouseEvent.BUTTON3:
						onRightClick(e.getX(), e.getY());
						break;
				}
				
				onAreaDragCancelled();
			}
			else
			{
				int x = pressedPoint.x;
				int y = pressedPoint.y;
				int w = e.getX() - x;
				int h = e.getY() - y;
				
				if (w < 0)
				{
					x += w;
					w = -w;
				}
				
				if (h < 0)
				{
					y += h;
					h = -h;
				}
				
				onAreaDragged(x, y, w, h);
			}
			
			pressedPoint = null;
		}
	}

	public final void mouseDragged(MouseEvent e)
	{
		if (pressedPoint != null)
		{
			int x = pressedPoint.x;
			int y = pressedPoint.y;
			int w = e.getX() - x;
			int h = e.getY() - y;
			
			if (w < 0)
			{
				x += w;
				w = -w;
			}
			
			if (h < 0)
			{
				y += h;
				h = -h;
			}
			
			onAreaDragging(x, y, w, h);
		}
	}
	
	public final void mouseEntered(MouseEvent e)
	{
		currentPoint = panelContains(e.getPoint()) ? e.getPoint() : null;
	}
	
	public final void mouseExited(MouseEvent e)
	{
		currentPoint = null;
	}
	
	public final void mouseMoved(MouseEvent e)
	{
		currentPoint = panelContains(e.getPoint()) ? e.getPoint() : null;
	}
	
	public final void mouseClicked(MouseEvent e){}
	public final void mouseWheelMoved(MouseWheelEvent e){}
	public final void keyTyped(KeyEvent e){}
	public final void keyPressed(KeyEvent e){}
	public final void keyReleased(KeyEvent e){}
	
	private boolean panelContains(Point p)
	{
		return p == null
			? false
			:  p.x > 0
			&& p.y > 0
			&& p.x < panel.getWidth()
			&& p.y < panel.getHeight();
	}
}
