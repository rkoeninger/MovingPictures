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
	
	private int tileSize = 32;
	
	private BufferedImage cachedTerrain = null;
	private BufferedImage cachedCostMapImage = null;
	private Object cacheLock = new Integer(0);
	
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
		this.sprites = sprites;
		this.tiles = tiles;
		this.cursors = cursors;
		this.overlays = new LinkedList<InputOverlay>();
		setBackground(new Color(127, 127, 255));
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setPreferredSize(new Dimension(map.getWidth()  * map.getTileSize(),
		                               map.getHeight() * map.getTileSize()));
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
			cachedCostMapImage = null;
			refresh();
		}
		else if (name.equals("Cost Values as Factors"))
		{
			showCostValuesAsFactors = isSet;
			cachedCostMapImage = null;
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
		cachedCostMapImage = null;
		cachedTerrain = null;
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
	}
	
	public void completeOverlay(InputOverlay overlay)
	{
		if (!overlay.equals(overlays.getFirst()))
			throw new IllegalArgumentException();
		
		overlays.removeFirst();
		overlay.dispose();
		overlay.setDisplay(null);
		removeMouseListener(overlay);
		removeMouseMotionListener(overlay);
		removeMouseWheelListener(overlay);
		removeKeyListener(overlay);
		
		if (!overlays.isEmpty())
		{
			InputOverlay oldOverlay = overlays.getFirst();
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
		
		synchronized (cacheLock)
		{
			cachedCostMapImage = null;
		}
		
		refresh();
	}
	
	public boolean getShowTerrainCostValues()
	{
		return showTerrainCostValues;
	}
	
	public void setShowCostValuesAsFactors(boolean showCostValuesAsFactors)
	{
		this.showCostValuesAsFactors = showCostValuesAsFactors;
		
		synchronized (cacheLock)
		{
			cachedCostMapImage = null;
		}
		
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
	
	public int getTileSize()
	{
		return tileSize;
	}
	
	public Position getPosition(Point point)
	{
		return new Position(point.x / tileSize, point.y / tileSize);
	}
	
	public Position getPosition(int absX, int absY)
	{
		return new Position(absX / tileSize, absY / tileSize);
	}
	
	public Point getPoint(Position pos)
	{
		return new Point(pos.x * tileSize, pos.y * tileSize);
	}
	
	public Region getRegion(Rectangle rect)
	{
		int minX = (int) Math.floor(rect.x / (double) tileSize);
		int minY = (int) Math.floor(rect.y / (double) tileSize);
		int maxX = (int) Math.ceil((rect.x + rect.width) / (double) tileSize);
		int maxY = (int) Math.ceil((rect.y + rect.height) / (double) tileSize);
		
		return new Region(minX, minY, maxX - minX, maxY - minY);
	}
	
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
			if (region.intersects(unit.getFootprint().getInnerRegion()))
				drawUnit(unit, g);
		
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
			
			if (!rect.contains(resPoint.x + 16, resPoint.y + 16))
				continue;
			
			Sprite sprite = deposit.isSurveyedBy(currentPlayer)
				? sprites.getSprite(deposit)
				: sprites.getUnknownDepositSprite();
			
			resPoint.x += sprite.getXOffset();
			resPoint.y += sprite.getYOffset();
			
			draw(g, sprite.getImage(), resPoint);
		}
		
		/*
		 * Draw night overlay
		 */
		if (night)
		{
			g.setColor(new Color(0, 0, 0, 127));
			fill(g, rect);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverUnits(g, rect);
	}
	
	private void drawTerrain(Graphics g, Rectangle rect)
	{
		final Region region = getVisibleRegion();
		
		if (showTerrainCostMap)
		{
			synchronized (cacheLock)
			{
				if (cachedCostMapImage == null)
				{
					cachedCostMapImage = new BufferedImage(
						rect.width,
						rect.height,
						BufferedImage.TYPE_INT_ARGB
					);
					Graphics cg = cachedCostMapImage.getGraphics();
					CostMap terrainCost = map.getTerrainCostMap();
					cg.setFont(Font.decode("Arial-9"));
					
					for (int y = region.y; y < region.getMaxY(); ++y)
					for (int x = region.x; x < region.getMaxX(); ++x)
					{
						cg.setColor(Utils.getGrayscale(
							terrainCost.getScaleFactor(x, y)
						));
						fillPosition(cg, x, y);
						
						if (showTerrainCostValues)
						{
							cg.setColor(Utils.getBlackWhiteComplement(
								cg.getColor()
							));
							
							double cost = showCostValuesAsFactors
								? terrainCost.getScaleFactor(x, y)
								: terrainCost.get(x, y);
							
							cg.drawString(
								Double.isInfinite(cost)
									? "Inf"
									: String.format("%.2f", cost),
								x * tileSize + 3,
								y * tileSize + 11
							);
						}
					}
				}
				
				g.drawImage(cachedCostMapImage, 0, 0, null);
			}
		}
		else
		{
			if (cachedTerrain == null)
			{
				cachedTerrain = new BufferedImage(
					map.getWidth()  * tileSize,
					map.getHeight() * tileSize,
					BufferedImage.TYPE_INT_ARGB
				);
				Graphics cg = cachedTerrain.getGraphics();

				for (int y = region.y; y < region.getMaxY(); ++y)
				for (int x = region.x; x < region.getMaxX(); ++x)
				{
					Position pos = new Position(x, y);
					String tileCode = map.getTileCode(x, y);
					
					if (tileCode == null)
					{
						cg.setColor(Color.YELLOW);
						fill(cg, pos);
						continue;
					}
					
					Image img = tiles.getTile(tileCode);
					
					if (img == null)
					{
						cg.setColor(Color.YELLOW);
						fill(cg, pos);
						continue;
					}
					
					draw(cg, img, pos);
					
					if (map.hasMinePlatform(pos))
					{
						draw(cg, sprites.getSprite("aCommonMine/platform"), pos);
					}
					else if (map.hasGeyser(pos))
					{
						draw(cg, sprites.getSprite("aGeyser/geyser"), pos);
					}
				}
			}
			
			g.drawImage(cachedTerrain, 0, 0, null);
		}
	}
	
	/**
	 * Gets the sprite for the given Unit in its current state and renders it
	 * along with any overlays - i.e. the UnitLayer state.
	 */
	private void drawUnit(Unit unit, Graphics g)
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
			drawUnit(unit.getTurret(), g);
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
		
		Position pos = unit.getPosition();
		
		if (unit.isGuardPost() || unit.isStructure())
		{
			if (unit.isIdle())
			{
				draw(g, sprites.getSprite("aStructureStatus/idle"), pos);
			}
			else if (unit.isDisabled())
			{
				List<Sprite> seq = sprites.getSequence("aStructureStatus/disabled");
				draw(g, seq.get(Utils.getTimeBasedIndex(100, seq.size())), pos);
			}
			else if (unit.isStructure()) // Active light only for structures
			{
				draw(g, sprites.getSprite("aStructureStatus/active"), pos);
			}
		}
	}
}
