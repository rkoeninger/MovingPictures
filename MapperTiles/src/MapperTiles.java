import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

public class MapperTiles extends MouseInputAdapter implements KeyListener
{
	private static TileSet ts;
	private static TiledMap map;
	private static DisplayPanel panel;
	private static JFrame frame;
	
	private static int brushFamily = 2;
	
	private static int prevMouseButton = -1;
	private static int prevX = -1;
	private static int prevY = -1;
	
	private static int scale = 0;
	private static int space = scale > 0 ? 32 << scale : 32 >> -scale;
	
	static void setScale(int s)
	{
		scale = s;
		space = scale > 0 ? 32 << scale : 32 >> -scale;
		panel.setPreferredSize(new Dimension(map.w * space, map.h * space));
		
		frame.pack();
		frame.setSize(Math.min(800, frame.getWidth()), Math.min(800, frame.getHeight()));
		panel.clearCache();
	}

	static void scaleUp()
	{
		setScale(scale+1);
	}

	static void scaleDown()
	{
		setScale(scale-1);
	}
	
	private static boolean fillBucket = false;
	
	private static int brushSize = 1;
	
	private static boolean showGrid = true;
	
	public static void main(String[] args) throws IOException
	{
		ts = TileSet.load();
		map = new TiledMap(24, 24, ts, 3);
		
		panel = new DisplayPanel();
		panel.map = map;
		panel.setPreferredSize(new Dimension(map.w * space, map.h * space));
		panel.addMouseListener(new MapperTiles());
		panel.addMouseMotionListener(new MapperTiles());
		
		frame = new JFrame("Tiles");
		frame.addKeyListener(new MapperTiles());
		frame.add(panel);
		frame.setResizable(false);
		frame.pack();
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
		else if (e.getKeyCode() == KeyEvent.VK_F)
		{
			fillBucket = !fillBucket;
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
			fillTiles(map, x, y, brushFamily);
			panel.clearCache();
			
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
			panel.clearCache();
			return;
		}
		
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			placeTile(map, x, y, brushFamily, brushSize);
			panel.clearCache();
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
			placeTile(map, x, y, brushFamily, brushSize);
			panel.clearCache();
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
		
		private Integer cacheLock = new Integer(0);
		
		public void clearCache()
		{
			synchronized (cacheLock)
			{
				background = null;
			}
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
						bg.drawImage(map.getTile(x, y).getImage(scale), x * space, y * space, null);
					}
				}
				
				g.drawImage(background, 0, 0, null);
			}
			
			if (showGrid && scale >= -2)
			{
				g.setColor(Color.BLACK);
				
				for (int vert = yMin + 1; vert <= yMax; ++vert)
					g.drawLine(vert * space, 0, vert * space, map.h * space);
				
				for (int horz = xMin; horz <= xMax; ++horz)
					g.drawLine(0, horz * space, map.w * space, horz * space);
			}
			
			if (map.contains(pointerX, pointerY))
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
					g.drawRect(r.x * space, r.y * space, r.width * space, r.height * space);
				}
			}
		}
	}
	
	private static void fillTiles(TiledMap map, int x, int y, int family)
	{
		Tile startTile = map.getTile(x, y);
		
		if (startTile.isTransition())
		{
			placeTile(map, x, y, family, 1);
			return;
		}
		
		if (family == startTile.getFamily())
			return;
		
		Map<Point, Tile> neighbors = spread(map, x, y, family);
		
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

				map.setTile(x, y, ts.getTransitionTile(
					neighborFamilies[0],
					neighborFamilies[1],
					neighborFamilies[2],
					neighborFamilies[3]
				));
			}
		}
		
		map.validate();
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
	
	private static void placeTile(TiledMap map, int x, int y, int family, int brushSize)
	{
		Rectangle r = getRect(x, y, brushSize);
		
		for (int row = r.y; row < r.y + r.height; ++row)
		for (int col = r.x; col < r.x + r.width; ++col)
		{
			Point p = new Point(col, row);
			
			if (map.contains(p))
			{
				map.setTile(p, ts.getPlainTile(family));
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
			processTransition(map, new Point(col, row), neighbors);

		// Right, top-to-bottom
		col = r.x + r.width - 1;
		for (row = r.y; row < r.y + r.height - 1; ++row)
			processTransition(map, new Point(col, row), neighbors);
		
		// Bottom, right-to-left
		row = r.y + r.height - 1;
		for (col = r.x + r.width - 1; col > r.x; --col)
			processTransition(map, new Point(col, row), neighbors);
			
		// Left, bottom-to-top
		col = r.x;
		for (row = r.y + r.height - 1; row > r.y; --row)
			processTransition(map, new Point(col, row), neighbors);
		
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

				map.setTile(x, y, ts.getTransitionTile(
					neighborFamilies[0],
					neighborFamilies[1],
					neighborFamilies[2],
					neighborFamilies[3]
				));
			}
		}
		
		map.validate();
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
