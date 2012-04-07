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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DensityWaypointMap
{
	final static int tileSize = 8;
	
	public static void main(String[] args) throws IOException
	{
		final int[][] terrain = loadTerrain(new File("terrain.bmp"));
		final int w = terrain.length;
		final int h = terrain[0].length;
		final int[][] distGraph = new int[w][h];
		
		for (int x = 0; x < w; ++x){
			for (int y = 0; y < h; ++y){
				if (terrain[x][y] != -1){
					distGraph[x][y] = (int)findClosestWall(terrain, new Point(x, y), true);
				}else{
					distGraph[x][y] = 0;
				}
			}
		}
		
		draw(w*tileSize, h*tileSize,new Draw(){
			public void paint(Graphics g){
				final double maxDist = Math.sqrt((w*w)+(h*h));
				
				for (int x = 0; x < w; ++x){
					for (int y = 0; y < h; ++y){
							int dist = distGraph[x][y];
							double rfrac = (dist/maxDist)*14;
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
							g.setColor(new Color((int)(255.0*rfrac),(int)(255.0*gfrac),(int)(255.0*bfrac)));
						g.fillRect(x*tileSize,y*tileSize,tileSize,tileSize);
					}
				}
			}
		});
		
		final int[][] waypointGraph = new int[w][h];
		
		final Set<Point> waypoints = new HashSet<Point>();
		
		/*
		 * Until every spot on map is in view of a waypoint and every
		 * waypoint is in view and connected to another waypoint, find
		 * the greatest valued spot on the dist graph, call it a waypoint,
		 * connect to other waypoints that have LOS
		 * and reduce values of spots on map
		 * around waypoint that have LOS with that waypoint.
		 */
//		while (!isFull(waypointGraph)){
			Point nextWaypoint = findMax(distGraph);
			int dist = distGraph[nextWaypoint.x][nextWaypoint.y];
			waypoints.add(nextWaypoint);
			System.out.println(nextWaypoint);
			
			for (int x = 0; x < w; ++x)
			for (int y = 0; y < h; ++y)
			{
				distGraph[x][y] -= dist /
					(1 + absDistance(nextWaypoint.x-x,
						nextWaypoint.y-y));
			}
//		}
			
			draw(w * tileSize, h * tileSize, new Draw(){
				public void paint(Graphics g){
					final double maxDist = Math.sqrt((w*w)+(h*h));
					
					for (int x = 0; x < w; ++x){
						for (int y = 0; y < h; ++y){
								int dist = distGraph[x][y];
								double rfrac = (dist/maxDist)*14;
								double gfrac = 0;
								double bfrac = 0;
								if (rfrac < 0){
									rfrac = 0;
								}
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
								g.setColor(new Color((int)(255.0*rfrac),(int)(255.0*gfrac),(int)(255.0*bfrac)));
							g.fillRect(x*tileSize,y*tileSize,tileSize,tileSize);
						}
					}
				}
			});
	}
	
	static boolean isFull(int[][] waypointGraph){
		for (int x = 0; x < waypointGraph.length; ++x){
			for (int y = 0; y < waypointGraph[x].length; ++y){
				if (waypointGraph[x][y]==0)
					return false;
			}
		}
		return true;
	}
	
	static Point findMax(int[][] densityGraph){
		Point maxPoint = null;
		int maxDist = Integer.MIN_VALUE;
		for (int x = 0; x < densityGraph.length; ++x){
			for (int y = 0; y < densityGraph[x].length; ++y){
				if (densityGraph[x][y]>maxDist){
					maxDist = densityGraph[x][y];
					maxPoint = new Point(x,y);
				}
			}
		}
		return maxPoint;
	}
	
	static double absDistance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.x-p2.x,2)+Math.pow(p1.y-p2.y,2));
	}
	
	static double absDistance(double dx, double dy){
		return Math.sqrt((dx*dx)+(dy*dy));
	}
	
	static int sqDistance(Point p1, Point p2){
		return (p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y);
	}

	static int sqDistance(int x, int y, Point p2){
		return ((x-p2.x)*(x-p2.x))+((y-p2.y)*(y-p2.y));
	}
	
	static double findClosestWall(int[][] terrain, Point p, boolean edgesAreWalls){
		int w = terrain.length;
		int h = terrain[0].length;
		double dist = Double.POSITIVE_INFINITY;
		if (edgesAreWalls)
			dist = Math.min(p.x < (w/2) ? p.x : w-p.x, p.y < (h/2) ? p.y : h-p.y);
		
		for (int x = 0; x < w; ++x){
			for (int y = 0; y < h; ++y){
				if (terrain[x][y] == -1){
					double currentDist = absDistance(p.x-x,p.y-y);
					if (currentDist < dist){
						dist = currentDist;
					}
				}
			}
		}
		
		return dist;
	}
	
	static boolean contains(int[][] grid, int x, int y){
		return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
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
	
	static class Direction{
		public final int dx, dy; Direction(int dx, int dy){this.dx=dx;this.dy=dy;}
		public Point apply(Point p){
			return new Point(p.x+dx,p.y+dy);
		}
		public static List<Direction> dirs;
		static {
			dirs = new ArrayList<Direction>(8);
			dirs.add(new Direction(1,0));
			dirs.add(new Direction(1,-1));
			dirs.add(new Direction(0,-1));
			dirs.add(new Direction(-1,-1));
			dirs.add(new Direction(-1,0));
			dirs.add(new Direction(-1,1));
			dirs.add(new Direction(1,0));
			dirs.add(new Direction(1,1));
		}
	}
	
	static Color randLineColor(){
		int x = rand.nextInt(256);
		return new Color(x, 255 - x, 0, 127);
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
