package com.robbix.mp5.ui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.Tile;
import com.robbix.mp5.map.TileSet;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.ani.AmbientAnimation;
import com.robbix.mp5.ui.obj.DisplayLayer;
import com.robbix.mp5.ui.obj.DisplayObject;
import com.robbix.mp5.ui.overlay.InputOverlay;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.AnimatedCursor;
import com.robbix.utils.CostMap;
import com.robbix.utils.GridMetrics;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.RImage;
import com.robbix.utils.Region;

@SuppressWarnings("serial")
public class DisplayPanel extends JComponent
{
	private LayeredMap map;
	private SpriteLibrary sprites;
	private TileSet tiles;
	private CursorSet cursors;
	
	private GridMetrics gm = new GridMetrics(0, 0, 32, 0);
	private int minScale, maxScale, normalScale;
	private int minShowUnitScale = -2;
	
	private double shadowXOffset = -0.125;
	private double shadowYOffset = -0.125;
	
	private BufferedImage cachedBackground = null;
	private Object cacheLock = new Object();
	private long lastRefreshTime = 0;
	
	private static Font costMapFont = Font.decode("SansSerif-9");
	private static Color BACKGROUND_BLUE = new Color(127, 127, 255);
	private Color letterBoxColor = Color.BLACK;
	
	private DisplayWindow window;
	
	private Player currentPlayer;
	
	private AnimatedCursor cursor;
	
	private LinkedList<InputOverlay> overlays;
	private InputOverlay.ListenerAdapter adapter;
	
	private List<AmbientAnimation> animations = new LinkedList<AmbientAnimation>();
	
	private EnumMap<DisplayLayer, List<DisplayObject>> displayLayers;
	
	private DisplayPanelView view;
	
	private boolean showGrid = false;
	private boolean showTubeConnectivity = false;
	private boolean showTerrainCostMap = false;
	private boolean showBackground = true;
	private boolean showShadows = false;
	
	public DisplayPanel(Game game)
	{
		this(game.getMap(), game.getSpriteLibrary(), game.getTileSet(), game.getCursorSet());
		currentPlayer = game.getDefaultPlayer();
	}
	
	public DisplayPanel(
		LayeredMap map,
		SpriteLibrary sprites,
		TileSet tiles,
		CursorSet cursors)
	{
		if (map == null || sprites == null || tiles == null || cursors == null)
			throw new NullPointerException();
		
		this.view = new DisplayPanelView(this);
		this.map = map;
		map.addDisplayPanel(this);
		this.sprites = sprites;
		this.tiles = tiles;
		this.gm.tileSize = tiles.getTileSize();
		this.normalScale = 0;
		this.maxScale = 3;
		this.minScale = getMinScale(gm.tileSize);
		this.cursors = cursors;
		this.overlays = new LinkedList<InputOverlay>();
		this.displayLayers = new EnumMap<DisplayLayer, List<DisplayObject>>(DisplayLayer.class);
		
		for (DisplayLayer layer : DisplayLayer.values())
			displayLayers.put(layer, new LinkedList<DisplayObject>());
		
		setBackground(BACKGROUND_BLUE);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		Dimension size = new Dimension(
			map.getWidth()  * gm.tileSize,
			map.getHeight() * gm.tileSize
		);
		setPreferredSize(size);
		this.adapter = new InputOverlay.ListenerAdapter();
		addKeyListener(adapter);
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
	}
	
	public DisplayPanel(DisplayPanel that)
	{
		this(that.getMap(), that.getSpriteLibrary(), that.getTileSet(), that.getCursorSet());
		this.setScale(that.getScale());
		this.setViewPoint(that.getViewX(), that.getViewY());
		this.currentPlayer = that.currentPlayer;
	}
	
	private static int getMinScale(long tileSize)
	{
		for (int b = 0; b < 64; ++b)
			if (((tileSize >> b) & 1) != 0)
				return -b;
		
		throw new ArithmeticException();
	}
	
