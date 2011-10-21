package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.KeyEventPostProcessor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.BorderRegion;
import com.robbix.mp5.basics.LShapedRegion;
import com.robbix.mp5.basics.LinearRegion;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;

public abstract class InputOverlay
implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	public static final Font OVERLAY_FONT = Font.decode("Arial-12");
	public static final Font COMMAND_FONT = Font.decode("Arial-bold-20");
	public static final Color TRANS_RED = new Color(255, 0, 0, 127);
	public static final Color TRANS_YELLOW = new Color(255, 255, 0, 127);
	public static final Color TRANS_GREEN = new Color(0, 255, 0, 127);
	public static final Color TRANS_WHITE = new Color(255, 255, 255, 127);
	
	private static int DRAG_THRESHOLD = 16;
	private static final int LEFT   = MouseEvent.BUTTON1;
	private static final int MIDDLE = MouseEvent.BUTTON2;
	private static final int RIGHT  = MouseEvent.BUTTON3;
	
	protected DisplayPanel panel;
	private Point currentPoint = null;
	private Point pressedPoint = null;
	private Rectangle dragArea = null;
	private boolean shiftOption;
	private boolean controlOption;
	private boolean shiftDown;
	private boolean controlDown;
	private String animatedCursor = null;
	
	protected boolean closesOnEscape = true;
	
	protected InputOverlay()
	{
	}
	
	protected InputOverlay(String animatedCursor)
	{
		this.animatedCursor = animatedCursor;
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect){}
	public void paintOverUnits(Graphics g, Rectangle rect){}
	
	public void drawInstructions(Graphics g, Rectangle rect, String... lines)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		int y = 0;
		int i = 0;
		FontMetrics metrics = g.getFontMetrics();
		
		for (String line : lines)
		{
			if (i == 0)
			{
				line = "Left Click to " + line;
			}
			else if (i == 1 && lines.length == 2 || i == 2)
			{
				line = "Right Click to " + line;
			}
			else if (i == 1 && lines.length == 3)
			{
				line = "Middle Click to " + line;
			}
			
			Rectangle2D bounds = metrics.getStringBounds(line, g);
			int x = (int) (rect.width / 2 - bounds.getCenterX());
			y += bounds.getHeight() + 4;
			g.drawString(line, x, y);
			i++;
		}
		
		g.translate(-rect.x, -rect.y);
	}
	
	public void drawCommand(Graphics g, Rectangle rect, Edge edge, String command)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(COMMAND_FONT);
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D bounds = metrics.getStringBounds(command, g);
		int w0 = 4;
		int h0 = (int) (4 + bounds.getHeight());
		int w2 = (int) (rect.width  / 2 - bounds.getCenterX());
		int h2 = (int) (rect.height / 2 - bounds.getCenterY());
		int wN = rect.width  - (int) bounds.getMaxX() - 4;
		int hN = rect.height - (int) bounds.getMaxY() - 4;
		
		switch (edge)
		{
		case N:  g.drawString(command, w2, h0);  break;
		case S:  g.drawString(command, w2, hN); break;
		case E:  g.drawString(command, wN, h2); break;
		case W:  g.drawString(command, w0, h2); break;
		case NE: g.drawString(command, wN, h0);  break;
		case NW: g.drawString(command, w0, h0);  break;
		case SW: g.drawString(command, w0, hN); break;
		case SE: g.drawString(command, wN, hN); break;
		case C:  g.drawString(command, w2, h2); break;
		}
		
		g.translate(-rect.x, -rect.y);
	}
	
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
		return currentPoint == null ? null : panel.getPosition(currentPoint);
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
		return dragArea == null ? null : panel.getRegion(dragArea);
	}
	
	public Region getEnclosedDragRegion()
	{
		return dragArea == null ? null : panel.getEnclosedRegion(dragArea);
	}
	
	public boolean isDragRegionLinear()
	{
		Region fullRegion = panel.getRegion(dragArea);
		return fullRegion.w == 1 || fullRegion.h == 1;
	}
	
	public LinearRegion getLinearDragRegion()
	{
		Position origin = panel.getPosition(pressedPoint);
		Region fullRegion = panel.getRegion(dragArea);
		int endX = origin.x;
		int endY = origin.y;
		
		if (fullRegion.w > fullRegion.h)
		{
			endX = fullRegion.x < origin.x
					? fullRegion.getX()
					: fullRegion.getMaxX() - 1;
			endY = origin.y;
		}
		else
		{
			endX = origin.x;
			endY = fullRegion.y < origin.y
				? fullRegion.getY()
				: fullRegion.getMaxY() - 1;
		}
		
		return new LinearRegion(origin, Mediator.getPosition(endX, endY));
	}
	
	public LShapedRegion getLShapedDragRegion()
	{
		Position origin = panel.getPosition(pressedPoint);
		Region fullRegion = panel.getRegion(dragArea);
		
		int farX = origin.x == fullRegion.x
			? fullRegion.getMaxX() - 1
			: fullRegion.getX();
		int farY = origin.y == fullRegion.y
			? fullRegion.getMaxY() - 1
			: fullRegion.getY();
		
		Position farEnd = Mediator.getPosition(farX, farY);
		Position elbow = isShiftOptionSet()
			? Mediator.getPosition(origin.x, farEnd.y)
			: Mediator.getPosition(farEnd.x, origin.y);
		return new LShapedRegion(origin, elbow, farEnd);
	}
	
	public BorderRegion getBorderDragRegion()
	{
		return new BorderRegion(panel.getRegion(dragArea));
	}
	
	public static enum Edge {NW,N,NE,W,C,E,SW,S,SE;}
	
	public Edge getPointEdge(int x, int y)
	{
		int w  = panel.getWidth();
		int h  = panel.getHeight();
		int x0 = 0;
		int y0 = 0;
		
		return Edge.values()[((x - x0) / (w / 3)) + (((y - y0) / (h / 3)) * 3)];
	}
	
	public boolean isShiftDown()
	{
		return shiftDown;
	}
	
	public boolean isControlDown()
	{
		return controlDown;
	}
	
	public boolean isShiftOptionSet()
	{
		return shiftOption;
	}
	
	public boolean isControlOptionSet()
	{
		return controlOption;
	}
	
	public void push(InputOverlay overlay)
	{
		panel.pushOverlay(overlay);
	}
	
	public void complete()
	{
		panel.completeOverlay(this);
	}
	
	public void init()
	{
		panel.setAnimatedCursor(animatedCursor);
	}
	
	public void dispose()
	{
	}
	
	public boolean postProcessKeyEvent(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.out.println(getClass());
		
		if (!shiftDown && e.isShiftDown())
			shiftOption = !shiftOption;
		
		if (!controlDown && e.isControlDown())
			controlOption = !controlOption;
		
		shiftDown = e.isShiftDown();
		controlDown = e.isControlDown();
		return false;
	}
	
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
		panel.addViewOffset(pressedPoint);
	}
	
	public final void mouseReleased(MouseEvent e)
	{
		Point mousePoint = panel.addViewOffset(e.getPoint());
		
		if (pressedPoint != null)
		{
			if (pressedPoint.distanceSq(mousePoint) < DRAG_THRESHOLD)
			{
				if      (e.getButton() == LEFT)   onLeftClick  (pressedPoint.x, pressedPoint.y);
				else if (e.getButton() == MIDDLE) onMiddleClick(pressedPoint.x, pressedPoint.y);
				else if (e.getButton() == RIGHT)  onRightClick (pressedPoint.x, pressedPoint.y);
				
				if (dragArea != null)
				{
					onAreaDragCancelled();
				}
			}
			else
			{
				prepNormalDragArea(e.getX(), e.getY());
				
				if (dragArea != null)
				{
					onAreaDragged(
						dragArea.x,
						dragArea.y,
						dragArea.width,
						dragArea.height
					);
				}
			}
			
			pressedPoint = null;
			dragArea = null;
		}
	}
	
	public final void mouseDragged(MouseEvent e)
	{
		shiftDown = e.isShiftDown();
		
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
	public final void keyReleased(KeyEvent e){}
	public final void keyPressed(KeyEvent e){}
	public final void keyTyped(KeyEvent e){}
	
	private boolean panelContains(int x, int y) // in terms of relative co-ords
	{
		Point p = new Point(x, y);
		panel.addViewOffset(p);
		
		return panel.getDisplayRect().contains(p);
	}
	
	private void prepCursorPoint(int x, int y) // in terms of relative co-ords
	{
		if (panelContains(x, y))
		{
			if (currentPoint == null)
			{
				currentPoint = new Point();
			}
			
			currentPoint.x = x;
			currentPoint.y = y;
			panel.addViewOffset(currentPoint);
		}
		else
		{
			currentPoint = null;
		}
	}
	
	private void prepNormalDragArea(int x, int y) // in terms of relative co-ords
	{
		prepCursorPoint(x, y);
		
		if (currentPoint == null)
		{
			return;
		}
		
		if (dragArea == null)
		{
			dragArea = new Rectangle();
		}
		
		dragArea.x = pressedPoint.x;
		dragArea.y = pressedPoint.y;
		dragArea.width  = currentPoint.x - dragArea.x;
		dragArea.height = currentPoint.y - dragArea.y;
		
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
	
	public static void paintSelectedUnitBox(Graphics g, Unit unit)
	{
		if (unit.isDead() || unit.isFloating()) return;
		
		int tileSize = unit.getMap().getDisplayPanel().getTileSize();
		int absWidth = unit.getWidth() * tileSize;
		int absHeight = unit.getHeight() * tileSize;
		
		/*
		 * Draw borders
		 */
		int nwCornerX = unit.getAbsX();
		int nwCornerY = unit.getAbsY();
		int neCornerX = nwCornerX + absWidth;
		int neCornerY = nwCornerY;
		int swCornerX = nwCornerX;
		int swCornerY = nwCornerY + absHeight;
		int seCornerX = nwCornerX + absWidth;
		int seCornerY = nwCornerY + absHeight;
		
		g.setColor(Color.WHITE);
		g.drawLine(nwCornerX, nwCornerY, nwCornerX + 4, nwCornerY);
		g.drawLine(nwCornerX, nwCornerY, nwCornerX,     nwCornerY + 4);
		g.drawLine(neCornerX, neCornerY, neCornerX - 4, neCornerY);
		g.drawLine(neCornerX, neCornerY, neCornerX,     neCornerY + 4);
		g.drawLine(swCornerX, swCornerY, swCornerX + 4, swCornerY);
		g.drawLine(swCornerX, swCornerY, swCornerX,     swCornerY - 4);
		g.drawLine(seCornerX, seCornerY, seCornerX - 4, seCornerY);
		g.drawLine(seCornerX, seCornerY, seCornerX,     seCornerY - 4);
		
		/*
		 * Draw health bar
		 */
		double hpFactor = unit.getHP() / (double) unit.getType().getMaxHP();
		hpFactor = Math.min(hpFactor, 1.0f);
		hpFactor = Math.max(hpFactor, 0.0f);
		
		boolean isRed = unit.getHealthBracket() == HealthBracket.RED;
		
		int hpBarLength = absWidth - 14;
		int hpLength = (int) (hpBarLength * hpFactor);
		
		double hpHue = 1.0 - hpFactor;
		hpHue *= 0.333;
		hpHue = 0.333 - hpHue;
		
		double hpAlpha = 2.0 - hpFactor;
		hpAlpha *= 127.0;
		
		Color hpColor = Color.getHSBColor((float) hpHue, 1.0f, 1.0f);
		hpColor = new Color(
			hpColor.getRed(),
			hpColor.getGreen(),
			hpColor.getBlue(),
			(int) hpAlpha
		);
		
		g.setColor(Color.BLACK);
		g.fillRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
		
		if (Utils.getTimeBasedSwitch(300, 2) || !isRed)
		{
			g.setColor(hpColor);
			g.fillRect(nwCornerX + 8, nwCornerY - 1, hpLength - 1, 3);
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
	}
	
	public static class ListenerAdapter
	implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener,
			   KeyEventPostProcessor
	{
		private InputOverlay overlay;
		
		public ListenerAdapter()
		{
			DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager()
									   .addKeyEventPostProcessor(this);
		}
		
		public void setOverlay(InputOverlay overlay)
		{
			this.overlay = overlay;
		}
		
		public boolean hasOverlay()
		{
			return overlay != null;
		}
		
		public InputOverlay getOverlay()
		{
			return overlay;
		}
		
		public boolean postProcessKeyEvent(KeyEvent e)
		{
			if (overlay == null)
				return false;
			
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				if (e.getID() == KeyEvent.KEY_PRESSED)
					if (overlay.closesOnEscape)
						if (overlay.panel.getCurrentOverlay() == overlay)
							overlay.complete();
			
			if (!overlay.shiftDown && e.isShiftDown())
				overlay.shiftOption = !overlay.shiftOption;
			
			if (!overlay.controlDown && e.isControlDown())
				overlay.controlOption = !overlay.controlOption;
			
			overlay.shiftDown = e.isShiftDown();
			overlay.controlDown = e.isControlDown();
			return false;
		}
		
		private boolean ho() { return hasOverlay(); }
		
		public void mouseWheelMoved(MouseWheelEvent e) { if (ho()) overlay.mouseWheelMoved(e); }
		public void mouseDragged   (MouseEvent e)      { if (ho()) overlay.mouseDragged(e);    }
		public void mouseMoved     (MouseEvent e)      { if (ho()) overlay.mouseMoved(e);      }
		public void mouseClicked   (MouseEvent e)      { if (ho()) overlay.mouseClicked(e);    }
		public void mouseEntered   (MouseEvent e)      { if (ho()) overlay.mouseEntered(e);    }
		public void mouseExited    (MouseEvent e)      { if (ho()) overlay.mouseExited(e);     }
		public void mousePressed   (MouseEvent e)      { if (ho()) overlay.mousePressed(e);    }
		public void mouseReleased  (MouseEvent e)      { if (ho()) overlay.mouseReleased(e);   }
		public void keyPressed     (KeyEvent e)        { if (ho()) overlay.keyPressed(e);      }
		public void keyReleased    (KeyEvent e)        { if (ho()) overlay.keyReleased(e);     }
		public void keyTyped       (KeyEvent e)        { if (ho()) overlay.keyTyped(e);        }
	}
}
