import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;

public class MapperTiles extends MouseInputAdapter implements KeyListener
{
	public static TileSet ts;
	private static TiledMap map;
	private static DisplayPanel panel;
	private static JFrame frame;
	private static JViewport viewport;
	
	private static int brushFamily = 2;

	private static boolean fillBucket = false;
	
	private static int brushSize = 1;
	private static boolean roundBrush = false;
	
	private static boolean showGrid = true;
	
	private static boolean doodad = false;
	
	private static int prevMouseButton = -1;
	private static int prevX = -1;
	private static int prevY = -1;
	
	private static int scale = 0;
	private static int space = scale > 0 ? 32 << scale : 32 >> -scale;
	
	private static int maxWindowW = 800;
	private static int maxWindowH = 800;
	
	static void setScale(int s)
	{
		Rectangle visible = panel.getVisibleRect();
		Point center = new Point(visible.x + visible.width / 2, visible.y + visible.height / 2);
		int oldSpace = space;
		
		scale = s;
		space = scale > 0 ? 32 << scale : 32 >> -scale;
		panel.setPreferredSize(new Dimension(map.w * space, map.h * space));
		
		Dimension preferredSize = frame.getPreferredSize();
		frame.setSize(Math.min(maxWindowW, preferredSize.width), Math.min(maxWindowH, preferredSize.height));
		
		visible = panel.getVisibleRect();
		center.x = center.x * space / oldSpace;
		center.y = center.y * space / oldSpace;
		Point viewPos = new Point(center.x - visible.width  / 2,
								  center.y - visible.height / 2);
		
		int xMin = 0;
		int yMin = 0;
		int xMax = panel.getWidth()  - viewport.getWidth()  - 1;
		int yMax = panel.getHeight() - viewport.getHeight() - 1;
		
		if      (viewPos.x < xMin) viewPos.x = xMin;
		else if (viewPos.x > xMax) viewPos.x = xMax;
		
		if      (viewPos.y < yMin) viewPos.y = yMin;
		else if (viewPos.y > yMax) viewPos.y = yMax;
		
		viewport.setViewPosition(viewPos);
		panel.clearCache();
	}
	
	static void scaleUp()
	{
		setScale(scale + 1);
	}
	
	static void scaleDown()
	{
		setScale(scale - 1);
	}
	