	public DisplayPanelView getView()
	{
		return view;
	}
	
	public void setMinimumShowUnitScale(int minShowUnitScale)
	{
		this.minShowUnitScale = minShowUnitScale;
	}
	
	public int getMinimumShowUnitScale()
	{
		return minShowUnitScale;
	}
	
	public void setLetterBoxColor(Color color)
	{
		if (color == null || color.getAlpha() != 255)
			throw new IllegalArgumentException();
		
		this.letterBoxColor = color;
	}
	
	public Color getLetterBoxColor()
	{
		return letterBoxColor;
	}
	
	public SpriteLibrary getSpriteLibrary()
	{
		return sprites;
	}
	
	public TileSet getTileSet()
	{
		return tiles;
	}
	
	public LayeredMap getMap()
	{
		return map;
	}
	
	public CursorSet getCursorSet()
	{
		return cursors;
	}
	
	public void setAnimatedCursor(String name)
	{
		if (cursor != null)
			cursor.hide(this);
		
		if (name != null)
		{
			cursor = cursors.getCursor(name);
			
			if (cursor == null)
				throw new IllegalArgumentException("Cursor " + name + " does not exist");
			
			cursor.show(this);
		}
	}
	
	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	public Point2D getShadowOffset()
	{
		return new Point2D.Double(shadowXOffset, shadowYOffset);
	}
	
	public void setShowGrid(boolean showGrid)
	{
		this.showGrid = showGrid;
		repaint();
	}
	
	public boolean isShowingGrid()
	{
		return showGrid;
	}
	
	public void setShowShadows(boolean showShadows)
	{
		this.showShadows = showShadows;
		repaint();
	}
	
	public boolean isShowingShadows()
	{
		return showShadows;
	}
	
	public void setShowTubeConnectivity(boolean showTubeConnectivity)
	{
		this.showTubeConnectivity = showTubeConnectivity;
		repaint();
	}
	
	public boolean isShowingTubeConnectivity()
	{
		return showTubeConnectivity;
	}
	
	public void setShowCostMap(boolean showCostMap)
	{
		this.showTerrainCostMap = showCostMap;
		refresh();
	}
	
	public boolean isShowingCostMap()
	{
		return showTerrainCostMap;
	}
	
	public void refresh()
	{
		synchronized (cacheLock)
		{
			cachedBackground = null;
		}
		
		repaint();
	}
	
	public void refresh(Position pos)
	{
		refresh();
	}
	
	public void refresh(Region region)
	{
		refresh();
	}
	
	public void fireCommandButton(Command command)
	{
		if (!overlays.isEmpty()) overlays.get(0).onCommand(command);
	}
	
	public void setDisplayWindow(DisplayWindow window)
	{
		this.window = window;
	}
	
	public void showStatus(Unit unit)
	{
		if (window != null)
			window.showStatus(unit);
		
		if (unit != null)
			showStatus(unit.getOwner());
	}
	
	public void showStatus(Player player)
	{
		if (window != null)
			window.showStatus(player);
		
		currentPlayer = player;
	}
	
	public void showFrameNumber(int frameNumber, double frameRate)
	{
		if (window != null)
			window.showFrameNumber(frameNumber, frameRate);
	}
	
	public void completeOverlays()
	{
		overlays.clear();
		adapter.setOverlay(null);
		setAnimatedCursor(null);
	}
	
	public InputOverlay getCurrentOverlay()
	{
		return overlays.isEmpty() ? null : overlays.getFirst();
	}
	
	public void completeOverlay(InputOverlay overlay)
	{
		if (overlay != overlays.getFirst())
			throw new IllegalArgumentException();
		
		overlays.removeFirst();
		overlay.dispose();
		overlay.setDisplay(null);
		
		if (!overlays.isEmpty())
		{
			InputOverlay oldOverlay = overlays.getFirst();
			oldOverlay.init();
			adapter.setOverlay(oldOverlay);
		}
		else
		{
			adapter.setOverlay(null);
		}
	}
	
