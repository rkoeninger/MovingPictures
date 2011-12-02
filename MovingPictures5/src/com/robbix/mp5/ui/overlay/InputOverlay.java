package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Font;
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
import java.awt.geom.Point2D;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;
import com.robbix.utils.BorderRegion;
import com.robbix.utils.ColorScheme;
import com.robbix.utils.LShapedRegion;
import com.robbix.utils.LinearRegion;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.Region;
import com.robbix.utils.Utils;

public abstract class InputOverlay
implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	public static final Font OVERLAY_FONT = Font.decode("SansSerif-12");
	public static final Color TRANS_RED = new Color(255, 0, 0, 127);
	public static final Color TRANS_YELLOW = new Color(255, 255, 0, 127);
	public static final Color TRANS_GREEN = new Color(0, 255, 0, 127);
	public static final Color TRANS_WHITE = new Color(255, 255, 255, 127);
	
	public static final ColorScheme RED    = ColorScheme.withTranslucentBody(Color.RED);
	public static final ColorScheme BLUE   = ColorScheme.withTranslucentBody(Color.BLUE);
	public static final ColorScheme YELLOW = ColorScheme.withTranslucentBody(Color.YELLOW);
	public static final ColorScheme GREEN  = ColorScheme.withTranslucentBody(Color.GREEN);
	public static final ColorScheme WHITE  = ColorScheme.withTranslucentBody(Color.WHITE);
	
	private static int DRAG_THRESHOLD = 8*8;
	private static final int LEFT   = MouseEvent.BUTTON1;
	private static final int MIDDLE = MouseEvent.BUTTON2;
	private static final int RIGHT  = MouseEvent.BUTTON3;
	
	protected DisplayPanel panel;
	private Point currentPoint = null;
	private Point pressedPoint = null;
	private Rectangle dragArea = null;
	private int shiftOption;
	private boolean shiftDown;
	private String animatedCursor = null;
	
	protected int shiftOptionCount = 2;
	
	protected boolean showTubeConnectivity = false;
	
	protected boolean closesOnEscape            = true;
	protected boolean closesOnRightClick        = true;
	protected boolean requiresLeftClickOnGrid   = true;
	protected boolean requiresRightClickOnGrid  = false;
	protected boolean requiresMiddleClickOnGrid = true;
	protected boolean requiresPaintOnGrid       = true;
	
	protected InputOverlay()
	{
	}
	
	protected InputOverlay(String animatedCursor)
	{
		this.animatedCursor = animatedCursor;
	}
	
	public void setDisplay(DisplayPanel panel)
	{
		this.panel = panel;
	}
	
	public DisplayPanel getDisplay()
	{
		return panel;
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
		panel.setShowTubeConnectivity(showTubeConnectivity);
	}
	
	public void dispose()
	{
		panel.setShowTubeConnectivity(false);
	}
	
	public void paint(DisplayGraphics g)
	{
		if (!requiresPaintOnGrid || isCursorOnGrid())
			paintImpl(g);
	}
	
	public void paintImpl(DisplayGraphics g){}
	
	public void drawSelectedUnitBox(DisplayGraphics g, Unit unit)
	{
		if (unit.isDead() || unit.isFloating()) return;
		
		if (panel.getScale() < 0)
		{
			g.setColor(Color.WHITE);
			g.draw(unit.getPosition());
			return;
		}
		
		g.translate(panel.getViewX(), panel.getViewY());
		
		int tileSize = panel.getTileSize();
		int absWidth = unit.getWidth() * tileSize;
		int absHeight = unit.getHeight() * tileSize;
		
		/*
		 * Draw borders
		 */
		Point2D absPoint = unit.getAbsPoint();
		int nwCornerX = (int) (absPoint.getX() * tileSize);
		int nwCornerY = (int) (absPoint.getY() * tileSize);
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
		double hpHue = 0.333 - (1.0 - hpFactor) * 0.333;
		int hpAlpha = (int) ((2.0 - hpFactor) * 127);
		
		Color hpColor = RColor.getHue(hpHue).toTranslucent(hpAlpha);
		
		g.setColor(Color.BLACK);
		g.fillRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
		
		/*
		 * Flash health bar when red 
		 */
		if (Utils.getTimeBasedSwitch(300, 2) || !isRed)
		{
			g.setColor(hpColor);
			g.fillRect(nwCornerX + 8, nwCornerY - 1, hpLength - 1, 3);
		}
		
		g.setColor(Color.WHITE);
		g.drawRect(nwCornerX + 7, nwCornerY - 2, hpBarLength, 4);
		
		g.translate(-panel.getViewX(), -panel.getViewY());
	}
	
	/**
	 * Like drawUnitFootprint(), but for a mine of undetermined type (common/rare).
	 */
	public String drawMineFootprint(DisplayGraphics g, Position pos)
	{
		Footprint fp = Footprint.STRUCT_2_BY_1_NO_W_SIDE;
		Region inner = fp.getInnerRegion().move(pos);
		Region outer = inner.stretch(1);
		LayeredMap map = panel.getMap();
		Color color;
		String toolTip = null;
		
		boolean onMap = map.getBounds().contains(inner);
		boolean available = map.canPlaceUnit(pos, fp);
		boolean hasDeposit = map.hasResourceDeposit(pos.shift(1, 0));
		
		color = onMap && available && hasDeposit ? GREEN.getFill() : RED.getFill();
		
		if      (!onMap)      toolTip = "Out of bounds";
		else if (!available)  toolTip = "Occupied";
		else if (!hasDeposit) toolTip = "No resource deposit";
		
		g.setColor(color);
		g.fill(inner);
		g.setColor(WHITE.getEdge());
		g.draw(outer);
		
		return toolTip;
	}
	
	/**
	 * Returns tooltip text so it can be drawn on top of unit placement sprite.
	 */
	public String drawUnitFootprint(DisplayGraphics g, UnitType type, Position pos)
	{
		Footprint fp = type.getFootprint();
		Region inner = fp.getInnerRegion().move(pos);
		Region outer = inner.stretch(1);
		LayeredMap map = panel.getMap();
		Color color;
		String toolTip = null;
		boolean rare = type.is("Rare");
		
		if (type.getName().endsWith("Mine"))
		{
			boolean onMap = map.getBounds().contains(inner);
			boolean available = map.canPlaceUnit(pos, fp);
			boolean hasDeposit = map.hasResourceDeposit(pos.shift(1, 0));
			boolean correctDeposit = hasDeposit && map.canPlaceMine(pos, rare);
			
			color = onMap && available && correctDeposit ? GREEN.getFill() : RED.getFill();
			
			if      (!onMap)          toolTip = "Out of bounds";
			else if (!available)      toolTip = "Occupied";
			else if (!hasDeposit)     toolTip = "No resource deposit";
			else if (!correctDeposit) toolTip = "Wrong deposit type";
		}
		else if (type.isStructureType() || type.isGuardPostType())
		{
			boolean onMap = map.getBounds().contains(inner);
			boolean available = map.canPlaceUnit(pos, fp);
			boolean connected = !type.needsConnection() || map.willConnect(pos, fp);
			boolean noDeposit = !containsDeposit(outer);
			
			color = onMap && available ? (connected && noDeposit ? GREEN.getFill() : YELLOW.getFill()) : RED.getFill();
			
			if      (!onMap)     toolTip = "Out of bounds";
			else if (!available) toolTip = "Occupied";
			else if (!noDeposit) toolTip = "Covers resource deposit";
			else if (!connected) toolTip = "No tube connection";
		}
		else
		{
			boolean onMap = map.getBounds().contains(inner);
			boolean available = map.canPlaceUnit(pos, fp);
			
			color = onMap && available ? GREEN.getFill() : RED.getFill();
			
			if      (!onMap)     toolTip = "Out of bounds";
			else if (!available) toolTip = "Occupied";
		}
		
		g.setColor(color);
		g.fill(inner);
		
		g.setColor(WHITE.getFill());
		
		for (Position tubePos : fp.getTubePositions())
			g.fill(tubePos.shift(inner.x, inner.y));
		
		g.setColor(WHITE.getEdge());
		
		if (type.isStructureType() || type.isGuardPostType())
			g.draw(outer);
		
		return toolTip;
	}
	
	private boolean containsDeposit(Region region)
	{
		LayeredMap map = panel.getMap();
		
		for (Position pos : region)
			if (map.getBounds().contains(pos) && map.hasResourceDeposit(pos))
				return true;
		
		return false;
	}
	
	public void onLeftClick(){}
	public void onRightClick(){}
	public void onMiddleClick(){}
	public void onAreaDragged(){}
	public void onLeftClickDrag(){}
	public void onCommand(Command command){}
	
	public boolean isCursorOnGrid()
	{
		return currentPoint != null && panel.getGridMetrics().getPosition(currentPoint) != null;
	}
	
	public Point getCursorPoint()
	{
		return currentPoint;
	}
	
	public Position getCursorPosition()
	{
		if (currentPoint == null)
			return null;
		
		return panel.getGridMetrics().getPosition(currentPoint);
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
		return dragArea == null ? null : panel.getGridMetrics().getRegion(dragArea);
	}
	
	public Region getEnclosedDragRegion()
	{
		return dragArea == null ? null : panel.getGridMetrics().getEnclosedRegion(dragArea);
	}
	
	public boolean isDragRegionLinear()
	{
		Region fullRegion = panel.getGridMetrics().getRegion(dragArea);
		return fullRegion.w == 1 || fullRegion.h == 1;
	}
	
	public LinearRegion getLinearDragRegion()
	{
		Position origin = panel.getGridMetrics().getPosition(pressedPoint);
		Region fullRegion = panel.getGridMetrics().getRegion(dragArea);
		int endX = origin.x;
		int endY = origin.y;
		
		if (fullRegion.w > fullRegion.h)
		{
			endX = fullRegion.x < origin.x ? fullRegion.getX() : fullRegion.getMaxX() - 1;
		}
		else
		{
			endY = fullRegion.y < origin.y ? fullRegion.getY() : fullRegion.getMaxY() - 1;
		}
		
		return new LinearRegion(origin, new Position(endX, endY));
	}
	
	public LShapedRegion getLShapedDragRegion()
	{
		return getLShapedDragRegion(true);
	}
	
	public LShapedRegion getLShapedDragRegion(boolean verticalFirst)
	{
		Position origin = panel.getGridMetrics().getPosition(pressedPoint);
		Region fullRegion = panel.getGridMetrics().getRegion(dragArea);
		
		int farX = origin.x == fullRegion.x ? fullRegion.getMaxX() - 1 : fullRegion.getX();
		int farY = origin.y == fullRegion.y ? fullRegion.getMaxY() - 1 : fullRegion.getY();
		
		return new LShapedRegion(origin, new Position(farX, farY), verticalFirst);
	}
	
	public BorderRegion getBorderDragRegion()
	{
		return new BorderRegion(panel.getGridMetrics().getRegion(dragArea));
	}
	
	public boolean isShiftDown()
	{
		return shiftDown;
	}
	
	public boolean isShiftOptionSet()
	{
		return shiftOption % 2 == 0;
	}
	
	public int getShiftOption()
	{
		return shiftOption % shiftOptionCount;
	}
	
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
				if (e.getButton() == LEFT)
				{
					if (!requiresLeftClickOnGrid || isCursorOnGrid())
						onLeftClick();
				}
				else if (e.getButton() == MIDDLE)
				{
					if (!requiresMiddleClickOnGrid || isCursorOnGrid())
						onMiddleClick();
				}
				else if (e.getButton() == RIGHT)
				{
					if (!requiresRightClickOnGrid || isCursorOnGrid())
						onRightClick();
					
					if (closesOnRightClick)
						complete();
				}
			}
			else
			{
				prepNormalDragArea(e.getX(), e.getY());
				
				if (dragArea != null)
					onAreaDragged();
			}
			
			pressedPoint = null;
			dragArea = null;
		}
	}
	
	public final void mouseDragged(MouseEvent e)
	{
		if (pressedPoint == null)
			mousePressed(e);
		
		if (pressedPoint.distanceSq(e.getPoint()) < DRAG_THRESHOLD)
			return;
		
		if (isCursorOnGrid() && pressedPoint != null)
		{
			prepNormalDragArea(e.getX(), e.getY());
			onLeftClickDrag();
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
		return panel.getGridMetrics().getPosition(p) != null;
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
	
	public static class ListenerAdapter
	implements KeyListener,
			   MouseListener,
			   MouseMotionListener,
			   MouseWheelListener,
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
				overlay.shiftOption++;
			
			overlay.shiftDown = e.isShiftDown();
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