	public static void main(String[] args) throws IOException
	{
		try
		{
			Rectangle dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			maxWindowW = (int)dm.getWidth();
			maxWindowH = (int)dm.getHeight();
		}
		catch (Exception e){}
		
		ts = TileSet.load();
		map = new TiledMap(32, 32, ts, 3);
		
		panel = new DisplayPanel();
		panel.map = map;
		panel.setPreferredSize(new Dimension(map.w * space, map.h * space));
		panel.addMouseListener(new MapperTiles());
		panel.addMouseMotionListener(new MapperTiles());
		
		viewport = new JViewport();
		viewport.setView(panel);
		
		frame = new JFrame("Tiles");
		frame.addKeyListener(new MapperTiles());
		frame.add(viewport);
		//frame.setResizable(false);
		
		setScale(0);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9)
		{
			brushSize = e.getKeyChar() - '0';
		}
		else if (e.getKeyCode() == KeyEvent.VK_G)
		{
			showGrid = !showGrid;
		}
		else if (e.getKeyCode() == KeyEvent.VK_D)
		{
			doodad = !doodad;
			
			if (doodad)
				fillBucket = false;
		}
		else if (e.getKeyCode() == KeyEvent.VK_F)
		{
			fillBucket = !fillBucket;
			
			if (fillBucket)
				doodad = false;
		}
		else if (e.getKeyCode() == KeyEvent.VK_S)
		{
			roundBrush = !roundBrush;
		}
		else if (e.getKeyCode() == KeyEvent.VK_MINUS)
		{
			if (scale >= -4)
			{
				scaleDown();
				panel.clearCache();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_EQUALS)
		{
			if (scale <= 1)
			{
				scaleUp();
				panel.clearCache();
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_Q)
		{
			brushFamily = 1;
		}
		else if (e.getKeyCode() == KeyEvent.VK_W)
		{
			brushFamily = 2;
		}
		else if (e.getKeyCode() == KeyEvent.VK_E)
		{
			brushFamily = 3;
		}
		else if (e.getKeyCode() == KeyEvent.VK_F5)
		{
			panel.clearCache();
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			Point p = viewport.getViewPosition();
			p.y = Math.max(0, p.y - space);
			viewport.setViewPosition(p);
			panel.clearCache();
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			Point p = viewport.getViewPosition();
			p.y = Math.min(panel.getHeight() - viewport.getHeight(), p.y + space);
			viewport.setViewPosition(p);
			panel.clearCache();
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			Point p = viewport.getViewPosition();
			p.x = Math.max(0, p.x - space);
			viewport.setViewPosition(p);
			panel.clearCache();
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			Point p = viewport.getViewPosition();
			p.x = Math.min(panel.getWidth() - viewport.getWidth(), p.x + space);
			viewport.setViewPosition(p);
			panel.clearCache();
		}
		
		panel.repaint();
	}
	
	public void keyReleased(KeyEvent e)
	{
	}
	
	public void keyTyped(KeyEvent e)
	{
	}
	
	public void mousePressed(MouseEvent e)
	{
		int x = e.getX() / space;
		int y = e.getY() / space;
		
		if (!map.contains(x, y))
			return;
		
		if (fillBucket && e.getButton() == MouseEvent.BUTTON1)
		{
			panel.repaintRegion(
			fillTiles(map, x, y, brushFamily)
			)
			;
			//panel.clearCache();
			
			return;
		}
		
		if (e.isAltDown())
		{		
			Tile current = map.getTile(x, y);
			map.setTile(x, y, ts.getTransitionTile(
				current.familyNE,
				current.familyNW,
				current.familySW,
				current.familySE
			));
			panel.repaintRegion(new Rectangle(x, y, 1, 1));
			//panel.clearCache();
			return;
		}

		if (e.getButton() == MouseEvent.BUTTON1 && doodad)
		{
			TileGroup tg = null;
			
			switch (brushSize)
			{
			case 1:
				switch (brushFamily)
				{
				case 1:
					tg = ts.getGroup("rock"); break;
				case 2:
					tg = ts.getGroup("whirlpool"); break;
				case 3:
					tg = ts.getGroup("tree"); break;
				}
				break;
			case 2:
				switch (brushFamily)
				{
				case 1:
					tg = ts.getGroup("crater"); break;
				case 2:
					tg = ts.getGroup("iceberg"); break;
				}
				break;
			case 3:
				switch (brushFamily)
				{
				case 1:
					tg = ts.getGroup("mountains"); break;
				}
				break;
			}
			
			Rectangle rc = getRect(x, y, brushSize);
			
			if (tg != null)
				panel.repaintRegion(
				placeGroup(map, rc.x, rc.y, tg)
				)
				;
			//panel.clearCache();
			return;
		}
		
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			panel.repaintRegion(
			placeTile(map, x, y, brushFamily, brushSize)
			)
			;
			//panel.clearCache();
		}
		
		prevMouseButton = e.getButton();
		prevX = x;
		prevY = y;
		panel.repaint();
	}
	
	public void mouseReleased(MouseEvent e)
	{
		prevMouseButton = -1;
		prevX = -1;
		prevY = -1;
		panel.repaint();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX() / space;
		int y = e.getY() / space;
		
		if (x == prevX && y == prevY)
			return;
		
		if (fillBucket)
			return;
		
		if (!map.contains(x, y))
			return;
		
		if (prevMouseButton == MouseEvent.BUTTON1)
		{
			panel.repaintRegion(
			placeTile(map, x, y, brushFamily, brushSize)
			)
			;
			//panel.clearCache();
		}

		panel.pointerX = x;
		panel.pointerY = y;
		prevX = x;
		prevY = y;
		panel.repaint();
	}
	
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX() / space;
		int y = e.getY() / space;
		
		if (!map.contains(x, y))
			return;
		
		if (x == panel.pointerX && y == panel.pointerY)
			return;
		