	public void pushOverlay(InputOverlay overlay)
	{
		if (!overlays.isEmpty())
			overlays.getFirst().dispose();
		
		overlays.addFirst(overlay);
		overlay.setDisplay(this);
		overlay.init();
		adapter.setOverlay(overlay);
	}
	
	/**
	 * Returns overlays in the order they were added.
	 * The current overlay will be at the end.
	 */
	public List<InputOverlay> getOverlays()
	{
		List<InputOverlay> list = new ArrayList<InputOverlay>(overlays);
		Collections.reverse(list);
		return list;
	}
	
	public void cueAnimation(AmbientAnimation animation)
	{
		animation.setDisplay(this);
		
		synchronized (animations)
		{
			animations.add(animation);
		}
	}
	
	public List<AmbientAnimation> getAnimations()
	{
		return animations;
	}
	
	public void addDisplayObject(DisplayObject dObj, DisplayLayer layer)
	{
		synchronized (displayLayers)
		{
			dObj.setDisplayPanel(this);
			displayLayers.get(layer).add(dObj);
		}
	}
	
	public void addDisplayObject(DisplayObject dObj)
	{
		addDisplayObject(dObj, dObj.getDisplayLayer());
	}
	
	public int getTotalWidth()
	{
		return map.getWidth() * gm.tileSize;
	}
	
	public int getTotalHeight()
	{
		return map.getHeight() * gm.tileSize;
	}
	
	public int getVerticalLetterBoxSpace()
	{
		return max(0, (getHeight() - getTotalHeight()) / 2);
	}
	
	public int getHorizontalLetterBoxSpace()
	{
		return max(0, (getWidth() - getTotalWidth()) / 2);
	}
	
	public Point getViewPoint()
	{
		return gm.getOffset();
	}
	
	public Point getViewCenterPoint()
	{
		return new Point((getWidth()  / 2) - gm.xOffset, (getHeight() / 2) - gm.yOffset);
	}
	
	public GridMetrics getGridMetrics()
	{
		return gm;
	}
	
	public int getViewX()
	{
		return gm.xOffset;
	}
	
	public int getViewY()
	{
		return gm.yOffset;
	}
	
	public void setViewPoint(int x, int y)
	{
		int oldScrollX = gm.xOffset;
		int oldScrollY = gm.yOffset;
		
		gm.xOffset = x;
		gm.yOffset = y;
		alignVisibleArea();
		
		if (gm.xOffset != oldScrollX || gm.yOffset != oldScrollY)
			refresh();
	}
	
	public void shiftViewPoint(int dx, int dy)
	{
		setViewPoint(gm.xOffset + dx, gm.yOffset + dy);
	}
	
	public void setViewCenterPoint(int x, int y)
	{
		setViewPoint((getWidth()  / 2) - x, (getHeight() / 2) - y);
	}
	
	public void setViewCenterPosition(Position pos)
	{
		setViewPoint(
			(getWidth()  / 2) - (pos.x * gm.tileSize + gm.tileSize / 2),
			(getHeight() / 2) - (pos.y * gm.tileSize + gm.tileSize / 2)
		);
	}
	
	private void alignVisibleArea()
	{
		int diffX = getWidth()  - getTotalWidth();
		int diffY = getHeight() - getTotalHeight();
		gm.xOffset = (diffX > 0) ? diffX / 2 : max(min(gm.xOffset, 0), diffX);
		gm.yOffset = (diffY > 0) ? diffY / 2 : max(min(gm.yOffset, 0), diffY);
	}
	
	public void zoomIn()
	{
		zoomIn(getViewCenterPoint());
	}
	
	public void zoomOut()
	{
		zoomOut(getViewCenterPoint());
	}
	
	public void zoomNormal()
	{
		zoomNormal(getViewCenterPoint());
	}
	
