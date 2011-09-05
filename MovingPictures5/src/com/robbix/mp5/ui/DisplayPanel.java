package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.robbix.mp5.Player;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.TileSet;
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
	
	public void showFrameNumber(int frameNumber)
	{
		if (titleBar != null)
			titleBar.showFrameNumber(frameNumber);
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
	
	/**
	 * Gets the region of positions currently visible on this display.
	 */
	public Region getVisibleRegion()
	{
		Rectangle rect = getVisibleRect();
		int tileSize = map.getTileSize();
		
		int minX = (int) Math.floor(rect.x / (double) tileSize);
		int minY = (int) Math.floor(rect.y / (double) tileSize);
		int maxX = (int) Math.ceil((rect.x + rect.width) / (double) tileSize);
		int maxY = (int) Math.ceil((rect.y + rect.height) / (double) tileSize);
		
		maxX = Math.min(maxX, map.getWidth()  - 1);
		maxY = Math.min(maxY, map.getHeight() - 1);
		
		return new Region(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}
	
	/**
	 * Paints the visible rect of the map, including terrain, units and
	 * overlays.
	 */
	public void paintComponent(Graphics g)
	{
		final int tileSize = map.getTileSize();
		final Rectangle rect = getVisibleRect();
		final Region region = getVisibleRegion();
		
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
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
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
			int absX = deposit.getPosition().x * tileSize;
			int absY = deposit.getPosition().y * tileSize;
			
			if (!rect.contains(absX + 16, absY + 16))
				continue;
			
			Sprite sprite = deposit.isSurveyedBy(currentPlayer)
				? sprites.getSprite(deposit)
				: sprites.getUnknownDepositSprite();
			
			g.drawImage(sprite.getImage(),
				absX + sprite.getXOffset(),
				absY + sprite.getYOffset(),
				null
			);
		}
		
		/*
		 * Draw night overlay
		 */
		if (night)
		{
			g.setColor(new Color(0, 0, 0, 127));
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverUnits(g, rect);
	}
	
	private void drawTerrain(Graphics g, Rectangle rect)
	{
		final int tileSize = map.getTileSize();
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
						cg.fillRect(
							x * tileSize,
							y * tileSize,
							tileSize,
							tileSize
						);
						
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
						cg.fillRect(
							x * tileSize,
							y * tileSize,
							tileSize,
							tileSize
						);
						continue;
					}
					
					Image img = tiles.getTile(tileCode);
					
					if (img == null)
					{
						cg.setColor(Color.YELLOW);
						cg.fillRect(
							x * tileSize,
							y * tileSize,
							tileSize,
							tileSize
						);
						continue;
					}
					
					cg.drawImage(img, x * tileSize, y * tileSize, null);
					
					if (map.hasMinePlatform(pos))
					{
						Sprite platformSprite =
							sprites.getSequence("aCommonMine/platform").get(0);
						
						cg.drawImage(
							platformSprite.getImage(),
							x * tileSize + platformSprite.getXOffset(),
							y * tileSize + platformSprite.getYOffset(),
							null
						);
					}
					else if (map.hasGeyser(pos))
					{
						Sprite geyserSprite =
							sprites.getSequence("aGeyser/geyser").get(0);
						
						cg.drawImage(
							geyserSprite.getImage(),
							x * tileSize + geyserSprite.getXOffset(),
							y * tileSize + geyserSprite.getYOffset(),
							null
						);
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
		final Sprite sprite = sprites.getSprite(unit);
		
		final int tileSize = map.getTileSize();
		
		int posX = unit.getX() * tileSize;
		int posY = unit.getY() * tileSize;
		
		if (showUnitLayerState && !unit.isTurret())
		{
			g.setColor(Utils.getTranslucency(Color.RED, 127));
			
			for (Position pos : unit.getFootprint().iterator(unit.getPosition()))
			{
				g.fillRect(
					pos.x * tileSize,
					pos.y * tileSize,
					tileSize,
					tileSize
				);
			}
			
			g.setColor(Utils.getTranslucency(Color.BLUE, 127));
			
			for (Position pos : unit.getMap().getReservations(unit))
			{
				g.fillRect(
					pos.x * tileSize,
					pos.y * tileSize,
					tileSize,
					tileSize
				);
			}
		}
		
		posX += unit.getXOffset();
		posY += unit.getYOffset();
		posX += sprite.getXOffset();
		posY += sprite.getYOffset();
		
		Image img = unit.getOwner() == null
			? sprite.getImage()
			: sprite.getImage(unit.getOwner().getColorHue());
		
		g.drawImage(img, posX, posY, null);
		
		if (unit.hasTurret())
		{
			drawUnit(unit.getTurret(), g);
		}
		else if (unit.getType().isGuardPostType() && currentPlayer.equals(unit.getOwner()))
		{
			if (unit.isIdle())
			{
				List<Sprite> statusSpriteGroup =
					sprites.getSequence("aStructureStatus/idle");
				Sprite statusSprite = statusSpriteGroup.get(0);
				
				g.drawImage(statusSprite.getImage(), unit.getX() * tileSize, unit.getY() * tileSize, null);
			}
			else if (unit.isDisabled())
			{
				List<Sprite> statusSpriteGroup =
					sprites.getSequence("aStructureStatus/disabled");
				int frame = (int)((System.currentTimeMillis() / 100) % statusSpriteGroup.size());
				Sprite statusSprite = statusSpriteGroup.get(frame);
				
				g.drawImage(statusSprite.getImage(), unit.getX() * tileSize, unit.getY() * tileSize, null);
			}
		}
		else if (unit.isStructure() && currentPlayer.equals(unit.getOwner()))
		{
			if (unit.isIdle())
			{
				List<Sprite> statusSpriteGroup =
					sprites.getSequence("aStructureStatus/idle");
				Sprite statusSprite = statusSpriteGroup.get(0);
				
				g.drawImage(statusSprite.getImage(), unit.getX() * tileSize, unit.getY() * tileSize, null);
			}
			else if (unit.isDisabled())
			{
				List<Sprite> statusSpriteGroup =
					sprites.getSequence("aStructureStatus/disabled");
				int frame = (int)((System.currentTimeMillis() / 100) % statusSpriteGroup.size());
				Sprite statusSprite = statusSpriteGroup.get(frame);
				
				g.drawImage(statusSprite.getImage(), unit.getX() * tileSize, unit.getY() * tileSize, null);
			}
			else
			{
				List<Sprite> statusSpriteGroup =
					sprites.getSequence("aStructureStatus/active");
				Sprite statusSprite = statusSpriteGroup.get(0);
				
				g.drawImage(statusSprite.getImage(), unit.getX() * tileSize, unit.getY() * tileSize, null);
			}
		}
	}
}