		panel.pointerX = x;
		panel.pointerY = y;
		panel.repaint();
	}
	
	public void mouseExited(MouseEvent e)
	{
		panel.pointerX = -2;
		panel.pointerY = -2;
		
		prevX = -1;
		prevY = -1;
		panel.repaint();
	}
	
	@SuppressWarnings("serial")
	private static class DisplayPanel extends JComponent
	{
		public TiledMap map;
		
		public int pointerX = -2, pointerY = -2;
		
		private BufferedImage background = null;
//		private Rectangle repaintRegion = null;
		
		private Integer cacheLock = new Integer(0);
		
		public void clearCache()
		{
			synchronized (cacheLock)
			{
				background = null;
			}
		}
		
		public void repaintRegion(Rectangle repaintRegion)
		{
			clearCache();
//			if (repaintRegion == null)
//				return;
//			
//			synchronized (cacheLock)
//			{
//				this.repaintRegion = repaintRegion;
//			}
		}
		
		public void paintComponent(Graphics g)
		{
			Rectangle visible = getVisibleRect();
			
			int xMin = visible.x / space;
			int yMin = visible.y / space;
			int xMax = Math.min((visible.x + visible.width ) / space + 1, map.w);
			int yMax = Math.min((visible.y + visible.height) / space + 1, map.h);
			
			synchronized (cacheLock)
			{
				if (background == null)
				{
					background = new BufferedImage(visible.width, visible.height, BufferedImage.TYPE_INT_ARGB);
					Graphics bg = background.getGraphics();
					
					for (int x = xMin; x < xMax; ++x)
					for (int y = yMin; y < yMax; ++y)
					{
						bg.drawImage(map.getTile(x, y).getImage(scale),
							(x - xMin) * space,
							(y - yMin) * space,
							null);
					}
					
					bg.dispose();
				}
//				else if (repaintRegion != null)
//				{
//					Graphics bg = background.getGraphics();
//					
//					int rxMin = Math.max(repaintRegion.x, 0);
//					int ryMin = Math.max(repaintRegion.y, 0);
//					int rxMax = Math.min((repaintRegion.x + repaintRegion.width ) + 1, map.w);
//					int ryMax = Math.min((repaintRegion.y + repaintRegion.height) + 1, map.h);
//					
//					for (int x = rxMin; x < rxMax; ++x)
//					for (int y = ryMin; y < ryMax; ++y)
//					{
//						bg.drawImage(map.getTile(x, y).getImage(scale), x * space, y * space, null);
//					}
//					
//					repaintRegion = null;
//				}
				
				g.drawImage(background, visible.x, visible.y, null);
			}
			
			if (showGrid && scale >= -2)
			{
				g.setColor(Color.BLACK);
				
				for (int vert = xMin + 1; vert <= xMax; ++vert)
					g.drawLine(vert * space, 0, vert * space, map.h * space);
				
				for (int horz = yMin + 1; horz <= yMax; ++horz)
					g.drawLine(0, horz * space, map.w * space, horz * space);
			}
			
			if (map.contains(pointerX, pointerY) && scale >= -3)
			{
				if (fillBucket)
				{
					g.setColor(new Color(255, 255, 0, 127));
					g.fillRect(pointerX * space, pointerY * space, space, space);
				}
				else
				{
					g.setColor(Color.YELLOW);
					Rectangle r = getRect(pointerX, pointerY, brushSize);
					
					if (doodad)
					{
						g.drawOval(r.x * space, r.y * space, r.width * space, r.height * space);
					}
					else
					{
						g.drawRect(r.x * space, r.y * space, r.width * space, r.height * space);
					}
				}
			}
		}
	}
	
	private static Rectangle placeGroup(TiledMap map, int x, int y, TileGroup group)
	{
		Rectangle r = group.getRectangle();
		r = new Rectangle(r.x + x, r.y + y, r.width, r.height);
		
		RectangleBuilder rb = new RectangleBuilder();
		
		for (int row = y; row < y + r.height; ++row)
		for (int col = x; col < x + r.width; ++col)
		{
			Point p = new Point(col, row);
			
			if (map.contains(p))
			{
				rb.add(map.setTile(p, group.getTile(col - x, row - y)));
			}
		}
		
		Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
		
		r.x -= 1;
		r.y -= 1;
		r.width += 2;
		r.height += 2;
		
		int row, col;
		
		// Top, left-to-right
		row = r.y;
		for (col = r.x; col < r.x + r.width - 1; ++col)
		{
			processTransition(map, new Point(col, row), neighbors);
			rb.add(new Point(col, row));
		}

		// Right, top-to-bottom
		col = r.x + r.width - 1;
		for (row = r.y; row < r.y + r.height - 1; ++row)
		{
			processTransition(map, new Point(col, row), neighbors);
			rb.add(new Point(col, row));
		}
		
		// Bottom, right-to-left
		row = r.y + r.height - 1;
		for (col = r.x + r.width - 1; col > r.x; --col)
		{
			processTransition(map, new Point(col, row), neighbors);
			rb.add(new Point(col, row));
		}
			
		// Left, bottom-to-top
		col = r.x;
		for (row = r.y + r.height - 1; row > r.y; --row)
		{
			processTransition(map, new Point(col, row), neighbors);
			rb.add(new Point(col, row));
		}
		
		for (Point p : neighbors.keySet())
		{
			if (map.contains(p))
			{
				x = p.x;
				y = p.y;
				
				Tile neighbor = neighbors.get(new Point(x, y));
				
				if (neighbor == null)
					continue;
				
				int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);
				
//				if (neighbor.familyNE == neighborFamilies[0]
//				 && neighbor.familyNW == neighborFamilies[1]
//				 && neighbor.familySW == neighborFamilies[2]
//				 && neighbor.familySE == neighborFamilies[3])
//				{
//					rb.add(map.setTile(p, neighbor));
//				}
//				else
//				{
					rb.add(map.setTile(p, ts.getTransitionTile(
						neighborFamilies[0],
						neighborFamilies[1],
						neighborFamilies[2],
						neighborFamilies[3]
					)));
//				}
			}
		}
		
		map.validate();
		
		return rb.r;
	}
	
	private static Rectangle fillTiles(TiledMap map, int x, int y, int family)
	{
		Tile startTile = map.getTile(x, y);
		
		if (startTile.isTransition())
		{
			return placeTile(map, x, y, family, 1);
		}
		
		if (family == startTile.getFamily())
			return new Rectangle(0, 0, 0, 0);
		
		Map<Point, Tile> neighbors = spread(map, x, y, family);
		
		RectangleBuilder rb = new RectangleBuilder();
		
		for (Point p : neighbors.keySet())
		{
			if (map.contains(p))
			{
				x = p.x;
				y = p.y;
				
				Tile neighbor = neighbors.get(new Point(x, y));
				
				if (neighbor == null)
					continue;
				
				int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);

				rb.add(p);
				
				map.setTile(x, y, ts.getTransitionTile(
					neighborFamilies[0],
					neighborFamilies[1],
					neighborFamilies[2],
					neighborFamilies[3]
				));
			}
		}
		
		map.validate();
		
		return rb.r;
	}
	
	private static Map<Point, Tile> spread(TiledMap map, int x, int y, int family)
	{
		Tile startTile = map.getTile(x, y);
		
		int replaceFamily = startTile.getFamily();
				
		Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
		
		Set<Point> closed = new HashSet<Point>();
		Set<Point> open = new HashSet<Point>();
		open.add(new Point(x, y));
		map.setTile(x, y, ts.getPlainTile(family));
		
		while (!open.isEmpty())
		{
			Point p = open.iterator().next();
			open.remove(p);
			closed.add(p);
			
			for (Direction d : Direction.dirs)
			{
				Point adj = new Point(p.x + d.dx, p.y + d.dy);
				
				if (!map.contains(adj))
					continue;
				
				if (!closed.add(adj))
					continue;
				
				Tile adjTile = map.getTile(adj);
				
				if (adjTile.isTransition())
				{
					neighbors.put(adj, adjTile);
					map.setTile(adj, Tile.NULL);
				}
				else if (adjTile.getFamily() == replaceFamily)
				{
					open.add(adj);
					map.setTile(adj, ts.getPlainTile(family));
				}
			}
		}
		
		return neighbors;
	}
	