	public void zoomGlobal()
	{
		setScale(minScale);
		setViewCenterPoint(getWidth() / 2, getHeight() / 2);
		refresh();
	}
	
	public void zoomIn(Position pos)
	{
		zoomIn(gm.getPoint(pos));
	}
	
	public void zoomOut(Position pos)
	{
		zoomOut(gm.getPoint(pos));
	}
	
	public void zoomNormal(Position pos)
	{
		zoomNormal(gm.getPoint(pos));
	}
	
	public void zoomIn(Point center)
	{
		if (gm.scale < maxScale)
		{
			setScaleCentered(gm.scale + 1, center);
			refresh(getDisplayRegion());
		}
	}
	
	public void zoomOut(Point center)
	{
		if (gm.scale > minScale)
		{
			setScaleCentered(gm.scale - 1, center);
			refresh(getDisplayRegion());
		}
	}
	
	public void zoomNormal(Point center)
	{
		if (gm.scale != normalScale)
		{
			setScaleCentered(normalScale, center);
			refresh(getDisplayRegion());
		}
	}
	
	/**
	 * Should be the root of methods: setSize, setBounds, resize.
	 */
	@SuppressWarnings("deprecation")
	public void reshape(int x, int y, int width, int height)
	{
		super.reshape(x, y, width, height);
		alignVisibleArea();
		refresh();
	}
	
	public int getMinimumScale()
	{
		return minScale;
	}
	
	public int getMaximumScale()
	{
		return maxScale;
	}
	
	public int getNormalScale()
	{
		return normalScale;
	}
	
	public void setNormalScale(int normalScale)
	{
		this.normalScale = normalScale;
	}
	
	/**
	 * Gets the current size of grid tiles considering the TileSet's
	 * standard tile size and the current zoom level.
	 */
	public int getTileSize()
	{
		return gm.tileSize;
	}
	
	/**
	 * Gets the current zoom scale.
	 * 0 is normal,
	 * negative values are zoomed out,
	 * positive values are zoomed in.
	 * 
	 * tileSize = baseTileSize * 2^scale
	 */
	public int getScale()
	{
		return gm.scale;
	}
	
	/**
	 * Sets the zoom scale and sets tileSize accordingly.
	 * 
	 * tileSize = baseTileSize * 2^scale
	 */
	public void setScale(int scale)
	{
		if (scale < minScale || scale > maxScale)
			throw new IllegalArgumentException("scale " + scale + " out of range");
		
		int normalTileSize = tiles.getTileSize();
		int newTileSize = scale < 0
			? normalTileSize >> -scale
			: normalTileSize << scale;
		
		gm.tileSize = newTileSize;
		gm.scale = scale;
		setPreferredSize(new Dimension(
			map.getWidth()  * gm.tileSize,
			map.getHeight() * gm.tileSize
		));
	}
	
	public void setScaleCentered(int scale, Point center)
	{
		double xFraction = center.x / (double) getTotalWidth();
		double yFraction = center.y / (double) getTotalHeight();
		setScale(scale);
		setViewCenterPoint(
			(int) (xFraction * getTotalWidth()),
			(int) (yFraction * getTotalHeight())
		);
	}
	
	public Rectangle getLetterBoxRect()
	{
		int hSpace = getHorizontalLetterBoxSpace();
		int vSpace = getVerticalLetterBoxSpace();
		
		return new Rectangle(
			hSpace,
			vSpace,
			getWidth() - 2 * hSpace,
			getHeight() - 2 * vSpace
		);
	}
	
	/**
	 * Gets the region of pixels currently visible on this display.
	 */
	public Rectangle getDisplayRect()
	{
		return new Rectangle(
			max(0, gm.xOffset),
			max(0, gm.yOffset),
			min(getTotalWidth(),  getWidth()),
			min(getTotalHeight(), getHeight())
		);
	}
	
