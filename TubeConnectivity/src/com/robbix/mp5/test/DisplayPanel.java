package com.robbix.mp5.test;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import com.robbix.mp5.test.TubeConnectivityTest.Tool;

public class DisplayPanel extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	private static final Color TRANS_BLUE   = new Color(0x7f0000ff, true);
	private static final Color TRANS_RED    = new Color(0x7fff0000, true);
	private static final Color TRANS_GREEN  = new Color(0x7f00ff00, true);
	private static final Color TRANS_YELLOW = new Color(0x7fffff00, true);
	private static final Color TRANS_CYAN   = new Color(0x7f00ffff, true);
	private static final Color TRANS_GRAY   = new Color(0x7f7f7f7f, true);
	
	private GameMap map;
	
	public boolean showGrid = true;
	
	public int tileSize = 24;
	
	private Tool tool;
	private Position toolPos;
	
	public DisplayPanel(GameMap map)
	{
		setMap(map);
		setFocusable(true);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
	}
	
	public void setTool(Tool tool)
	{
		this.tool = tool;
	}
	
	public void setToolPosition(Position toolPos)
	{
		this.toolPos = toolPos;
	}
	
	public Position getToolPosition()
	{
		return toolPos;
	}
	
	public void setMap(GameMap map)
	{
		this.map = map;
		setPreferredSize(new Dimension(map.getWidth() * tileSize, map.getHeight() * tileSize));
	}
	
	public void setTileSize(int tileSize)
	{
		this.tileSize = tileSize;
		setPreferredSize(new Dimension(map.getWidth() * tileSize, map.getHeight() * tileSize));
	}
	
	public void paintComponent(Graphics g)
	{
		/*
		 * Draw map
		 */
		for (int x = 0; x < map.w; ++x)
		for (int y = 0; y < map.h; ++y)
		{
			Position p = new Position(x, y);
			
			if (map.isSource(p))
			{
				g.setColor(TRANS_GREEN);
			}
			else if (map.isOccupied(p))
			{
				g.setColor(map.isAlive(p) ? TRANS_BLUE : TRANS_RED);
			}
			else if (map.isTube(p))
			{
				g.setColor(TRANS_GRAY);
			}
			else
			{
				g.setColor(Color.WHITE);
			}
			
			g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
		}

		/*
		 * Draw grid
		 */
		if (showGrid)
		{
			g.setColor(Color.BLACK);
			
			for (int x = 1; x < map.w; ++x)
				g.drawLine(x * tileSize, 0, x * tileSize, getHeight());
			
			for (int y = 1; y < map.h; ++y)
				g.drawLine(0, y * tileSize, getWidth(), y * tileSize);
		}
		
		/*
		 * Draw tool overlay
		 */
		if (tool != null && toolPos != null)
		{
			switch (tool)
			{
			case TUBE:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				break;
				
			case SOURCE:
				g.setColor(TRANS_CYAN);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				g.setColor(Color.MAGENTA);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				break;
				
			case COMMAND_CENTER:
				g.setColor(TRANS_CYAN);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 2);
				g.setColor(Color.MAGENTA);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 2);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 2) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 3) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize * 2);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize * 2);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 0) * tileSize, (toolPos.y + 2) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE2:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 2, tileSize * 2);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 2, tileSize * 2);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 2) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 2) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE3:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 2);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 2);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 2) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 3) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE4:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 3);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 3, tileSize * 3);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 3) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 3) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE5:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 4, tileSize * 3);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 4, tileSize * 3);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 2) * tileSize, (toolPos.y + 3) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 4) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE6:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 5, tileSize * 4);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize * 5, tileSize * 4);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 2) * tileSize, (toolPos.y + 4) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 5) * tileSize, (toolPos.y + 2) * tileSize, tileSize, tileSize);
				break;
				
			case STRUCTURE7:
				g.setColor(TRANS_YELLOW);
				g.fillRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				g.setColor(Color.BLUE);
				g.drawRect(toolPos.x * tileSize, toolPos.y * tileSize, tileSize, tileSize);
				g.setColor(TRANS_GRAY);
				g.fillRect((toolPos.x + 1) * tileSize, (toolPos.y + 0) * tileSize, tileSize, tileSize);
				g.fillRect((toolPos.x + 0) * tileSize, (toolPos.y + 1) * tileSize, tileSize, tileSize);
				break;
			}
		}
	}
}