//	private static Rectangle placeTileIgnoreGroups(TiledMap map, Rectangle area, int family)
//	{
//		Rectangle r = area;
//		
//		RectangleBuilder rb = new RectangleBuilder();
//		
//		Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
//		
//		for (int row = r.y; row < r.y + r.height; ++row)
//		for (int col = r.x; col < r.x + r.width; ++col)
//		{
//			Point p = new Point(col, row);
//			
//			if (map.contains(p))
//			{
//				map.setTile(p, ts.getPlainTile(family));
//				rb.add(p);
//			}
//		}
//		
//		r.x -= 1;
//		r.y -= 1;
//		r.width += 2;
//		r.height += 2;
//		
//		int row, col;
//		
//		// Top, left-to-right
//		row = r.y;
//		for (col = r.x; col < r.x + r.width - 1; ++col)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//	
//		// Right, top-to-bottom
//		col = r.x + r.width - 1;
//		for (row = r.y; row < r.y + r.height - 1; ++row)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//		
//		// Bottom, right-to-left
//		row = r.y + r.height - 1;
//		for (col = r.x + r.width - 1; col > r.x; --col)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//			
//		// Left, bottom-to-top
//		col = r.x;
//		for (row = r.y + r.height - 1; row > r.y; --row)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//		
//		for (Point p : neighbors.keySet())
//		{
//			if (map.contains(p))
//			{
//				int x = p.x;
//				int y = p.y;
//				
//				Tile neighbor = neighbors.get(p);
//				
//				if (neighbor == null)
//					continue;
//				
//				int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);
//	
//				if (neighbor.familyNE == neighborFamilies[0]
//				 && neighbor.familyNW == neighborFamilies[1]
//				 && neighbor.familySW == neighborFamilies[2]
//				 && neighbor.familySE == neighborFamilies[3]
//				 && neighbor.isGroupMember())
//				{
//					map.setTile(p, neighbor);
//				}
//				else
//				{
//					map.setTile(p, ts.getTransitionTile(
//						neighborFamilies[0],
//						neighborFamilies[1],
//						neighborFamilies[2],
//						neighborFamilies[3]
//					));
//				}
//			}
//		}
//		
//		map.validate();
//		
//		return rb.r;
//	}
	
	private static Rectangle placeTile(TiledMap map, int x, int y, int family, int brushSize)
	{
		if (brushSize == 1)
		{
			if (!map.contains(x, y))
				return new Rectangle();
			
			RectangleBuilder rb = new RectangleBuilder();
			
			rb.add(new Point(x, y));
			
			map.setTile(x, y, ts.getPlainTile(family));
			
			List<Point> neighboringPositions = new ArrayList<Point>(8);
			if (map.contains(x+1, y))   neighboringPositions.add(new Point(x+1, y));
			if (map.contains(x-1, y))   neighboringPositions.add(new Point(x-1, y));
			if (map.contains(x,   y+1)) neighboringPositions.add(new Point(x,   y+1));
			if (map.contains(x,   y-1)) neighboringPositions.add(new Point(x,   y-1));
			if (map.contains(x+1, y+1)) neighboringPositions.add(new Point(x+1, y+1));
			if (map.contains(x+1, y-1)) neighboringPositions.add(new Point(x+1, y-1));
			if (map.contains(x-1, y+1)) neighboringPositions.add(new Point(x-1, y+1));
			if (map.contains(x-1, y-1)) neighboringPositions.add(new Point(x-1, y-1));
			
			Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
			
			for (Point np : neighboringPositions)
			{
				neighbors.put(np, map.getTile(np));
				rb.add(map.setTile(np, Tile.NULL));
			}
			
			for (Point p : neighbors.keySet())
			{
				if (map.contains(p))
				{
					x = p.x;
					y = p.y;
					
					Tile neighbor = neighbors.get(p);
					
					if (neighbor == null)
						continue;
					
					int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);
					
//					if (neighbor.familyNE == neighborFamilies[0]
//					 && neighbor.familyNW == neighborFamilies[1]
//					 && neighbor.familySW == neighborFamilies[2]
//					 && neighbor.familySE == neighborFamilies[3]
//					 && neighbor.isGroupMember())
//					{
//						map.setTile(p, neighbor);
//						
//						//does nothing
//						TileGroup group = neighbor.getGroup();
//						Rectangle groupArea = group.getRectangle();
//						Point groupPos = neighbor.getGroupPosition();
//						
//						// Right now, TileGroups are limited to being rectangular in shape
//						groupArea = new Rectangle(
//							groupArea.x + p.x + groupPos.x,
//							groupArea.y + p.y + groupPos.y,
//							groupArea.width,
//							groupArea.height
//						);
//						
//						for (int xx = groupArea.x; xx < groupArea.x + groupArea.width;  ++xx)
//						for (int yy = groupArea.y; yy < groupArea.y + groupArea.height; ++yy)
//						{
//							if (map.getTile(xx, yy).isGroupMember())
//								map.setTile(xx, yy, ts.getPlainTile(neighbor.getFamily()));
//						}
//					}
//					else
//					{
						rb.add(map.setTile(p, ts.getTransitionTile(
							neighborFamilies[0],
							neighborFamilies[1],
							neighborFamilies[2],
							neighborFamilies[3]
						)));
//					}
				}
			}
			
			map.validate();
			
			return rb.r;
		}
		else
		{
			Rectangle r = getRect(x, y, brushSize);
			
			RectangleBuilder rb = new RectangleBuilder();
			
			for (int row = r.y; row < r.y + r.height; ++row)
			for (int col = r.x; col < r.x + r.width;  ++col)
			{
				rb.add(placeTile(map, col, row, family, 1));
			}
			
			map.validate();
			
			return extend(rb.r, 1);
		}
	}
	
	/*
	 * Prematurely optimized.
	 */