	public Rectangle2D getGridDisplayRect()
	{
		return new Rectangle2D.Double(
			max(0, gm.xOffset) / (double) gm.tileSize,
			max(0, gm.yOffset) / (double) gm.tileSize,
			min(getTotalWidth(),  getWidth())  / (double) gm.tileSize,
			min(getTotalHeight(), getHeight()) / (double) gm.tileSize
		);
	}
	
	/**
	 * Gets the region of positions currently visible on this display.
	 */
	public Region getDisplayRegion()
	{
		return gm.getRegion(getDisplayRect());
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Paint method and delegates.
	 */
	
	/**
	 * Paints the visible rect of the map, including terrain, units and
	 * overlays.
	 */
	public void paintComponent(Graphics g0)
	{
		DisplayGraphics g = new DisplayGraphics((Graphics2D) g0);
		g.setGridMetrics(gm);
		drawLetterBox(g);
		Rectangle rect = getDisplayRect();
		Region region = gm.getRegion(rect);
		Rectangle2D absRect = region.getAbsRect();
		
		if (showBackground)
		{
			drawTerrain(g, rect);
		}
		else
		{
			g.setColor(getBackground());
			g.fill(rect);
		}
		
		if (showGrid && gm.scale >= -2)
		{
			g.setColor(Color.BLACK);
			drawGrid(g, rect, region);
		}
		
		if (showTubeConnectivity && gm.scale >= -2)
			drawTubeConnectivity(g, region);
		
		drawObjects(g, absRect);
		
		synchronized (animations)
		{
			for (AmbientAnimation animation : animations)
				if (absRect.intersects(animation.getBounds()) && gm.scale >= minShowUnitScale)
					animation.paint(g);
		}
		
		if (! overlays.isEmpty())
			overlays.getFirst().paint(g);
	}
	
	/**
	 * Draws the background-colored letterboxes around the
	 * map display area.
	 */
	private void drawLetterBox(DisplayGraphics g)
	{
		g.setColor(letterBoxColor);
		int hSpace = getHorizontalLetterBoxSpace();
		int vSpace = getVerticalLetterBoxSpace();
		
		if (getWidth() > getTotalWidth())
		{
			g.fillRect(0, 0, hSpace, getHeight());
			g.fillRect(getWidth() - hSpace - 1, 0, hSpace + 1, getHeight());
		}
		
		if (getHeight() > getTotalHeight())
		{
			g.fillRect(hSpace, 0, getWidth() - hSpace * 2, vSpace);
			g.fillRect(hSpace, getHeight() - vSpace - 1, getWidth() - hSpace * 2, vSpace + 1);
		}
	}
	
	/**
	 * Draws terrain (surface or cost map) depending on options using
	 * Graphics g in the visible rect.
	 */
	private void drawTerrain(DisplayGraphics g, Rectangle rect)
	{
		synchronized (cacheLock)
		{
			Region region = getDisplayRegion();
			region = map.getBounds().getIntersection(region);
			long time = System.currentTimeMillis();
			
			if ((cachedBackground == null) || (time - lastRefreshTime > 1000))
			{
				lastRefreshTime = time;
				cachedBackground = new RImage(rect.width, rect.height, false);
				Graphics2D bg = (Graphics2D) cachedBackground.getGraphics();
				DisplayGraphics cg = new DisplayGraphics(bg);
				cg.setGridMetrics(g.getGridMetrics());
				cg.translate(min(0, -gm.xOffset), min(0, -gm.yOffset));
				
				if (showTerrainCostMap) drawCostMap(cg, region);
								   else drawSurface(cg, region);
				
				cg.dispose();
			}
			
			Rectangle letterBox = getLetterBoxRect();
			g.drawImage(cachedBackground, letterBox.x, letterBox.y, null);
		}
	}
	
	/**
	 * Draws the terrain costmap using Graphics g with in given visible Region.
	 */
	private void drawCostMap(DisplayGraphics g, Region region)
	{
		CostMap costMap = map.getTerrainCostMap();
		g.setFont(costMapFont);
		
		for (Position pos : region)
		{
			RColor color = RColor.getGray(costMap.getScaleFactor(pos));
			g.setColor(color);
			g.fill(pos);
			
			if (gm.scale >= -1)
			{
				g.setColor(color.invert().toBlackWhite());
				double cost = costMap.get(pos);
				String costLabel = null;
				
				if (costMap.isInfinite(pos)) costLabel = "Inf";
				else if (gm.scale >= 0)  costLabel = String.format("%.2f", cost);
				else if (gm.scale == -1) costLabel = String.valueOf((int) cost);
				
				if (costLabel != null)
					g.drawString(costLabel, pos);
			}
		}
	}
	
	/**
	 * Draws the terrain surface image using Graphics g in the visible Region.
	 */
	private void drawSurface(DisplayGraphics g, Region region)
	{
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			Tile tile = tiles.getTile(map.getTileCode(x, y));
			
			if (gm.scale >= minShowUnitScale)
			{
				g.drawImageAtPosition(tile.getImage(), x, y);
			}
			else
			{
				g.setColor(tile.getAverageColor());
				g.fillPosition(x, y);
			}
		}
	}
	
