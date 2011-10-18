package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Grid;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.map.TileSet;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.ani.AmbientAnimation;
import com.robbix.mp5.ui.overlay.InputOverlay;
import com.robbix.mp5.unit.Unit;

@SuppressWarnings("serial")
public class DisplayPanel extends JComponent
{
	private LayeredMap map;
	private SpriteLibrary sprites;
	private TileSet tiles;
	private CursorSet cursors;
	
	private Grid<Boolean> dirty;
	private Filter<Boolean> trueFilter = Filter.getTrueFilter();
	
	private Point scrollPoint = new Point();
	private int tileSize = 32;
	private int scale = 0;
	private final int minScale, maxScale;
	
	private Map<Integer, BufferedImage> cachedBackgrounds = null;
	private Object cacheLock = new Object();
	
	private static Font costMapFont = Font.decode("Arial-9");
	private static Color TRANS_GRAY = new Color(0, 0, 0, 127);
	private static Color BACKGROUND_BLUE = new Color(127, 127, 255);
	
	private UnitStatus unitStatus;
	private PlayerStatus playerStatus;
	private TitleBar titleBar;
	
	private Player currentPlayer;
	
	private LinkedList<InputOverlay> overlays;
	private InputOverlay.ListenerAdapter adapter;
	
	private List<AmbientAnimation> animations =
		new LinkedList<AmbientAnimation>();
	
	private List<String> options = Arrays.asList(
		"Grid",
		"Background",
		"Unit Layer State",
		"Tube Connectivity",
		"Terrain Cost Map",
		"Terrain Cost Values",
		"Cost Values as Factors",
		"Night"
	);
	
	private boolean showGrid = false;
	private boolean showUnitLayerState = false;
	private boolean showTubeConnectivity = false;
	private boolean showTerrainCostMap = false;
	private boolean showTerrainCostValues = false;
	private boolean showCostValuesAsFactors = false;
	private boolean showBackground = true;
	
	private boolean night = false;
	
	private AnimatedCursor cursor = null;
	private Timer cursorTimer = null;
	
	public DisplayPanel(
		LayeredMap map,
		SpriteLibrary sprites,
		TileSet tiles,
		CursorSet cursors)
	{
		if (map == null || sprites == null || tiles == null || cursors == null)
			throw new IllegalArgumentException();
		
		this.map = map;
		map.setDisplayPanel(this);
		dirty = new Grid<Boolean>(map.getBounds(), true);
		this.sprites = sprites;
		this.tiles = tiles;
		this.tileSize = tiles.getTileSize();
		this.maxScale = 0;
		this.minScale = -Utils.log2(tileSize);
		this.cursors = cursors;
		this.overlays = new LinkedList<InputOverlay>();
		setBackground(BACKGROUND_BLUE);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		Dimension size = new Dimension(
			map.getWidth()  * tileSize,
			map.getHeight() * tileSize
		);
		setPreferredSize(size);
		this.cachedBackgrounds = new HashMap<Integer, BufferedImage>();
		MouseEvents mouseEvents = new MouseEvents();
		addMouseWheelListener(mouseEvents);
		addMouseMotionListener(mouseEvents);
		addMouseListener(mouseEvents);
		this.adapter = new InputOverlay.ListenerAdapter();
		addKeyListener(adapter);
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
	}
	
	public List<String> getOptionNames()
	{
		return options;
	}
	
	public boolean getOptionValue(String name)
	{
		if (name.equals("Grid"))
		{
			return showGrid;
		}
		else if (name.equals("Background"))
		{
			return showBackground;
		}
		else if (name.equals("Tube Connectivity"))
		{
			return showTubeConnectivity;
		}
		else if (name.equals("Unit Layer State"))
		{
			return showUnitLayerState;
		}
		else if (name.equals("Terrain Cost Map"))
		{
			return showTerrainCostMap;
		}
		else if (name.equals("Terrain Cost Values"))
		{
			return showTerrainCostValues;
		}
		else if (name.equals("Cost Values as Factors"))
		{
			return showCostValuesAsFactors;
		}
		else if (name.equals("Night"))
		{
			return night;
		}
		
		throw new IllegalArgumentException("Not an option: " + name);
	}
	
