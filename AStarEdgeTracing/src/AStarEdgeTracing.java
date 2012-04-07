
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AStarEdgeTracing {

	final static int tileSize = 8;
	
	public static void main(String[] args) throws IOException{
		while (true){main2(args);}
	}
	
	public static void main2(String[] args) throws IOException
	{
		out("\n\n\n");
		
		final int[][] terrain = loadTerrain(new File("terrain.bmp"));
		final int w = terrain.length;
		final int h = terrain[0].length;
		final Point start = randUnoccupied(terrain);
		final Point end = randUnoccupied(terrain);
//		final Point start = new Point(3, 60);
//		final Point end = new Point(34, 28);

		verbose = true;
		
		checkWalls = false;
		long time = System.nanoTime();
		final Map<Point, Cost> openSet = new HashMap<Point, Cost>();
		final Set<Point> closedSet = new HashSet<Point>();
		final Collection<FromTo> nodes = new HashSet<FromTo>();
		final int iterations = getNodes(terrain, openSet, closedSet, start, end, nodes);
		
		out("--- Normal A*");
		out(iterations + " iterations");
		if (iterations < 0){
			out("Path could not be found");
			System.exit(0);
		}
		
		final List<Point> path = getPath(nodes, start, end);
		out(((System.nanoTime() - time) / 1000000.0) + " ms");
		out("Path length: " + path.size());
		
		
		

		/* 
		 * Improvement #2: Wall check
		 */
		checkWalls = true;
		long time2 = System.nanoTime();
		final Map<Point, Cost> openSet2 = new HashMap<Point, Cost>();
		final Set<Point> closedSet2 = new HashSet<Point>();
		final Collection<FromTo> nodes2 = new HashSet<FromTo>();
		final int iterations2 = getNodes(terrain, openSet2, closedSet2, start, end, nodes2);
		
		out("--- A* with wall tracing");
		out(iterations2 + " iterations");
		if (iterations2 < 0){
			out("Path could not be found");
			System.exit(0);
		}
		
		final List<Point> path2 = getPath(nodes2, start, end);
		out(((System.nanoTime() - time2) / 1000000.0) + " ms");
		out("Path length: " + path2.size() + (path2.size() * 9 / 10 > path.size() ? " !!!!!!!!!!!!!!" : ""));
		
		
		
		draw(w * tileSize, h * tileSize, new Draw(){
			public void paint(Graphics g)
			{
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, w * tileSize, h * tileSize);
				
				g.setColor(Color.BLACK);
				for (int x = 0; x < w; ++x)
				for (int y = 0; y < h; ++y)
					if (terrain[x][y] < 0)
						g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
				
				g.setColor(new Color(0, 255, 0, 127));
				for (FromTo ft : nodes)
					g.fillRect(ft.to.x   * tileSize, ft.to.y   * tileSize, tileSize, tileSize);
				g.setColor(new Color(0, 0, 255, 127));
				for (FromTo ft : nodes2)
					g.fillRect(ft.to.x   * tileSize, ft.to.y   * tileSize, tileSize, tileSize);

				g.setColor(new Color(255, 255, 0, 127));
				for (Point p : path)
					g.fillRect(p.x * tileSize, p.y * tileSize, tileSize, tileSize);
				g.setColor(new Color(255, 0, 255, 127));
				for (Point p : path2)
					g.fillRect(p.x * tileSize, p.y * tileSize, tileSize, tileSize);
				
				g.setColor(Color.BLUE);
				g.fillRect(start.x * tileSize, start.y * tileSize, tileSize, tileSize);
				
				g.setColor(Color.YELLOW);
				g.fillRect(end.x * tileSize, end.y * tileSize, tileSize, tileSize);
			}
		});
		
		//System.exit(0);
	}

	static List<Point> getPath(Collection<FromTo> nodes, Point start, Point end)
	{
		List<Point> path = new ArrayList<Point>((int)absDistance(start, end));
		
		path.add(end);
		
		while (! path.get(path.size()-1).equals(start)){
			path.add(find(nodes, path.get(path.size()-1)).from);
		}
		
		Collections.reverse(path);
		return path;
	}
	
	static FromTo find(Collection<FromTo> nodes, Point to)
	{
		for (FromTo ft : nodes)
			if (ft.to.equals(to))
				return ft;
		
		throw new Error();
	}
	
	static void removeTo(Collection<FromTo> nodes, Point to)
	{
		for (FromTo ft : nodes)
			if (ft.to.equals(to))
			{
				nodes.remove(ft);
				return;
			}
	}
	
	static int getNodes(
		int[][] terrain,
		Map<Point, Cost> openSet,
		Set<Point> closedSet,
		Point start,
		Point end,
		Collection<FromTo> nodes)
	{
		openSet.put(start, new Cost(0.0, heurisitc(terrain, start, end)));

		for (int i = 0; !openSet.isEmpty(); ++i)
		{
			/*
			 * TODO:
			 * Future Improvement:
			 * 
			 * replace unordered openSet with a
			 * 
			 * TreeSet<PointAndCost>
			 * 
			 * class PointAndCost
			 * {
			 *     Point p;  Cost c;
			 * }
			 * 
			 * Comparator<PointAndCost> LOWER_F
			 * {
			 *     public int compare(PointAndCost p1, PointAndCost p2)
			 *     {
			 *         return p1.cost.f - p2.cost.f;
			 *     }
			 * }
			 */
			Cost lowestCost = new Cost(Double.POSITIVE_INFINITY, 0);
			Point current = null;
			
			for (Point p : openSet.keySet())
				if (openSet.get(p).f < lowestCost.f)
				{
					lowestCost = openSet.get(p);
					current = p;
				}
			
			if (current.equals(end))
				return i;
			
			openSet.remove(current);
			closedSet.add(current);
			
			/*
			 * Improvement #2: Wall check
			 */
			boolean straightPathOccupied =
				!checkAlt(
					terrain,
					closedSet,
					approxDirection(current, end).apply(current)
				);
			
			for (Direction dir : Direction.dirs)
			{

				/*
				 * Improvement #2: Wall check
				 * Checking walls produces slightly suboptimal results,
				 * but also cuts search time down but up to 50%
				 */
				if (checkWalls)
					if (straightPathOccupied && !hasWallNeighbor(terrain, current, dir))
						continue;
				
				Point neighbor = dir.apply(current);
				
				if (!checkAlt(terrain, closedSet, neighbor))
					continue;
				
				/*
				 * Improvement #1: Compare against old measured path.
				 * If a route to this neighbor pos with a lower g is found,
				 * it replaces the old one.
				 */
				Cost standingCost = openSet.get(neighbor);
				Cost neighborCost =
					new Cost(
						lowestCost.g + (dir.isDiagonal() ? 1.414 : 1), // g
						heurisitc(terrain, neighbor, end)              // h
					);
				
				if (standingCost == null || neighborCost.g < standingCost.g) // #1
				{
					openSet.put(neighbor, neighborCost);
					
					/*
					 * Improvement #0:
					 * Don't forget to remove old node from list if a better
					 * (measured) path to that position is found. When the path
					 * is reconstructed, the searching of the nodes is not in
					 * any particular order, so we get weirdness in the
					 * reconstructed path.
					 * 
					 * Do this if the old is replaced or not, there shouldn't be
					 * crossed paths in the node tree.
					 */
					removeTo(nodes, neighbor);
					
					nodes.add(new FromTo(current, neighbor));
				}
			}
		}
		
		return -1;
	}

	/* 
	 * Improvement #2: Wall check
	 */
	static boolean checkWalls = false;
	
	/*
	 * Improvement # -1 (already implemented):
	 * Separate heuristic function from main of algorithm.
	 */
	static double heurisitc(int[][] terrain, Point start, Point end){
		return absDistance(start, end);
	}
	
	/*
	 * Improvement #1:
	 * Cost class allows us to distinguish between total cost and measured cost
	 */
	static class Cost{
		public final double f, g;
		public Cost(double g, double h){
			this.g = g;
			this.f = g + h;
		}
	}
	
	static boolean hasWallNeighbor(int[][] terrain, Point current, Direction dir)
	{
		Point altA = dir.rotateCW().apply(current);
		Point altB = dir.rotateCCW().apply(current);
		
		return (contains(terrain, altA) && terrain[altA.x][altA.y] < 0)
			|| (contains(terrain, altB) && terrain[altB.x][altB.y] < 0);
	}
	
	static Direction getBestDirection(int[][] terrain, List<Point> path, Point current, Point end)
	{
		Direction straight = approxDirection(current, end);
		Point straightPoint = straight.apply(current);
		
		if (checkAlt(terrain, path, straightPoint))
			return new DirectionAndMeta(straight, false);
		
		Direction altCW = straight;
		Direction altCCW = straight;
		Point altCWPoint;
		Point altCCWPoint;
		
		for (int x = 0; x < 3; ++x)
		{
			altCW = altCW.rotateCW();
			altCWPoint = altCW.apply(current);
			
			if (checkAlt(terrain, path, altCWPoint))
				return new DirectionAndMeta(altCW, true);
			
			altCCW = altCCW.rotateCCW();
			altCCWPoint = altCCW.apply(current);
	
			if (checkAlt(terrain, path, altCCWPoint))
				return new DirectionAndMeta(altCCW, true);
		}
		
		Direction rev = straight.reverse();
		Point revPoint = rev.apply(current);
		
		if (checkAlt(terrain, path, revPoint))
			return new DirectionAndMeta(rev, true);
		
		return null;
	}
	
	static boolean checkAlt(int[][] terrain, Collection<Point> path, Point alt)
	{
		return contains(terrain,alt)
			&& terrain[alt.x][alt.y] >= 0
			&& !path.contains(alt);
	}
	
	static Direction approxDirection(Point start, Point end){
		double angle = atan(abs((end.y - start.y) / (double)(end.x - start.x)));
		return new Direction(
			(angle < (PI * 3.0 / 8.0)) ? (end.x < start.x ? -1 : 1) : 0,
			(angle > (PI       / 8.0)) ? (end.y < start.y ? -1 : 1) : 0
		);
	}
	
	static Point randUnoccupied(int[][] terrain){
		while (true){
			Point p = new Point(rand.nextInt(terrain.length), rand.nextInt(terrain[0].length));
			
			if (terrain[p.x][p.y] >= 0)
				return p;
		}
	}
	
	static class FromTo{
		public final Point from, to;
		public FromTo(Point from, Point to){
			this.from = from;
			this.to = to;
		}
	}
	
	static double absDistance(Point p1, Point p2){
		return sqrt(pow(p1.x-p2.x,2)+pow(p1.y-p2.y,2));
	}
	
	static double absDistance(double dx, double dy){
		return sqrt((dx*dx)+(dy*dy));
	}
	
	static int sqDistance(Point p1, Point p2){
		return (p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y);
	}

	static int sqDistance(int x, int y, Point p2){
		return ((x-p2.x)*(x-p2.x))+((y-p2.y)*(y-p2.y));
	}
	
	static int sqDistance(int dx, int dy){
		return (dx*dx)+(dy*dy);
	}
	
	static boolean contains(int[][] grid, int x, int y){
		return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
	}

	static boolean contains(int[][] grid, Point p){
		return p.x >= 0 && p.x < grid.length && p.y >= 0 && p.y < grid[0].length;
	}
	
	static boolean verbose = false;
	
	static void out(Object o){
		if (verbose) System.out.println(o);
	}
	
	static int[][] loadTerrain(File file) throws IOException{
		BufferedImage img = (BufferedImage) ImageIO.read(file);
		WritableRaster raster = img.getRaster();
		int w = raster.getWidth();
		int h = raster.getHeight();
		int[][] grid = new int[w][h];
		int[] pixel = new int[4];
		
		for (int x = 0; x < w; ++x){
			for (int y = 0; y < h; ++y){
				raster.getPixel(x, y, pixel);
				if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 0)
					grid[x][y] = -1;
			}
		}
		
		return grid;
	}
	
	static Random rand = new Random();
	
	static class PointAndMeta extends Point
	{
		private static final long serialVersionUID = 1L;
		public final boolean alt;
		public PointAndMeta(int x, int y, boolean alt)
		{
			super(x, y);
			this.alt = alt;
		}
		public PointAndMeta(Point p, boolean alt)
		{
			super(p.x, p.y);
			this.alt = alt;
		}
	}
	
	static class DirectionAndMeta extends Direction
	{
		public final boolean alt;
		public DirectionAndMeta(int dx, int dy, boolean alt)
		{
			super(dx, dy);
			this.alt = alt;
		}
		public DirectionAndMeta(Direction d, boolean alt)
		{
			super(d.dx, d.dy);
			this.alt = alt;
		}
		public PointAndMeta apply(Point p){
			return new PointAndMeta(p.x + dx, p.y + dy, alt);
		}
	}
	
	static class Direction{
		public final int dx, dy; Direction(int dx, int dy){this.dx=dx;this.dy=dy;}
		public Point apply(Point p){
			return new Point(p.x + dx, p.y + dy);
		}
		public boolean isDiagonal(){
			return dx != 0 && dy != 0;
		}
		public boolean equals(Object o){
			if (o instanceof Direction){
				Direction that = (Direction) o;
				return this.dx == that.dx && this.dy == that.dy;
			}
			return false;
		}
		public Direction rotateCW(){
			if (dx != 0 && dy != 0)
			{
				if (dx == dy)
					return new Direction(0, dy);
				else
					return new Direction(dx, 0);
			}
			else if (dx == 0 && dy != 0)
			{
				return new Direction(-dy, dy);
			}
			else if (dx != 0 && dy == 0)
			{
				return new Direction(dx, dx);
			}
			
			throw new Error();
		}
		public Direction rotateCCW(){
			if (dx != 0 && dy != 0)
			{
				if (dx == dy)
					return new Direction(dx, 0);
				else
					return new Direction(0, dy);
			}
			else if (dx == 0 && dy != 0)
			{
				return new Direction(dy, dy);
			}
			else if (dx != 0 && dy == 0)
			{
				return new Direction(dx, -dx);
			}
			
			throw new Error();
		}
		public Direction reverse(){return new Direction(-dx,-dy);}
		public static List<Direction> dirs;
		static {
			dirs = new ArrayList<Direction>(8);
			dirs.add(new Direction(1,0));
			dirs.add(new Direction(1,-1));
			dirs.add(new Direction(0,-1));
			dirs.add(new Direction(-1,-1));
			dirs.add(new Direction(-1,0));
			dirs.add(new Direction(-1,1));
			dirs.add(new Direction(0,1));
			dirs.add(new Direction(1,1));
		}
	}
	
	static Color randLineColor(){
		int x = rand.nextInt(256);
		return new Color(x, 255 - x, 0, 127);
	}
	
	static Color heatColor(double val){
		double rfrac = val;
		double gfrac = 0;
		double bfrac = 0;
		if (rfrac > 1){
			gfrac = rfrac - 1;
			rfrac = 1;
		}
		if (gfrac > 1){
			bfrac = gfrac-1;
			gfrac=1;
		}
		if (bfrac > 1){
			bfrac = 1;
		}
		return new Color((int)(255.0*rfrac),(int)(255.0*gfrac),(int)(255.0*bfrac));
	}
	
	static interface Draw{
		public void paint(Graphics g);
	}
	
	static void draw(final int w, final int h, final Draw d){
		final JPanel panel = new JPanel(){
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g){
				d.paint(g);
			}
		};
		
		panel.setPreferredSize(new Dimension(w + 1, h + 1));
		final JFrame frame = new JFrame("Preview");
		frame.add(panel);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		MouseAdapter clickClose = new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				frame.setVisible(false);
				frame.dispose();
			}
		};
		
		panel.addMouseListener(clickClose);
		frame.addMouseListener(clickClose);
		
		while (frame.isVisible()){
			try{Thread.sleep(100);}catch(InterruptedException e){}
		}
	}
}
