package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.robbix.mp5.ui.DisplayPanel;

// TODO: API input events (mousePressed, mouseDragged, keyPressed)
//       need to call MovingPictures input handler methods
//       (i.e. leftClick, rectDragged)
public abstract class InputOverlay
implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	private static int DRAG_THRESHOLD = 16;
	
	private DisplayPanel panel;
	private Point pressedPoint = null;
	
	public void paintOverTerrian(Graphics g){}
	public void paintOverUnits(Graphics g){}
	
	public void setDisplay(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplay()
	{
		return panel;
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
	
	public final void mouseClicked(MouseEvent e){}
	
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

	public void mouseDragged(MouseEvent e)
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
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}
	public void mouseWheelMoved(MouseWheelEvent e){}
	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
}
