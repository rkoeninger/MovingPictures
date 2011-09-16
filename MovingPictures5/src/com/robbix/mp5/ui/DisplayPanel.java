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
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Grid;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
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
	
	// FIXME: Proper display assumes this is the same as Map.getSpotSize()
	private int tileSize = 32;
	
	private BufferedImage cachedBackground = null;
	private Object cacheLock = new Object();
	
	private static Font costMapFont = Font.decode("Arial-9");
	private static Color TRANS_GRAY = new Color(0, 0, 0, 127);
	private static Color BACKGROUND_BLUE = new Color(127, 127, 255);
	
	private UnitStatus unitStatus;
	private PlayerStatus playerStatus;
	private TitleBar titleBar;
	
	private Player currentPlayer;
	
	private LinkedList<InputOverlay> overlays;
	
	private List<AmbientAnimation> animations =
		new LinkedList<AmbientAnimation>();
	
	private List<String> options = Arrays.asList(
		"Grid",
		"Background",
		"Unit Layer State",
		"Terrain Cost Map",
		"Terrain Cost Values",
		"Cost Values as Factors",
		"Night"
	);
	
	private boolean showGrid = false;
	private boolean showUnitLayerState = false;
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
		this.cachedBackground = new BufferedImage(
			size.width,
			size.height,
			BufferedImage.TYPE_INT_RGB
		);
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
		while (overlays.size() > 1)
		{
			InputOverlay overlay = overlays.removeFirst();
			overlay.dispose();
			overlay.setDisplay(null);
			removeMouseListener(overlay);
			removeMouseMotionListener(overlay);
			removeMouseWheelListener(overlay);
			removeKeyListener(overlay);
		}
		
		setAnimatedCursor(null);
	}
	
	public void completeOverlay(InputOverlay overlay)
	{
		if (!overlay.equals(overlays.getFirst()))
			throw new IllegalArgumentException();
		
		overlays.removeFirst();
		overlay.dispose();
		overlay.setDisplay(null);
		setAnimatedCursor(null);
		removeMouseListener(overlay);
		removeMouseMotionListener(overlay);
		removeMouseWheelListener(overlay);
		removeKeyListener(overlay);
		
		if (!overlays.isEmpty())
		{
			InputOverlay oldOverlay = overlays.getFirst();
			oldOverlay.init();
			addMouseListener(oldOverlay);
			addMouseMotionListener(oldOverlay);
			addMouseWheelListener(oldOverlay);
			addKeyListener(oldOverlay);
		}
	}
	
	public void pushOverlay(InputOverlay overlay)
	{
		if (!overlays.isEmpty())
		{
			InputOverlay oldOverlay = overlays.getFirst();
			removeMouseListener(oldOverlay);
			removeMouseMotionListener(oldOverlay);
			removeMouseWheelListener(oldOverlay);
			removeKeyListener(oldOverlay);
		}
		
		overlays.addFirst(overlay);
		overlay.setDisplay(this);
		overlay.init();
		addMouseListener(overlay);
		addMouseMotionListener(overlay);
		addMouseWheelListener(overlay);
		addKeyListener(overlay);
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
	
	/**
	 * Gets the current size of grid tiles considering the TileSet's
	 * standard tile size and the current zoom level.
	 */
	public int getTileSize()
	{
		return tileSize;
	}
	
	/**
	 * Translates a pixel Point on the panel to a grid Position on the map.
	 */
	public Position getPosition(Point point)
	{
		return new Position(point.x / tileSize, point.y / tileSize);
	}
	
	/**
	 * Translates a pair of pixel co-ordinates on the panel
	 * to a grid Position on the map.
	 */
	public Position getPosition(int absX, int absY)
	{
		return new Position(absX / tileSize, absY / tileSize);
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
	 * Gets the region of positions currently visible on this display.
	 */
	public Region getVisibleRegion()
	{
		return getRegion(getVisibleRect());
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
		g.drawImage(
			sprite.getImage(),
			pos.x * tileSize + sprite.getXOffset(),
			pos.y * tileSize + sprite.getYOffset(),
			null
		);
	}
	
	public void draw(Graphics g, Sprite sprite, Point point, Player owner)
	{
		g.drawImage(
			owner == null
				? sprite.getImage()
				: sprite.getImage(owner.getColorHue()),
			point.x + sprite.getXOffset(),
			point.y + sprite.getYOffset(),
			null
		);
	}
	
	/**
	 * Paints the visible rect of the map, including terrain, units and
	 * overlays.
	 */
	public void paintComponent(Graphics g)
	{
		final Rectangle rect = getVisibleRect();
		final Region region = getRegion(rect);
		
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
			g.setColor(getBackground());
			fill(g, rect);
		}
		
		/*
		 * Draw grid
		 */
		if (showGrid)
		{
			g.setColor(Color.BLACK);
			int w = getWidth();
			int h = getHeight();
			
			for (int x = region.x; x < region.getMaxX(); ++x)
				g.drawLine(x * tileSize, 0, x * tileSize, h);
			
			for (int y = region.y; y < region.getMaxY(); ++y)
				g.drawLine(0, y * tileSize, w, y * tileSize);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverTerrian(g, rect);
		
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
			overlays.getFirst().paintOverUnits(g, rect);
	}
	
	/**
	 * Draws terrain (surface or cost map) depending on options using
	 * Graphics g in the visible rect.
	 */
	private void drawTerrain(Graphics g, Rectangle rect)
	{
		synchronized (cacheLock)
		{
			Region region = getVisibleRegion();
			region = map.getBounds().getIntersection(region);
			List<Position> dirtySpots = dirty.findAll(trueFilter, region);
			
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
			draw(g, tiles.getTile(map.getTileCode(pos)), pos);
			
			if (map.hasMinePlatform(pos))
			{
				draw(g, sprites.getSprite("aCommonMine/platform"), pos);
			}
			else if (map.hasGeyser(pos))
			{
				draw(g, sprites.getSprite("aGeyser/geyser"), pos);
			}
			
			dirty.set(pos, false);
		}
	}
	
	/**
	 * Gets the sprite for the given Unit in its current state and renders it
	 * along with any overlays - i.e. the UnitLayer state.
	 */
	private void drawUnit(Graphics g, Unit unit)
	{
		if (!unit.hasAnimationSequence())
		{
			unit.setAnimationSequence(sprites);
		}
		
		List<Sprite> seq = unit.getAnimationSequence();
		Sprite sprite = seq.get(unit.getAnimationFrame() % seq.size());
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
				draw(g, sprites.getSprite("aStructureStatus/idle"), pos);
			}
			else if (unit.isDisabled())
			{
				List<Sprite> seq = sprites.getSequence("aStructureStatus/disabled");
				draw(g, seq.get(Utils.getTimeBasedIndex(100, seq.size())), pos);
			}
			else if (unit.isStructure())
			{
				draw(g, sprites.getSprite("aStructureStatus/active"), pos);
			}
		}
	}
}