	public void setOptionValue(String name, boolean isSet)
	{
		if (name.equals("Grid"))
		{
			showGrid = isSet;
			repaint();
		}
		else if (name.equals("Background"))
		{
			showBackground = isSet;
			refresh();
		}
		else if (name.equals("Tube Connectivity"))
		{
			showTubeConnectivity = isSet;
			repaint();
		}
		else if (name.equals("Unit Layer State"))
		{
			showUnitLayerState = isSet;
			repaint();
		}
		else if (name.equals("Terrain Cost Map"))
		{
			showTerrainCostMap = isSet;
			refresh();
		}
		else if (name.equals("Terrain Cost Values"))
		{
			showTerrainCostValues = isSet;
			refresh();
		}
		else if (name.equals("Cost Values as Factors"))
		{
			showCostValuesAsFactors = isSet;
			refresh();
		}
		else if (name.equals("Night"))
		{
			night = isSet;
			repaint();
		}
		else
		{
			throw new IllegalArgumentException("Not an option: " + name);
		}
	}
	
	public void refresh()
	{
		synchronized (cacheLock)
		{
			dirty.fill(true);
		}
		
		repaint();
	}
	
	public void refresh(Position pos)
	{
		synchronized (cacheLock)
		{
			dirty.set(pos, true);
		}
		
		repaint();
	}
	
	public void refresh(Region region)
	{
		synchronized (cacheLock)
		{
			dirty.fill(map.getBounds().getIntersection(region), true);
		}
		
		repaint();
	}
	
	public void fireCommandButton(String command)
	{
		if (!overlays.isEmpty()) overlays.get(0).onCommand(command);
	}
	
	public void setUnitStatus(UnitStatus status)
	{
		this.unitStatus = status;
	}
	
	public void setPlayerStatus(PlayerStatus status)
	{
		this.playerStatus = status;
	}
	
	public void setTitleBar(TitleBar titleBar)
	{
		this.titleBar = titleBar;
	}
	
	public void showStatus(Unit unit)
	{
		if (unitStatus != null)
			unitStatus.showStatus(unit);
		
		if (unit != null)
			showStatus(unit.getOwner());
	}
	
	public void showStatus(Player player)
	{
		if (playerStatus != null)
			playerStatus.showStatus(player);
		
		currentPlayer = player;
	}
	
	public void showFrameNumber(int frameNumber, double frameRate)
	{
		if (titleBar != null)
			titleBar.showFrameNumber(frameNumber, frameRate);
	}
	
	public void setAnimatedCursor(String name)
	{
		this.cursor = name == null ? null : cursors.getCursor(name);
		
		if (cursorTimer != null)
		{
			cursorTimer.stop();
			cursorTimer = null;
		}
		
		if (cursor == null)
		{
			setCursor(Cursor.getDefaultCursor());
			return;
		}
		
		cursorTimer = new Timer(0, new ActionListener()
		{
			int cursorIndex = 0;
			
			public void actionPerformed(ActionEvent e)
			{
				setCursor(cursor.getCursor(cursorIndex));
				int delayMillis = cursor.getDelay(cursorIndex);
				cursorTimer.setDelay(delayMillis);
				cursorIndex++;
				
				if (cursorIndex == cursor.getFrameCount())
					cursorIndex = 0;
			}
		});
		cursorTimer.start();
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
	
	public void completeOverlays()
	{
		overlays.clear();
		adapter.setOverlay(null);
		setAnimatedCursor(null);
	}
	
	public void completeOverlay(InputOverlay overlay)
	{
		if (!overlay.equals(overlays.getFirst()))
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
		overlays.addFirst(overlay);
		overlay.setDisplay(this);
		overlay.init();
		adapter.setOverlay(overlay);
	}
	
	public void setShowBackground(boolean showBackground)
	{
		this.showBackground = showBackground;
		repaint();
	}
	
	public boolean getShowBackground()
	{
		return showBackground;
	}
	
	public void setShowUnitLayerState(boolean showUnitLayerState)
	{
		this.showUnitLayerState = showUnitLayerState;
		repaint();
	}
	
	public boolean getShowUnitLayerState()
	{
		return showUnitLayerState;
	}
	
	public void setShowGrid(boolean showGrid)
	{
		this.showGrid = showGrid;
		repaint();
	}
	
	public boolean getShowGrid()
	{
		return showGrid;
	}
	
	public void setShowTerrainCostMap(boolean showTerrainCostMap)
	{
		this.showTerrainCostMap = showTerrainCostMap;
		refresh();
	}
	
	public boolean getShowTerrainCostMap()
	{
		return showTerrainCostMap;
	}

	public void setShowTerrainCostValues(boolean showTerrainCostValues)
	{
		this.showTerrainCostValues = showTerrainCostValues;
		refresh();
	}
	
	public boolean getShowTerrainCostValues()
	{
		return showTerrainCostValues;
	}
	
	public void setShowCostValuesAsFactors(boolean showCostValuesAsFactors)
	{
		this.showCostValuesAsFactors = showCostValuesAsFactors;
		refresh();
	}
	
	public boolean getShowCostValuesAsFactors()
	{
		return showCostValuesAsFactors;
	}
	
	public void cueAnimation(AmbientAnimation animation)
	{
		synchronized (animations)
		{
			animations.add(animation);
		}
	}
	
	public List<AmbientAnimation> getAnimations()
	{
		return animations;
	}
	
	private class MouseEvents extends MouseAdapter
	{
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			if (e.isControlDown())
			{
				Point zoomPoint = e.getPoint();
				zoomPoint.x -= scrollPoint.x;
				zoomPoint.y -= scrollPoint.y;
				
				if (e.getWheelRotation() > 0) zoomIn(zoomPoint);
				else                          zoomOut(zoomPoint);
			}
		}
		
		public void mouseClicked(MouseEvent e)
		{
			if (e.isControlDown() && e.getButton() == MouseEvent.BUTTON2)
			{
				if (tileSize == tiles.getTileSize())
				{
					zoomGlobal();
				}
				else
				{
					Point zoomPoint = e.getPoint();
					zoomPoint.x -= scrollPoint.x;
					zoomPoint.y -= scrollPoint.y;
					zoomNormal(zoomPoint);
				}
			}
		}
	}
	
