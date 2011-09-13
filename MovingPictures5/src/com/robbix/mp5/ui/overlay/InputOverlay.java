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
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.ui.DisplayPanel;

public abstract class InputOverlay
implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	public static final Font OVERLAY_FONT = Font.decode("Arial-12");
	public static final Color TRANS_RED = new Color(255, 0, 0, 127);
	
	private static int DRAG_THRESHOLD = 16;
	private static final int LEFT   = MouseEvent.BUTTON1;
	private static final int MIDDLE = MouseEvent.BUTTON2;
	private static final int RIGHT  = MouseEvent.BUTTON3;
	
	protected DisplayPanel panel;
	private Point currentPoint = null;
	private Point pressedPoint = null;
	private Rectangle dragArea = null;
	
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
		return currentPoint != null
			&& panelContains(currentPoint.x, currentPoint.y);
	}
	
	public Point getCursorPoint()
	{
		return currentPoint;
	}
	
	public Position getCursorPosition()
	{
		return panel.getPosition(currentPoint);
	}
	
	public boolean isDragging()
	{
		return dragArea != null;
	}
	
	public Rectangle getDragArea()
	{
		return dragArea;
	}
	
	public Region getDragRegion()
	{
		return panel.getRegion(dragArea);
	}
	
	public Region getEnclosedDragRegion()
	{
		return panel.getEnclosedRegion(dragArea);
	}
	
	// FIXME: any good?
	public Region getLinearDragRegion()
	{
		Position origin = panel.getPosition(pressedPoint);
		Region fullRegion = panel.getRegion(dragArea);
		
		if (fullRegion.w > fullRegion.h)
		{
			if (fullRegion.x < origin.x)
			{
				return new Region(origin, new Position(fullRegion.x, origin.y));
			}
			else
			{
				return new Region(origin, new Position(fullRegion.x + fullRegion.w, origin.y));
			}
		}
		else
		{
			if (fullRegion.y < origin.y)
			{
				return new Region(origin, new Position(origin.x, fullRegion.y));
			}
			else
			{
				return new Region(origin, new Position(origin.x, fullRegion.y + fullRegion.h));
			}
		}
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
				if      (e.getButton() == LEFT)   onLeftClick  (e.getX(), e.getY());
				else if (e.getButton() == MIDDLE) onMiddleClick(e.getX(), e.getY());
				else if (e.getButton() == RIGHT)  onRightClick (e.getX(), e.getY());
				
				if (dragArea != null)
				{
					onAreaDragCancelled();
					dragArea = null;
				}
			}
			else
			{
				prepNormalDragArea(e.getX(), e.getY());
				onAreaDragged(
					dragArea.x,
					dragArea.y,
					dragArea.width,
					dragArea.height
				);
				dragArea = null;
			}
			
			pressedPoint = null;
		}
	}
	
	public final void mouseDragged(MouseEvent e)
	{
		if (pressedPoint != null)
		{
			prepNormalDragArea(e.getX(), e.getY());
			onAreaDragging(
				dragArea.x,
				dragArea.y,
				dragArea.width,
				dragArea.height
			);
		}
	}
	
	public final void mouseEntered(MouseEvent e)
	{
		prepCursorPoint(e.getX(), e.getY());
	}
	
	public final void mouseExited(MouseEvent e)
	{
		currentPoint = null;
	}
	
	public final void mouseMoved(MouseEvent e)
	{
		prepCursorPoint(e.getX(), e.getY());
	}
	
	public final void mouseClicked(MouseEvent e){}
	public final void mouseWheelMoved(MouseWheelEvent e){}
	public final void keyTyped(KeyEvent e){}
	public final void keyPressed(KeyEvent e){}
	public final void keyReleased(KeyEvent e){}
	
	private boolean panelContains(int x, int y)
	{
		return x > 0
			&& y > 0
			&& x < panel.getWidth()
			&& y < panel.getHeight();
	}
	
	private void prepCursorPoint(int x, int y)
	{
		if (panelContains(x, y))
		{
			if (currentPoint == null)
			{
				currentPoint = new Point();
			}
			
			currentPoint.x = x;
			currentPoint.y = y;
		}
		else
		{
			currentPoint = null;
		}
	}
	
	private void prepNormalDragArea(int x, int y)
	{
		if (dragArea == null)
		{
			dragArea = new Rectangle();
		}
		
		dragArea.x = pressedPoint.x;
		dragArea.y = pressedPoint.y;
		dragArea.width  = x - dragArea.x;
		dragArea.height = y - dragArea.y;
		
		if (dragArea.width < 0)
		{
			dragArea.x += dragArea.width;
			dragArea.width = -dragArea.width;
		}
		
		if (dragArea.height < 0)
		{
			dragArea.y += dragArea.height;
			dragArea.height = -dragArea.height;
		}
		
	}
}