//	private static Rectangle placeTile(TiledMap map, int x, int y, int family, int brushSize)
//	{
//		Rectangle r = getRect(x, y, brushSize);
//		
//		RectangleBuilder rb = new RectangleBuilder();
//		
//		Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
//		
//		for (int row = r.y; row < r.y + r.height; ++row)
//		for (int col = r.x; col < r.x + r.width; ++col)
//		{
//			Point p = new Point(col, row);
//			
//			if (map.contains(p))
//			{
//				Tile current = map.getTile(p);
//				
//				if (current.isGroupMember())
//				{
//					Point ppp = current.getGroupPosition();
//					Rectangle rrr = current.getGroup().getRectangle();
//					placeTileIgnoreGroups(
//						map,
//						new Rectangle(col - ppp.x, row - ppp.y, rrr.width, rrr.height),
//						current.getFamily()
//					);
//				}
//				
//				map.setTile(p, ts.getPlainTile(family));
//				rb.add(p);
//			}
//		}
//		
//		r.x -= 1;
//		r.y -= 1;
//		r.width += 2;
//		r.height += 2;
//		
//		int row, col;
//		
//		// Top, left-to-right
//		row = r.y;
//		for (col = r.x; col < r.x + r.width - 1; ++col)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//
//		// Right, top-to-bottom
//		col = r.x + r.width - 1;
//		for (row = r.y; row < r.y + r.height - 1; ++row)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//		
//		// Bottom, right-to-left
//		row = r.y + r.height - 1;
//		for (col = r.x + r.width - 1; col > r.x; --col)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//			
//		// Left, bottom-to-top
//		col = r.x;
//		for (row = r.y + r.height - 1; row > r.y; --row)
//		{
//			processTransition(map, new Point(col, row), neighbors);
//			rb.add(new Point(col, row));
//		}
//		
//		for (Point p : neighbors.keySet())
//		{
//			if (map.contains(p))
//			{
//				x = p.x;
//				y = p.y;
//				
//				Tile neighbor = neighbors.get(p);
//				
//				if (neighbor == null)
//					continue;
//				
//				int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);
//
//				if (neighbor.familyNE == neighborFamilies[0]
//				 && neighbor.familyNW == neighborFamilies[1]
//				 && neighbor.familySW == neighborFamilies[2]
//				 && neighbor.familySE == neighborFamilies[3]
//				 && neighbor.isGroupMember())
//				{
//					map.setTile(p, neighbor);
//				}
//				else
//				{
//					map.setTile(p, ts.getTransitionTile(
//						neighborFamilies[0],
//						neighborFamilies[1],
//						neighborFamilies[2],
//						neighborFamilies[3]
//					));
//				}
//			}
//		}
//		
//		map.validate();
//		
//		return rb.r;
//	}
	
	private static Rectangle extend(Rectangle r, int amount)
	{
		return new Rectangle(r.x - amount, r.y - amount, r.width + 2 * amount, r.height + 2 * amount);
	}
	
	private static void processTransition(TiledMap map, Point p, Map<Point, Tile> oldTiles)
	{
		if (map.contains(p))
		{
			Tile oldTile = map.getTile(p);
			map.setTile(p, Tile.NULL);
			oldTiles.put(p, oldTile);
		}
	}
	
	private static Rectangle getRect(int x, int y, int brushSize)
	{
		int yStart = (int)Math.ceil(y - (brushSize / 2.0));
		int yStop  = yStart + brushSize - 1;
		int xStart = (int)Math.ceil(x - (brushSize / 2.0));
		int xStop  = xStart + brushSize - 1;
		
		if (brushSize % 2 == 0)
		{
			yStart += 1;
			yStop  += 1;
			xStart += 1;
			xStop  += 1;
		}
		
		return new Rectangle(xStart, yStart, xStop - xStart + 1, yStop - yStart + 1);
	}
	
	private static int[] getNeighborFamilies(TiledMap map, int x, int y, Tile prevTile)
	{
		int familyNE = prevTile.familyNE;

		if (map.contains(x + 1, y - 1) && map.getTile(x + 1, y - 1).familySW != 0)
		{
			familyNE = map.getTile(x + 1, y - 1).familySW;
		}
		if (map.contains(x + 0, y - 1) && map.getTile(x + 0, y - 1).familySE != 0)
		{
			familyNE = map.getTile(x + 0, y - 1).familySE;
		}
		if (map.contains(x + 1, y + 0) && map.getTile(x + 1, y + 0).familyNW != 0)
		{
			familyNE = map.getTile(x + 1, y + 0).familyNW;
		}
		
		int familyNW = prevTile.familyNW;

		if (map.contains(x - 1, y - 1) && map.getTile(x - 1, y - 1).familySE != 0)
		{
			familyNW = map.getTile(x - 1, y - 1).familySE;
		}
		if (map.contains(x + 0, y - 1) && map.getTile(x + 0, y - 1).familySW != 0)
		{
			familyNW = map.getTile(x + 0, y - 1).familySW;
		}
		if (map.contains(x - 1, y + 0) && map.getTile(x - 1, y + 0).familyNE != 0)
		{
			familyNW = map.getTile(x - 1, y + 0).familyNE;
		}
		
		int familySW = prevTile.familySW;

		if (map.contains(x - 1, y + 1) && map.getTile(x - 1, y + 1).familyNE != 0)
		{
			familySW = map.getTile(x - 1, y + 1).familyNE;
		}
		if (map.contains(x + 0, y + 1) && map.getTile(x + 0, y + 1).familyNW != 0)
		{
			familySW = map.getTile(x + 0, y + 1).familyNW;
		}
		if (map.contains(x - 1, y + 0) && map.getTile(x - 1, y + 0).familySE != 0)
		{
			familySW = map.getTile(x - 1, y + 0).familySE;
		}
		
		int familySE = prevTile.familySE;

		if (map.contains(x + 1, y + 1) && map.getTile(x + 1, y + 1).familyNW != 0)
		{
			familySE = map.getTile(x + 1, y + 1).familyNW;
		}
		if (map.contains(x + 0, y + 1) && map.getTile(x + 0, y + 1).familyNE != 0)
		{
			familySE = map.getTile(x + 0, y + 1).familyNE;
		}
		if (map.contains(x + 1, y + 0) && map.getTile(x + 1, y + 0).familySW != 0)
		{
			familySE = map.getTile(x + 1, y + 0).familySW;
		}
		
		return new int[]{familyNE, familyNW, familySW, familySE};
	}
	
	/* Does only single tiles */
