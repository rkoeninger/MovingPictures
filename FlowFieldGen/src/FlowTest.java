import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.*;

public class FlowTest {

	static double[][] costs;
	static ArrayList<Point> path;
	static Point startingPoint;
	
	static boolean flowMode = false;
	static JFrame frame;
	static int tileSize = 16;
	
	public static void main(String[] args) throws IOException{
		frame = new JFrame();
		
		BufferedImage img = (BufferedImage) ImageIO.read(new File("terrain.bmp"));
		WritableRaster srcRaster = img.getRaster();
		costs = new double[img.getWidth()][img.getHeight()];
		int[] pixel = new int[4];
		int[] red = new int[]{255, 0, 0, 0};
		int[] blue = new int[]{0, 0, 255, 0};
		
		path = new ArrayList<Point>();
		
		for (int y = 0; y < costs.length; ++y)
			for (int x = 0; x < costs[y].length; ++x){
				srcRaster.getPixel(x, y, pixel);
				if (Arrays.equals(pixel, red))
				{
					costs[y][x] = 0.0;
					path.add(new Point(x, y));
				}
				else if (Arrays.equals(pixel, blue))
				{
					costs[y][x] = 0.0;
					startingPoint = new Point(x, y);
				}
				else
				{
					costs[y][x] = Double.POSITIVE_INFINITY;
				}
			}
		
		final JPanel showPanel = new ShowPanel(img.getHeight(), img.getWidth());
		
		JButton calcButton = new JButton("Derive");
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(showPanel, BorderLayout.CENTER);
		frame.getContentPane().add(calcButton, BorderLayout.SOUTH);
		
		calcButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				double[][] costs2 = new double[costs.length][costs[0].length];
				
				ArrayList<Point> sortedPath = new ArrayList<Point>(path.size() + 1);
				sortedPath.add(startingPoint);
				Point current = startingPoint;
				Point previous = null;
				
				while (current != null)
				{
					Point toAdd;
					
					if      (checkPoint(current.x + 1, current.y,     previous)) toAdd = new Point(current.x + 1, current.y    );
					else if (checkPoint(current.x - 1, current.y,     previous)) toAdd = new Point(current.x - 1, current.y    );
					else if (checkPoint(current.x,     current.y + 1, previous)) toAdd = new Point(current.x,     current.y + 1);
					else if (checkPoint(current.x,     current.y - 1, previous)) toAdd = new Point(current.x,     current.y - 1);
					else if (checkPoint(current.x + 1, current.y + 1, previous)) toAdd = new Point(current.x + 1, current.y + 1);
					else if (checkPoint(current.x - 1, current.y + 1, previous)) toAdd = new Point(current.x - 1, current.y + 1);
					else if (checkPoint(current.x + 1, current.y - 1, previous)) toAdd = new Point(current.x + 1, current.y - 1);
					else if (checkPoint(current.x - 1, current.y - 1, previous)) toAdd = new Point(current.x - 1, current.y - 1);
					
					else break;
					
					sortedPath.add(toAdd);
					previous = current;
					current = toAdd;
				}
				
				for (Point p : sortedPath){
					System.out.println(p + "\t\t" + (sortedPath.size() - sortedPath.indexOf(p) - 1));
				}
				
				for (int y = 0; y < costs.length; ++y)
					for (int x = 0; x < costs[y].length; ++x){
						Point p = new Point(x, y);
						
						costs2[y][x] = !sortedPath.contains(p)
								? distanceToClosest(p, sortedPath)
								: (sortedPath.size() - sortedPath.indexOf(p) - 1) / ((double)sortedPath.size()*10);
					}
				
				costs = costs2;
				flowMode = true;
				showPanel.repaint();
			}
		});
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
	
	static double distanceToClosest(Point p, ArrayList<Point> points){
		double shortestDistance = 1000000.0;
		for (Point q : points){
			shortestDistance = Math.min(Math.sqrt(Math.pow((p.x-q.x),2) + Math.pow((p.y-q.y),2)), shortestDistance);
		}
		return shortestDistance;
	}
	
	static boolean checkPoint(int candidateX, int candidateY, Point previous){
		Point candidate = new Point(candidateX, candidateY);
		return (path.contains(candidate) && !candidate.equals(previous));
	}
static Random rand = new Random();	
	@SuppressWarnings("serial")
	static class ShowPanel extends JPanel{

		public ShowPanel(int w, int h){
			setPreferredSize(new Dimension(w * tileSize, h * tileSize));
			
			addMouseMotionListener(new MouseMotionAdapter(){
				public void mouseMoved(MouseEvent e){
					frame.setTitle((e.getX()/tileSize) + ", " + (e.getY()/tileSize)
							+ " = " + costs[e.getY()/tileSize][e.getX()/tileSize]);
				}
			});
		}
		
		public void paintComponent(Graphics g){
			for (int y = 0; y < costs.length; ++y)
				for (int x = 0; x < costs[y].length; ++x){
					int grayscale = (int)(Math.pow(2, -costs[y][x]) * 255);
					g.setColor(new Color(grayscale, grayscale, grayscale));
					g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
					
					if (flowMode){
						
						//g.setColor(new Color(rand.nextInt(128) + 128, rand.nextInt(64) + 64, rand.nextInt(64) + 64));
						if ((((x ^ y) >> 0) & 0x1) == 0){
							g.setColor(Color.RED);
						}else{
							g.setColor(Color.BLUE);
						}
						
						double lowestCost = Double.POSITIVE_INFINITY;
						Point lowestNeighbor = new Point(x, y);
						
						Point neighbor;
						
						neighbor = new Point(x+1, y);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x-1, y);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x, y+1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x, y-1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x+1, y+1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x+1, y-1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x-1, y+1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}

						neighbor = new Point(x-1, y-1);
						if (getCost(neighbor) < lowestCost){
							lowestNeighbor = neighbor;
							lowestCost = getCost(neighbor);
						}
						
						g.drawLine(x*tileSize + tileSize/2,
								   y*tileSize + tileSize/2,
								lowestNeighbor.x*tileSize + tileSize/2,
								lowestNeighbor.y*tileSize + tileSize/2
								);
						g.fillOval(x*tileSize + tileSize/4,
								y*tileSize + tileSize/4,
								tileSize / 2,
								tileSize / 2);
					}
					
				}
			
			
			g.setColor(new Color(0,255,0,127));
			g.fillRect(startingPoint.x * tileSize, startingPoint.y * tileSize, tileSize, tileSize);
			
		}
		
	}
	
	static double getCost(Point p){
		try                                     { return costs[p.y][p.x];              }
		catch (ArrayIndexOutOfBoundsException e){ return Double.POSITIVE_INFINITY; }
	}
	
}