	public int getTotalWidth()
	{
		return map.getWidth() * tileSize;
	}
	
	public int getTotalHeight()
	{
		return map.getHeight() * tileSize;
	}
	
	public int getVerticalLetterBoxSpace()
	{
		return Math.max(0, (getHeight() - getTotalHeight()) / 2);
	}
	
	public int getHorizontalLetterBoxSpace()
	{
		return Math.max(0, (getWidth() - getTotalWidth()) / 2);
	}
	
	public Point getViewPosition()
	{
		return new Point(scrollPoint);
	}
	
	public Point getViewCenterPosition()
	{
		return new Point(
			 (getWidth()  / 2) - scrollPoint.x,
			 (getHeight() / 2) - scrollPoint.y
		);
	}
	
	public int getViewX()
	{
		return scrollPoint.x;
	}
	
	public int getViewY()
	{
		return scrollPoint.y;
	}
	
	public Point addViewOffset(Point p)
	{
		p.translate(-scrollPoint.x, -scrollPoint.y);
		return p;
	}
	
	public Point subtractViewOffset(Point p)
	{
		p.translate(scrollPoint.x, scrollPoint.y);
		return p;
	}
	
	public void setViewPosition(int x, int y)
	{
		scrollPoint.x = x;
		scrollPoint.y = y;
		centerAsNeeded();
		refresh(getDisplayRegion());
	}
	
	public void shiftViewPosition(int dx, int dy)
	{
		setViewPosition(
			scrollPoint.x + dx,
			scrollPoint.y + dy
		);
	}
	
	public void setViewCenterPosition(int x, int y)
	{
		setViewPosition(
			(getWidth()  / 2) - x,
			(getHeight() / 2) - y
		);
	}
	
	public void setViewCenterPosition(Position pos)
	{
		setViewPosition(
			(getWidth()  / 2) - (pos.x * tileSize + tileSize / 2),
			(getHeight() / 2) - (pos.y * tileSize + tileSize / 2)
		);
	}
	
	private void centerAsNeeded()
	{
		int diffWidth  = getWidth()  - getTotalWidth();
		int diffHeight = getHeight() - getTotalHeight();
		
		scrollPoint.x = (diffWidth > 0)
			? diffWidth / 2
			: Math.max(Math.min(scrollPoint.x, 0), diffWidth);
		
		scrollPoint.y = (diffHeight > 0)
			? diffHeight / 2
			: Math.max(Math.min(scrollPoint.y, 0), diffHeight);
		
		refresh(getDisplayRegion());
	}
	
	public void zoomIn()
	{
		zoomIn(getViewCenterPosition());
	}
	