//	private static void placeTile(TiledMap map, int x, int y, int family)
//	{
//		map.setTile(x, y, ts.getPlainTile(family));
//		
//		Map<Point, Tile> neighbors = new HashMap<Point, Tile>();
//		
//		for (Direction d : Direction.dirs)
//		{
//			if (map.contains(x + d.dx, y + d.dy))
//			{
//				neighbors.put(new Point(x + d.dx, y + d.dy), map.getTile(x + d.dx, y + d.dy));
//				map.setTile(x + d.dx, y + d.dy, Tile.NULL);
//			}
//		}
//		
//		int centerX = x;
//		int centerY = y;
//		
//		for (Direction d : Direction.dirs)
//		{
//			if (map.contains(centerX + d.dx, centerY + d.dy))
//			{
//				x = centerX + d.dx;
//				y = centerY + d.dy;
//				
//				Tile neighbor = neighbors.get(new Point(x, y));
//				
//				if (neighbor == null)
//					continue;
//				
//				int[] neighborFamilies = getNeighborFamilies(map, x, y, neighbor);
//
//				map.setTile(x, y, ts.getTransitionTile(
//					neighborFamilies[0],
//					neighborFamilies[1],
//					neighborFamilies[2],
//					neighborFamilies[3]
//				));
//			}
//		}
//	}
}
