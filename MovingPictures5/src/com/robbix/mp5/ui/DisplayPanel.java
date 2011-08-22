package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.robbix.mp5.Player;
import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Position;
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
	
	private Player currentPlayer;
	
	private LinkedList<InputOverlay> overlays;
	
	private List<AmbientAnimation> animations = new LinkedList<AmbientAnimation>();
	
	private List<String> options = new ArrayList<String>(6){{
		add("Grid");
		add("Background");
		add("Unit Layer State");
		add("Terrain Cost Map");
		add("Terrain Cost Values");
		add("Cost Values as Factors");
		add("Night");
	}};
	
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
			throw new NullPointerException();
		
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
			repaint();
		}
		else if (name.equals("Terrain Cost Values"))
		{
			showTerrainCostValues = isSet;
			cachedCostMapImage = null;
			repaint();
		}
		else if (name.equals("Cost Values as Factors"))
		{
			showCostValuesAsFactors = isSet;
			cachedCostMapImage = null;
			repaint();
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
		repaint();
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
		
		repaint();
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
		
		repaint();
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
	 * Paints the visible region of the map, including terrain, units and
	 * overlays.
	 */
	public void paintComponent(Graphics g)
	{
		final int tileSize = map.getTileSize();
		
		/*
		 * Draw terrian.
		 * 
		 * Draw cached terrian/costMap image unless changes have been made.
		 */
		if (showBackground)
		{
			drawTerrain(g);
		}
		else
		{
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		/*
		 * Draw grid
		 */
		if (showGrid)
		{
			g.setColor(Utils.getBlackWhiteComplement(getBackground()));
			int w = getWidth();
			int h = getHeight();
			
			for (int x = 1; x < w; ++x)
				g.drawLine(x * tileSize, 0, x * tileSize, h);
			
			for (int y = 1; y < h; ++y)
				g.drawLine(0, y * tileSize, w, y * tileSize);
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverTerrian(g);
		
		/*
		 * Draw units.
		 * 
		 * Units are returned by UnitLayer iterator already in z-order.
		 */
		for (Unit unit : map.getUnitIterator(true))
			drawUnit(unit, g);
		
		/*
		 * Draw ambient animations
		 */
		synchronized (animations)
		{
			Iterator<AmbientAnimation> animationItr = animations.iterator();
			
			while (animationItr.hasNext())
			{
				AmbientAnimation animation = animationItr.next();
				
				if (getVisibleRect().intersects(animation.getBounds()))
				{
					animation.paint(g);
				}
			}
		}
		
		/*
		 * Draw resource deposits.
		 */
		for (ResourceDeposit deposit : map.getResourceDeposits())
		{
			Sprite sprite = sprites.getSprite(deposit);
			
			g.drawImage(sprite.getImage(),
				deposit.getPosition().x * tileSize + sprite.getXOffset(),
				deposit.getPosition().y * tileSize + sprite.getYOffset(),
				null
			);
		}
		
		/*
		 * Draw night overlay
		 */
		if (night)
		{
			g.setColor(new Color(0, 0, 0, 127));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		/*
		 * Draw input overlay
		 */
		if (! overlays.isEmpty())
			overlays.getFirst().paintOverUnits(g);
	}
	
	private void drawTerrain(Graphics g)
	{
		final int tileSize = map.getTileSize();
		
		if (showTerrainCostMap)
		{
			synchronized (cacheLock)
			{
				if (cachedCostMapImage == null)
				{
					cachedCostMapImage = new BufferedImage(
						map.getWidth()  * tileSize,
						map.getHeight() * tileSize,
						BufferedImage.TYPE_INT_ARGB
					);
					Graphics cg = cachedCostMapImage.getGraphics();
					
					CostMap terrainCost = map.getTerrainCostMap();
					
					cg.setFont(Font.decode("Arial-9"));
					
					for (int y = 0; y < terrainCost.h; ++y)
					for (int x = 0; x < terrainCost.w; ++x)
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
				
				for (int y = 0; y < map.getHeight(); ++y)
				for (int x = 0; x < map.getWidth();  ++x)
				{
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
					
					if (map.hasMinePlatform(new Position(x, y)))
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