	/**
	 * Draws grid of size tileSize over Region.
	 */
	private void drawGrid(DisplayGraphics g, Rectangle rect, Region region)
	{
		for (int x = max(1, region.x); x < region.getMaxX(); ++x)
			g.drawLine(x * gm.tileSize + gm.xOffset, 0, x * gm.tileSize + gm.xOffset, getHeight() - 1);
		
		for (int y = max(1, region.y); y < region.getMaxY(); ++y)
			g.drawLine(0, y * gm.tileSize + gm.yOffset, getWidth() - 1, y * gm.tileSize + gm.yOffset);
	}
	
	/**
	 * Highlights tubes and buildings as active/potentially active or disabled.
	 */
	private void drawTubeConnectivity(DisplayGraphics g, Region region)
	{
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			Unit occupant = map.getUnit(x, y);
			boolean needsConnection = occupant != null && occupant.needsConnection();
			boolean isSource        = occupant != null && occupant.isConnectionSource();
			boolean hasConnection   = occupant != null && occupant.needsConnection()
			                                           && occupant.isConnected();
			
			if (map.hasTube(x, y) || needsConnection || isSource)
			{
				boolean alive = (map.isAlive(x, y) || hasConnection || isSource);
				g.setColor(alive ? InputOverlay.TRANS_GREEN : InputOverlay.TRANS_RED);
				g.fillPosition(x, y);
			}
		}
	}
	
	/**
	 * Draws listed display objects layer by layer, sorting as required.
	 */
	private void drawObjects(DisplayGraphics g, Rectangle2D absRect)
	{
		synchronized (displayLayers)
		{
			for (DisplayLayer layer : DisplayLayer.values())
			{
				List<DisplayObject> list = displayLayers.get(layer);
				List<DisplayObject> listCopy = new ArrayList<DisplayObject>();
				
				Iterator<DisplayObject> dObjItr = list.iterator();
				
				while (dObjItr.hasNext())
				{
					DisplayObject dObj = dObjItr.next();
					
					if (!dObj.isAlive())
					{
						dObjItr.remove();
						continue;
					}
					
					if (absRect.intersects(dObj.getBounds()))
						listCopy.add(dObj);
				}
				
				if (layer.comparator != null)
				{
					try
					{
						Collections.sort(listCopy, layer.comparator);
					}
					catch (IllegalArgumentException e)
					{
						// FIXME: Comparators.Z_ORDER is causing IllegalArgumentException
						//        "Comparison method violates its general contract!"
					}
				}
				
				for (DisplayObject dObj : listCopy)
					dObj.paint(g);
			}
		}
	}
}
