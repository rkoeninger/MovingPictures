package com.robbix.mp5.mapper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class MapDisplayPanel extends JComponent
{
	public int tileSize = 32;
	
	public Map map;
	
	public int brushSize = 1;
	public boolean square = true;
	public boolean feathered = false;
	
	// MouseMotionListener doesn't hold button pressed!!! why?!
	private int prevButton = 0;
	
	public void setMap(Map map)
	{
		this.map = map;
		assessSize();
		repaint();
	}
	
	public void setTileSize(int size)
	{
		this.tileSize = size;
		assessSize();
		repaint();
	}
	
	public void assessSize()
	{
		if (map != null)
			setPreferredSize(new Dimension(map.w * tileSize, map.h * tileSize));
	}
	
	public MapDisplayPanel(final Map initMap)
	{
		setMap(initMap);
		
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				prevButton = e.getButton();
				int gridX = e.getX() / tileSize;
				int gridY = e.getY() / tileSize;
				
				if (gridX < 0 || gridX >= map.w || gridY < 0 || gridY >= map.h)
					return;
				
				switch (e.getButton())
				{
					case MouseEvent.BUTTON1: // Left
						
						if (e.isShiftDown())
						{
							if (square)
							{
								blackSquareFlat(gridX, gridY, brushSize);
							}
							else
							{
								blackCircleFlat(gridX, gridY, brushSize);
							}
						}
						else if (square)
						{
							darkenSquareFlat(gridX, gridY, brushSize, 0.9);
						}
						else if (feathered)
						{
							darkenCircleFeathered(gridX, gridY, brushSize, 0.9);
						}
						else
						{
							darkenCircleFlat(gridX, gridY, brushSize, 0.9);
						}
						
						break;
					case MouseEvent.BUTTON3: // Right
						
						if (e.isShiftDown())
						{
							if (square)
							{
								whiteSquareFlat(gridX, gridY, brushSize);
							}
							else
							{
								whiteCircleFlat(gridX, gridY, brushSize);
							}
						}
						else if (square)
						{
							lightenSquareFlat(gridX, gridY, brushSize, 1.1);
						}
						else if (feathered)
						{
							lightenCircleFeathered(gridX, gridY, brushSize, 1.1);
						}
						else
						{
							lightenCircleFlat(gridX, gridY, brushSize, 1.1);
						}
						
						break;
					case MouseEvent.BUTTON2: // Middle
						
						if (e.isShiftDown()) map.putFixture(gridX, gridY, null); // clear fixture
						else                 map.rotateFixture(gridX, gridY, true);
						
						break;
				}
				
				MapDisplayPanel.this.repaint();
			}
			
			public void mouseReleased(MouseEvent e)
			{
				prevButton = 0;
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			int prevX = -10, prevY = -10;
			
			public void mouseDragged(MouseEvent e)
			{
				int gridX = e.getX() / tileSize;
				int gridY = e.getY() / tileSize;

				if (gridX < 0 || gridX >= map.w || gridY < 0 || gridY >= map.h)
					return;
				
				if (gridX != prevX || gridY != prevY)
					switch (prevButton)
					{
						case MouseEvent.BUTTON1: // Left
							
							if (e.isShiftDown())
							{
								if (square)
								{
									blackSquareFlat(gridX, gridY, brushSize);
								}
								else
								{
									blackCircleFlat(gridX, gridY, brushSize);
								}
							}
							else if (square)
							{
								darkenSquareFlat(gridX, gridY, brushSize, 0.9);
							}
							else if (feathered)
							{
								darkenCircleFeathered(gridX, gridY, brushSize, 0.9);
							}
							else
							{
								darkenCircleFlat(gridX, gridY, brushSize, 0.9);
							}
							
							break;
						case MouseEvent.BUTTON3: // Right
							
							if (e.isShiftDown())
							{
								if (square)
								{
									whiteSquareFlat(gridX, gridY, brushSize);
								}
								else
								{
									whiteCircleFlat(gridX, gridY, brushSize);
								}
							}
							else if (square)
							{
								lightenSquareFlat(gridX, gridY, brushSize, 1.1);
							}
							else if (feathered)
							{
								lightenCircleFeathered(gridX, gridY, brushSize, 1.1);
							}
							else
							{
								lightenCircleFlat(gridX, gridY, brushSize, 1.1);
							}
							
							break;
					}
				
				prevX = gridX;
				prevY = gridY;
				
				MapDisplayPanel.this.repaint();
			}
		});
		
		addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int gridX = e.getX() / tileSize;
				int gridY = e.getY() / tileSize;
				
				if (gridX < 0 || gridX >= map.w || gridY < 0 || gridY >= map.h)
					return;
				
				int turns = Math.abs(e.getWheelRotation());
				boolean direction = e.getWheelRotation() < 0;
				
				for (int x = 0; x < turns; ++x)
					map.rotateFixture(gridX, gridY, direction);
				
				MapDisplayPanel.this.repaint();
			}
		});
	}
	
	private Font font = Font.decode("Arial-20");
	
	public void paintComponent(Graphics g)
	{
		if (map == null) return;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		Rectangle visible = getVisibleRect();
		
		int xMin = visible.x / tileSize;
		int xMax = Math.min(map.w - 1, ((visible.x + visible.width) / tileSize) + 1);
		int yMin = visible.y / tileSize;
		int yMax = Math.min(map.h - 1, ((visible.y + visible.height) / tileSize) + 1);
		
		for (int y = yMin; y <= yMax; ++y)
		for (int x = xMin; x <= xMax; ++x)
		{
			int gray = (int) (255 * map.getScaleFactor(x, y));
			g.setColor(new Color(gray, gray, gray));
			g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
			
			Map.Fixture fixture = map.getFixture(x, y);
			
			if (fixture != null)
			{
				switch (fixture)
				{
					case WALL:
						g.setFont(font);
						g.setColor(g.getColor().getRed() < 127
							? Color.WHITE
							: Color.BLACK);
						g.drawString("W", x * tileSize + 7, y * tileSize + 24);
						break;
					case TUBE:
						g.setFont(font);
						g.setColor(g.getColor().getRed() < 127
							? Color.WHITE
							: Color.BLACK);
						g.drawString("T", x * tileSize + 10, y * tileSize + 24);
						break;
				}
			}
		}
		
		for (ResourceMarker resMark : map.getResourceMarkers())
		{
			g.setColor(resMark.type == ResourceMarker.Type.COMMON ? ORANGE : GOLD);
			g.fillRect(resMark.pos.x * tileSize, resMark.pos.y * tileSize, 8, 8);
		}
		
		if (tileSize > 4)
		{
			g.setColor(Color.BLACK);
			for (int v = 1; v < map.w; ++v) g.drawLine(v * tileSize, 0, v * tileSize, map.h * tileSize);
			for (int h = 1; h < map.h; ++h) g.drawLine(0, h * tileSize, map.w * tileSize, h * tileSize);
		}
	}
	
	private static final Color ORANGE = new Color(255, 97, 15);
	private static final Color GOLD = new Color(255, 223, 65);
	
	private double distance(int x1, int y1, int x2, int y2)
	{
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
	
	private void blackSquareFlat(int x0, int y0, int radius)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			map.black(x, y);
		}
	}

	private void whiteSquareFlat(int x0, int y0, int radius)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			map.white(x, y);
		}
	}

	private void blackCircleFlat(int x0, int y0, int radius)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = distance(x, y, x0, y0);
			
			if (Math.round(d) <= radius)
			{
				map.black(x, y);
			}
		}
	}

	private void whiteCircleFlat(int x0, int y0, int radius)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = distance(x, y, x0, y0);
			
			if (Math.round(d) <= radius)
			{
				map.white(x, y);
			}
		}
	}
	
	private void darkenSquareFlat(int x0, int y0, int radius, double darkness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			map.darker(x, y, darkness);
		}
	}
	
	private void lightenSquareFlat(int x0, int y0, int radius, double lightness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			map.lighter(x, y, lightness);
		}
	}
	
	private void darkenCircleFlat(int x0, int y0, int radius, double darkness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = distance(x, y, x0, y0);
			
			if (Math.round(d) <= radius)
			{
				map.darker(x, y, darkness);
			}
		}
	}
	
	private void lightenCircleFlat(int x0, int y0, int radius, double lightness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = Math.round(distance(x, y, x0, y0));
			
			if (d <= radius)
			{
				map.lighter(x, y, lightness);
			}
		}
	}
	
	private void darkenCircleFeathered(int x0, int y0, int radius, double darkness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = Math.round(distance(x, y, x0, y0));
			
			if (d <= radius)
			{
				double darknessDiff = 1 - darkness;
				double distanceFactor = d / (radius + 1);
				double weightedDarkness = 1 - (darknessDiff * (1 - distanceFactor));
				map.darker(x, y, weightedDarkness);
			}
		}
	}
	
	private void lightenCircleFeathered(int x0, int y0, int radius, double lightness)
	{
		radius -= 1;
		
		for (int y = y0 - radius; y <= y0 + radius; ++y)
		for (int x = x0 - radius; x <= x0 + radius; ++x)
		if (map.contains(x, y))
		{
			double d = Math.round(distance(x, y, x0, y0));
			
			if (d <= radius)
			{
				double lightnessDiff = lightness - 1;
				double distanceFactor = d / (radius + 1);
				double weightedLightness = 1 + (lightnessDiff * (1 - distanceFactor));
				map.lighter(x, y, weightedLightness);
			}
		}
	}
}
