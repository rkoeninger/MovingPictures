package com.robbix.mp5.ui;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import com.robbix.mp5.Game;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.map.Tile;
import com.robbix.mp5.map.TileSet;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.ani.AmbientAnimation;
import com.robbix.mp5.ui.overlay.InputOverlay;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.AnimatedCursor;
import com.robbix.utils.CostMap;
import com.robbix.utils.GridMetrics;
import com.robbix.utils.Position;
import com.robbix.utils.RImage;
import com.robbix.utils.Region;
import com.robbix.utils.Utils;

@SuppressWarnings("serial")
public class DisplayPanel extends JComponent
{
	private LayeredMap map;
	private SpriteLibrary sprites;
	private TileSet tiles;
	private CursorSet cursors;
	
	private Point scroll = new Point();
	private int tileSize = 32;
	private int scale = 0;
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
	
	private UnitStatus unitStatus;
	private PlayerStatus playerStatus;
	private TitleBar titleBar;
	
	private Player currentPlayer;
	
	private AnimatedCursor cursor;
	
	private LinkedList<InputOverlay> overlays;
	private InputOverlay.ListenerAdapter adapter;
	
	private List<AmbientAnimation> animations = new LinkedList<AmbientAnimation>();
	
	private List<DisplayObject> displayObjects = new LinkedList<DisplayObject>();
	
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
		this.tileSize = tiles.getTileSize();
		this.normalScale = 0;
		this.maxScale = 3;
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
		return max(0, (getHeight() - getTotalHeight()) / 2);
	}
	
	public int getHorizontalLetterBoxSpace()
	{
		return max(0, (getWidth() - getTotalWidth()) / 2);
	}
	
	public Point getViewPoint()
	{
		return new Point(scroll);
	}
	
	public Point getViewCenterPoint()
	{
		return new Point(
			 (getWidth()  / 2) - scroll.x,
			 (getHeight() / 2) - scroll.y
		);
	}
	
	public int getViewX()
	{
		return scroll.x;
	}
	
	public int getViewY()
	{
		return scroll.y;
	}
	
	public void setViewPoint(int x, int y)
	{
		int oldScrollX = scroll.x;
		int oldScrollY = scroll.y;
		
		scroll.x = x;
		scroll.y = y;
		centerAsNeeded();
		
		if (scroll.x != oldScrollX || scroll.y != oldScrollY)
			refresh();
	}
	
	public void shiftViewPoint(int dx, int dy)
	{
		setViewPoint(
			scroll.x + dx,
			scroll.y + dy
		);
	}
	
	public void setViewCenterPoint(int x, int y)
	{
		setViewPoint(
			(getWidth()  / 2) - x,
			(getHeight() / 2) - y
		);
	}
	
	public void setViewCenterPosition(Position pos)
	{
		setViewPoint(
			(getWidth()  / 2) - (pos.x * tileSize + tileSize / 2),
			(getHeight() / 2) - (pos.y * tileSize + tileSize / 2)
		);
	}
	
	private void centerAsNeeded()
	{
		int diffWidth  = getWidth()  - getTotalWidth();
		int diffHeight = getHeight() - getTotalHeight();
		
		scroll.x = (diffWidth > 0)
			? diffWidth / 2
			: max(min(scroll.x, 0), diffWidth);
		
		scroll.y = (diffHeight > 0)
			? diffHeight / 2
			: max(min(scroll.y, 0), diffHeight);
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
		if (scale != normalScale)
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
		centerAsNeeded();
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
		if (scale < minScale || scale > maxScale)
			throw new IllegalArgumentException("scale " + scale + " out of range");
		
		int normalTileSize = tiles.getTileSize();
		int newTileSize = scale < 0
			? normalTileSize >> -scale
			: normalTileSize << scale;
		
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
		setViewCenterPoint(
			(int) (xFraction * getTotalWidth()),
			(int) (yFraction * getTotalHeight())
		);
	}
	
	/**
	 * Translates a pixel Point on the panel to a grid Position on the map.
	 */
	public Position getPosition(Point point)
	{
		return getPosition(point.x, point.y);
	}
	
	/**
	 * Translates a pair of pixel co-ordinates on the panel
	 * to a grid Position on the map.
	 */
	public Position getPosition(int x, int y)
	{
		if (x < max(0, scroll.x)
		 || y < max(0, scroll.y)
		 || x > min(getWidth(),  scroll.x + getTotalWidth())
		 || y > min(getHeight(), scroll.y + getTotalHeight()))
			return null;
		
		return new Position(
			(x - scroll.x) / tileSize,
			(y - scroll.y) / tileSize
		);
	}
	
	/**
	 * Translates a grid Position on the map to a pixel Point on the panel
	 * that is the upper-left corner of that grid Position as drawn.
	 */
	public Point getPoint(Position pos)
	{
		return new Point(
			pos.x * tileSize + scroll.x,
			pos.y * tileSize + scroll.y
		);
	}
	
	/**
	 * Translates a grid Position on the map to a pixel Point on the panel
	 * that is in the center of that grid Position as drawn.
	 */
	public Point getCenteredPoint(Position pos)
	{
		int half = tileSize / 2;
		return new Point(
			pos.x * tileSize + half + scroll.x,
			pos.y * tileSize + half + scroll.y
		);
	}
	
	/**
	 * Translates a pixel Rectangle on the panel to a grid Region,
	 * including all grid spots the Rectangle intersects.
	 */
	public Region getRegion(Rectangle rect)
	{
		int minX = (int) floor((rect.x - scroll.x) / (double) tileSize);
		int minY = (int) floor((rect.y - scroll.y) / (double) tileSize);
		int maxX = (int) ceil((rect.x - scroll.x + rect.width) / (double) tileSize);
		int maxY = (int) ceil((rect.y - scroll.y + rect.height) / (double) tileSize);
		
		return map.getBounds().getIntersection(
			new Region(minX, minY, maxX - minX, maxY - minY)
		);
	}
	
	/**
	 * Translates a pixel Rectangle on the panel to a grid Region,
	 * including only grid spots fully enclosed by the Rectangle.
	 */
	public Region getEnclosedRegion(Rectangle rect)
	{
		int minX = (int) ceil((rect.x - scroll.x) / (double) tileSize);
		int minY = (int) ceil((rect.y - scroll.y) / (double) tileSize);
		int maxX = (int) floor((rect.x - scroll.x + rect.width) / (double) tileSize);
		int maxY = (int) floor((rect.y - scroll.y + rect.height) / (double) tileSize);
		
		return map.getBounds().getIntersection(
			new Region(minX, minY, maxX - minX, maxY - minY)
		);
	}
	
	/**
	 * Translates a grid Region on the map to a pixel Rectangle on the panel.
	 */
	public Rectangle getRectangle(Region region)
	{
		return new Rectangle(
			region.x * tileSize + scroll.x,
			region.y * tileSize + scroll.y,
			region.w * tileSize,
			region.h * tileSize
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
			max(0, scroll.x),
			max(0, scroll.y),
			min(getTotalWidth(),  getWidth()),
			min(getTotalHeight(), getHeight())
		);
	}
	
	public Rectangle2D getGridDisplayRect()
	{
		return new Rectangle2D.Double(
			max(0, scroll.x) / (double) tileSize,
			max(0, scroll.y) / (double) tileSize,
			min(getTotalWidth(),  getWidth())  / (double) tileSize,
			min(getTotalHeight(), getHeight()) / (double) tileSize
		);
	}
	
	/**
	 * Gets the region of positions currently visible on this display.
	 */
	public Region getDisplayRegion()
	{
		return getRegion(getDisplayRect());
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
		g.setGridMetrics(new GridMetrics(scroll.x, scroll.y, tileSize, scale));
		drawLetterBox(g);
		Rectangle rect = getDisplayRect();
		Region region = getRegion(rect);
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
		
		if (showGrid && tileSize >= 8)
		{
			g.setColor(Color.BLACK);
			drawGrid(g, rect, region);
		}
		
		if (showTubeConnectivity)
			drawTubeConnectivity(g, region);
		
		for (Unit unit : map.getUnitIterator(true))
			if (region.intersects(unit.getOccupiedBounds()))
				drawUnit(g, unit);
		
		for (DisplayObject dObj : displayObjects)
			if (absRect.intersects(dObj.getBounds()))
				dObj.paint(g);
		
		synchronized (animations)
		{
			for (AmbientAnimation animation : animations)
				if (absRect.intersects(animation.getBounds()) && scale >= minShowUnitScale)
					animation.paint(g);
		}
		
		for (ResourceDeposit res : map.getResourceDeposits())
			if (region.contains(res.getPosition()))
				drawResourceDeposit(g, res);
		
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
				DisplayGraphics cg = new DisplayGraphics((Graphics2D) cachedBackground.getGraphics());
				cg.setGridMetrics(g.getGridMetrics());
				cg.translate(min(0, -scroll.x), min(0, -scroll.y));
				
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
			Color color = Utils.getGrayscale(costMap.getScaleFactor(pos));
			g.setColor(color);
			g.fill(pos);
			
			if (scale >= -1)
			{
				g.setColor(Utils.getBlackWhiteComplement(color));
				double cost = costMap.get(pos);
				String costLabel = null;
				
				if (costMap.isInfinite(pos)) costLabel = "Inf";
				else if (scale >= 0)  costLabel = String.format("%.2f", cost);
				else if (scale == -1) costLabel = String.valueOf((int) cost);
				
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
		for (Position pos : region)
		{
			Tile tile = tiles.getTile(map.getTileCode(pos));
			
			if (scale >= minShowUnitScale)
			{
				g.drawImage(tile.getImage(), pos);
			}
			else
			{
				g.setColor(tile.getAverageColor());
				g.fill(pos);
			}
			
			if (map.hasMinePlatform(pos))
			{
				g.draw(sprites.getSprite("aCommonMine", "platform"), pos);
			}
			else if (map.hasGeyser(pos))
			{
				g.draw(sprites.getSprite("aGeyser", "geyser"), pos);
			}
		}
	}
	
	/**
	 * Draws grid of size tileSize over Region.
	 */
	private void drawGrid(DisplayGraphics g, Rectangle rect, Region region)
	{
		for (int x = max(1, region.x); x < region.getMaxX(); ++x)
			g.drawLine(x * tileSize + scroll.x, 0, x * tileSize + scroll.x, getHeight() - 1);
		
		for (int y = max(1, region.y); y < region.getMaxY(); ++y)
			g.drawLine(0, y * tileSize + scroll.y, getWidth() - 1, y * tileSize + scroll.y);
	}
	
	/**
	 * Highlights tubes and buildings as active/potentially active or disabled.
	 */
	private void drawTubeConnectivity(DisplayGraphics g, Region region)
	{
		Color transGreen = InputOverlay.GREEN.getFill();
		Color transRed = InputOverlay.RED.getFill();
		
		for (int x = region.x; x < region.getMaxX(); ++x)
		for (int y = region.y; y < region.getMaxY(); ++y)
		{
			Position pos = new Position(x, y);
			Unit occupant = map.getUnit(pos);
			boolean needsConnection = occupant != null && occupant.needsConnection();
			boolean isSource = occupant != null && occupant.isConnectionSource();
			boolean hasConnection = occupant != null && occupant.needsConnection()
													 && occupant.isConnected();
			
			if (map.hasTube(pos) || needsConnection || isSource)
			{
				boolean alive = (map.isAlive(pos) || hasConnection || isSource);
				g.setColor(alive ? transGreen : transRed);
				g.fill(pos);
			}
		}
	}
	
	/**
	 * Gets the sprite for the given Unit in its current state and renders it
	 * along with any overlays - i.e. the UnitLayer state.
	 */
	private void drawUnit(DisplayGraphics g, Unit unit)
	{
		if (!unit.isTurret() && scale < minShowUnitScale)
		{
			g.setColor(unit.getOwner().getColor());
			g.fill(unit.getOccupiedBounds());
			return;
		}
		
		Point2D point = unit.getAbsPoint();
		Sprite sprite = sprites.getSprite(unit);
		
		if (showShadows && !unit.isTurret())
		{
			Point2D shadowPoint = new Point2D.Double(
				point.getX() + shadowXOffset,
				point.getY() + shadowYOffset
			);
			drawShadow(g, sprite, shadowPoint);
		}
		
		if (!unit.isTurret() && sprite == SpriteSet.BLANK_SPRITE)
		{
			g.setColor(unit.getOwner().getColor());
			g.fill(unit.getOccupiedBounds());
		}
		else
		{
			int hue = unit.getOwner() != null ? unit.getOwner().getColorHue() : -1;
			g.draw(sprite, point, hue);
		}
		
		if (unit.hasTurret())
		{
			drawUnit(g, unit.getTurret());
		}
		else if (unit.isGuardPost() || unit.isStructure())
		{
			drawStatusLight(g, unit);
		}
	}
	
	/**
	 * Draws upper-left corner status light for structures and guard posts.
	 * Green "active" lights are not draw for guard posts.
	 * Unit must be owned by current viewing player to have status light drawn.
	 */
	private void drawStatusLight(DisplayGraphics g, Unit unit)
	{
		if (!currentPlayer.owns(unit))
			return;
		
		Position pos = unit.getPosition();
		
		if (unit.isIdle())
		{
			g.draw(sprites.getSprite("aStructureStatus", "idle"), pos);
		}
		else if (unit.isDisabled())
		{
			SpriteGroup seq = sprites.getAmbientSpriteGroup("aStructureStatus", "disabled");
			int index = Utils.getTimeBasedIndex(100, seq.getSpriteCount());
			g.draw(seq.getSprite(index), pos);
		}
		else if (unit.isStructure())
		{
			g.draw(sprites.getSprite("aStructureStatus", "active"), pos);
		}
	}
	
	private static final Color COMMON_ORE_COLOR = new Color(255, 92, 0);
	private static final Color RARE_ORE_COLOR = new Color(255, 255, 106);
	
	/**
	 * Draws a ResourceDeposit at that deposit's position.
	 */
	private void drawResourceDeposit(DisplayGraphics g, ResourceDeposit res)
	{
		if (scale < -1)
		{
			Color color = res.getType() == ResourceType.COMMON_ORE
				? COMMON_ORE_COLOR
				: RARE_ORE_COLOR;
			g.setColor(color);
			g.fill(res.getPosition());
		}
		else
		{
			Sprite sprite = res.isSurveyedBy(currentPlayer)
				? sprites.getSprite(res)
				: sprites.getUnknownDepositSprite();
			g.draw(sprite, res.getPosition());
		}
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Draw helpers. Helpers account for tileSize and scrollPoint.
	 */
	
	private void drawShadow(Graphics g, Sprite sprite, Point2D absPoint)
	{
		Image img = sprite.getShadow();
		int x = (int) (absPoint.getX() * tileSize) + scroll.x + sprite.getXOffset(scale);
		int y = (int) (absPoint.getY() * tileSize) + scroll.y + sprite.getYOffset(scale);
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
}