	public void zoomOut()
	{
		zoomOut(getViewCenterPosition());
	}
	
	public void zoomNormal()
	{
		zoomNormal(getViewCenterPosition());
	}
	
	public void zoomGlobal()
	{
		setScale(minScale);
		setViewCenterPosition(getWidth() / 2, getHeight() / 2);
		refresh();
	}
	
	public void zoomIn(Position pos)
	{
		zoomIn(getPoint(pos));
	}
	
	public void zoomOut(Position pos)
	{
		zoomOut(getPoint(pos));
	}
	
	public void zoomNormal(Position pos)
	{
		zoomNormal(getPoint(pos));
	}
	
	public void zoomIn(Point center)
	{
		if (scale < maxScale)
		{
			setScaleCentered(scale + 1, center);
			refresh(getDisplayRegion());
		}
	}
	
	public void zoomOut(Point center)
	{
		if (scale > minScale)
		{
			setScaleCentered(scale - 1, center);
			refresh(getDisplayRegion());
		}
	}
	
	public void zoomNormal(Point center)
	{
		if (scale != 0)
		{
			setScaleCentered(0, center);
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
		centerAsNeeded();
	}
	
	/**
	 * Gets the current size of grid tiles considering the TileSet's
	 * standard tile size and the current zoom level.
	 */
	public int getTileSize()
	{
		return tileSize;
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
		return scale;
	}
	
	/**
	 * Sets the zoom scale and sets tileSize accordingly.
	 * 
	 * tileSize = baseTileSize * 2^scale
	 */
	public void setScale(int scale)
	{
		int normalTileSize = tiles.getTileSize();
		int newTileSize = scale < 0
			? normalTileSize >> -scale
			: normalTileSize << scale;
		
		if (newTileSize < 1 || newTileSize > 32)
			throw new IllegalArgumentException("scale " + scale + " out of range");
		
		this.tileSize = newTileSize;
		this.scale = scale;
		setPreferredSize(new Dimension(
			map.getWidth()  * tileSize,
			map.getHeight() * tileSize
		));
	}
	
	public void setScaleCentered(int scale, Point center)
	{
		double xFraction = center.x / (double) getTotalWidth();
		double yFraction = center.y / (double) getTotalHeight();
		setScale(scale);
		setViewCenterPosition(
			(int) (xFraction * getTotalWidth()),
			(int) (yFraction * getTotalHeight())
		);
	}
	
	/**
	 * Translates a pixel Point on the panel to a grid Position on the map.
	 */
	public Position getPosition(Point point)
	{
		return Mediator.getPosition(point.x / tileSize, point.y / tileSize);
	}
	
	/**
	 * Translates a pair of pixel co-ordinates on the panel
	 * to a grid Position on the map.
	 */
	public Position getPosition(int absX, int absY)
	{
		return Mediator.getPosition(absX / tileSize, absY / tileSize);
	}
	
	/**
	 * Translates a grid Position on the map to a pixel Point on the panel
	 * that is the upper-left corner of that grid Position as drawn.
	 */
	public Point getPoint(Position pos)
	{
		return new Point(pos.x * tileSize, pos.y * tileSize);
	}

	/**
	 * Translates a grid Position on the map to a pixel Point on the panel
	 * that is in the center of that grid Position as drawn.
	 */
	public Point getCenteredPoint(Position pos)
	{
		int half = tileSize / 2;
		return new Point(pos.x * tileSize + half, pos.y * tileSize + half);
	}
	
	/**
	 * Translates a pixel Rectangle on the panel to a grid Region,
	 * including all grid spots the Rectangle intersects.
	 */
	public Region getRegion(Rectangle rect)
	{
		int minX = (int) Math.floor(rect.x / (double) tileSize);
		int minY = (int) Math.floor(rect.y / (double) tileSize);
		int maxX = (int) Math.ceil((rect.x + rect.width) / (double) tileSize);
		int maxY = (int) Math.ceil((rect.y + rect.height) / (double) tileSize);
		
		return new Region(minX, minY, maxX - minX, maxY - minY);
	}
	
	/**
	 * Translates a pixel Rectangle on the panel to a grid Region,
	 * including only grid spots fully enclosed by the Rectangle.
	 */
	public Region getEnclosedRegion(Rectangle rect)
	{
		int minX = (int) Math.ceil(rect.x / (double) tileSize);
		int minY = (int) Math.ceil(rect.y / (double) tileSize);
		int maxX = (int) Math.floor((rect.x + rect.width) / (double) tileSize);
		int maxY = (int) Math.floor((rect.y + rect.height) / (double) tileSize);
		
		return new Region(minX, minY, maxX - minX, maxY - minY);
	}
	
	/**
	 * Translates a grid Region on the map to a pixel Rectangle on the panel.
	 */
	public Rectangle getRectangle(Region region)
	{
		return new Rectangle(
			region.x * tileSize,
			region.y * tileSize,
			region.w * tileSize,
			region.h * tileSize
		);
	}
	
	/**
	 * Gets the region of pixels currently visible on this display.
	 */
	public Rectangle getDisplayRect()
	{
		return new Rectangle(
			Math.max(0, -scrollPoint.x),
			Math.max(0, -scrollPoint.y),
			Math.min(getTotalWidth(),  getWidth()),
			Math.min(getTotalHeight(), getHeight())
		);
	}
	
	/**
	 * Gets the region of positions currently visible on this display.
	 */
	public Region getDisplayRegion()
	{
		return getRegion(getDisplayRect());
	}
	
	public void draw(Graphics g, Position pos)
	{
		g.drawRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
	}
	
	public void fill(Graphics g, Position pos)
	{
		g.fillRect(pos.x * tileSize, pos.y * tileSize, tileSize, tileSize);
	}
	
	public void drawPosition(Graphics g, int x, int y)
	{
		g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
	}
	
	public void fillPosition(Graphics g, int x, int y)
	{
		g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
	}
	
	public void draw(Graphics g, Region region)
	{
		g.drawRect(
			region.x * tileSize,
			region.y * tileSize,
			region.w * tileSize,
			region.h * tileSize
		);
	}
	
	public void fill(Graphics g, Region region)
	{
		g.fillRect(
				region.x * tileSize,
				region.y * tileSize,
				region.w * tileSize,
				region.h * tileSize
			);
	}
	
	public void draw(Graphics g, Rectangle rect)
	{
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void fill(Graphics g, Rectangle rect)
	{
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void draw(Graphics g, Image img, Point point)
	{
		g.drawImage(img, point.x, point.y, null);
	}
	
	public void draw(Graphics g, Image img, Rectangle rect)
	{
		g.drawImage(
			img,
			rect.x, rect.y, rect.x + rect.width, rect.y + rect.height,
			rect.x, rect.y, rect.x + rect.width, rect.y + rect.height,
			null
		);
	}
	
	public void draw(Graphics g, Image img, Position pos)
	{
		g.drawImage(img, pos.x * tileSize, pos.y * tileSize, null);
	}
	
	public void draw(Graphics g, Sprite sprite, Position pos)
	{
		Image img = sprite.getImage();
		int x = pos.x * tileSize + sprite.getXOffset(scale);
		int y = pos.y * tileSize + sprite.getYOffset(scale);
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int w2 = scale < 0 ? w >> -scale : w << scale;
		int h2 = scale < 0 ? h >> -scale : h << scale;
		g.drawImage(
			img,
			x, y, x + w2, y + h2,
			0, 0, w, h,
			null
		);
	}
	
	public void draw(Graphics g, Sprite sprite, Point point, Player owner)
	{
		Image img = sprite.getImage(owner == null ? -1 : owner.getColorHue());
		int x = point.x + sprite.getXOffset(scale);
		int y = point.y + sprite.getYOffset(scale);
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int w2 = scale < 0 ? w >> -scale : w << scale;
		int h2 = scale < 0 ? h >> -scale : h << scale;
		g.drawImage(
			img,
			x, y, x + w2, y + h2,
			0, 0, w, h,
			null
		);
	}
	
	public void translate(Graphics g, int dx, int dy)
	{
		Rectangle bounds = g.getClipBounds();
		g.translate(bounds.x + dx, bounds.y + dy);
	}
	
	/**
	 * Paints the visible rect of the map, including terrain, units and
	 * overlays.
	 */
	public void paintComponent(Graphics g)
	{
		/*
		 * Draw letter-box around display when panel is bigger
		 * than visible area.
		 */
		g.setColor(getBackground());
		int hEdgeSpace = getHorizontalLetterBoxSpace();
		int vEdgeSpace = getVerticalLetterBoxSpace();
		g.fillRect(0, 0, hEdgeSpace, getHeight());
		g.fillRect(getWidth() - 1 - hEdgeSpace, 0, hEdgeSpace, getHeight());
		g.fillRect(0, 0, getWidth(), vEdgeSpace);
		g.fillRect(0, getHeight() - 1 - vEdgeSpace, getWidth(), vEdgeSpace);
		
		Rectangle overlayRect = new Rectangle(
			-scrollPoint.x,
			-scrollPoint.y,
			getWidth(),
			getHeight()
		);
		Rectangle rect = getDisplayRect();
		Region region = getRegion(rect);
		translate(g, scrollPoint.x, scrollPoint.y);
		
		/*
		 * Draw terrian.
		 * 
		 * Draw cached terrian/costMap image unless changes have been made.
		 */
		if (showBackground)
		{
			drawTerrain(g, rect);
		}
		else
		{
			g.setColor(Color.BLACK);
			fill(g, rect);
		}
		
		/*
		 * Draw grid
		 */
		if (showGrid && tileSize >= 8)
		{
			g.setColor(Color.BLACK);
			int w = getTotalWidth();
			int h = getTotalHeight();
			
			for (int x = Math.max(1, region.x); x < region.getMaxX(); ++x)
				g.drawLine(x * tileSize, 0, x * tileSize, h - 1);
			
			for (int y = Math.max(1, region.y); y < region.getMaxY(); ++y)
				g.drawLine(0, y * tileSize, w - 1, y * tileSize);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverTerrain(g, overlayRect);
		
		/*
		 * Draw tube connectivity
		 */
		if (showTubeConnectivity)
			drawTubes(g, region);
		
		/*
		 * Draw units.
		 * 
		 * Units are returned by UnitLayer iterator already in z-order.
		 */
		for (Unit unit : map.getUnitIterator(true))
			if (region.intersects(unit.getOccupiedBounds()))
				drawUnit(g, unit);
		
		/*
		 * Draw ambient animations
		 */
		synchronized (animations)
		{
			for (AmbientAnimation animation : animations)
				if (rect.intersects(animation.getBounds()))
					animation.paint(g);
		}
		
		/*
		 * Draw resource deposits.
		 */
		for (ResourceDeposit deposit : map.getResourceDeposits())
		{
			Point resPoint = getPoint(deposit.getPosition());
			resPoint.translate(tileSize / 2, tileSize / 2);
			
			if (!rect.contains(resPoint))
				continue;
			
			if (scale < -1)
			{
				g.setColor(deposit.getType() == ResourceType.COMMON_ORE
					? new Color(255, 92, 0)
					: new Color(255, 255, 106));
				fill(g, deposit.getPosition());
				continue;
			}
			
			Sprite sprite = deposit.isSurveyedBy(currentPlayer)
				? sprites.getSprite(deposit)
				: sprites.getUnknownDepositSprite();
			
			draw(g, sprite, deposit.getPosition());
		}
		
		/*
		 * Draw night overlay
		 */
		if (night)
		{
			g.setColor(TRANS_GRAY);
			fill(g, rect);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverUnits(g, overlayRect);
		
		translate(g, -scrollPoint.x, -scrollPoint.y);
	}
	
	/**
	 * Draws terrain (surface or cost map) depending on options using
	 * Graphics g in the visible rect.
	 */
	private void drawTerrain(Graphics g, Rectangle rect)
	{
		synchronized (cacheLock)
		{
			Region region = getDisplayRegion();
			region = map.getBounds().getIntersection(region);
			List<Position> dirtySpots = dirty.findAll(trueFilter, region);
			
			BufferedImage cachedBackground = cachedBackgrounds.get(tileSize);
			
			if (cachedBackground == null)
			{
				cachedBackground = new BufferedImage(
					map.getWidth() * tileSize,
					map.getHeight() * tileSize,
					BufferedImage.TYPE_INT_RGB
				);
				
				cachedBackgrounds.put(tileSize, cachedBackground);
			}
			
			Graphics cg = cachedBackground.getGraphics();
			
			if (showTerrainCostMap) drawCostMap(cg, dirtySpots);
							   else drawSurface(cg, dirtySpots);
			
			cg.dispose();
			draw(g, cachedBackground, rect);
		}
	}
	
	/**
	 * Draws the terrain costmap using Graphics g with in given visible Region.
	 */
	private void drawCostMap(Graphics g, List<Position> dirtySpots)
	{
		CostMap terrainCost = map.getTerrainCostMap();
		g.setFont(costMapFont);
		
		for (Position pos : dirtySpots)
		{
			g.setColor(Utils.getGrayscale(terrainCost.getScaleFactor(pos)));
			fill(g, pos);
			
			if (showTerrainCostValues)
			{
				g.setColor(Utils.getBlackWhiteComplement(g.getColor()));
				
				double cost = showCostValuesAsFactors
					? terrainCost.getScaleFactor(pos)
					: terrainCost.get(pos);
				
				g.drawString(
					Double.isInfinite(cost)
						? "Inf"
						: String.format("%.2f", cost),
					pos.x * tileSize + 3,
					pos.y * tileSize + 11
				);
			}
			
			dirty.set(pos, false);
		}
	}
	
	/**
	 * Draws the terrain surface image using Graphics g in the visible Region.
	 */
	private void drawSurface(Graphics g, List<Position> dirtySpots)
	{
		for (Position pos : dirtySpots)
		{
			draw(g, tiles.getTile(map.getTileCode(pos)).getImage(scale), pos);
			
			if (map.hasMinePlatform(pos))
			{
				draw(g, sprites.getSprite("aCommonMine", "platform"), pos);
			}
			else if (map.hasGeyser(pos))
			{
				draw(g, sprites.getSprite("aGeyser", "geyser"), pos);
			}
			
			dirty.set(pos, false);
		}
	}
	
	/**
	 * Highlights tubes and buildings as active/potentially active or disabled.
	 */
	private void drawTubes(Graphics g, Region region)
	{
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			Position pos = Mediator.getPosition(x, y);
			
			Unit occupant = map.getUnit(pos);
			boolean structNeedsConnection = occupant != null && occupant.needsConnection();
			boolean structHasConnection = occupant != null && occupant.isConnected();
			
			if (map.hasTube(pos) || structNeedsConnection)
			{
				g.setColor(
					map.isAlive(pos) || structHasConnection
					? InputOverlay.TRANS_GREEN
					: InputOverlay.TRANS_RED
				);
				
				fill(g, pos);
			}
		}
	}
	
	/**
	 * Gets the sprite for the given Unit in its current state and renders it
	 * along with any overlays - i.e. the UnitLayer state.
	 */
	private void drawUnit(Graphics g, Unit unit)
	{
		if (!unit.isTurret() && scale < -2)
		{
			g.setColor(unit.getOwner().getColor());
			fill(g, unit.getOccupiedBounds());
			return;
		}
		
		Sprite sprite = sprites.getSprite(unit);
		Point unitPoint = new Point(unit.getAbsX(), unit.getAbsY());
		
		if (showUnitLayerState && !unit.isTurret())
		{
			g.setColor(Utils.getTranslucency(Color.RED, 127));
			
			for (Position pos : unit.getFootprint().iterator(unit.getPosition()))
				fill(g, pos);
			
			g.setColor(Utils.getTranslucency(Color.BLUE, 127));
			
			for (Position pos : unit.getMap().getReservations(unit))
				fill(g, pos);
		}
		
		draw(g, sprite, unitPoint, unit.getOwner());
		
		if (unit.hasTurret())
		{
			drawUnit(g, unit.getTurret());
		}
		else
		{
			drawStatusLight(g, unit);
		}
	}
	
	/**
	 * Draws upper-left corner status light for structures and guard posts.
	 * Green "active" lights are not draw for guard posts.
	 * Unit must be owned by current viewing player to have status light drawn.
	 */
	private void drawStatusLight(Graphics g, Unit unit)
	{
		if (!currentPlayer.owns(unit))
			return;
		
		if (unit.isGuardPost() || unit.isStructure())
		{
			Position pos = unit.getPosition();
			
			if (unit.isIdle())
			{
				draw(g, sprites.getSprite("aStructureStatus", "idle"), pos);
			}
			else if (unit.isDisabled())
			{
				SpriteGroup seq = sprites.getAmbientSpriteGroup("aStructureStatus", "disabled");
				draw(g, seq.getSprite(Utils.getTimeBasedIndex(100, seq.getSpriteCount())), pos);
			}
			else if (unit.isStructure())
			{
				draw(g, sprites.getSprite("aStructureStatus", "active"), pos);
			}
		}
	}
}
