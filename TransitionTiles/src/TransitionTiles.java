import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TransitionTiles
{
	static String tileSetFolder =
		"..\\OP2Tileset";
	
	static String ogTileSet =
		"well0010\\well";
	
	static String bgTileSet =
		"well0006\\well";
	
	static Color O_FAMILY = new Color(128, 68, 13);
	static Color G_FAMILY = new Color(115, 106, 87);
	static Color B_FAMILY = new Color(0, 0, 0); // BE MORE PRECISE
	
	static enum Family{O,G,B}
	
	public static void main(String[] args) throws Exception
	{
		File root = new File(tileSetFolder, ogTileSet);
		File root2 = new File(tileSetFolder, bgTileSet);
		
		Collection<Tile> tiles = new ArrayList<Tile>();
		
		ArrayList<File> fileList = new ArrayList<File>();
		fileList.addAll(Arrays.asList(root.listFiles()));
		fileList.addAll(Arrays.asList(root2.listFiles()));
		
		for (File file : fileList)
		{
			if (!file.getName().endsWith(".bmp")) continue;
			
			BufferedImage img = ImageIO.read(file);
			Tile tile = new Tile(img, file.getName());
			tile.name = file.getName();
			tile.name = tile.name.substring(0, tile.name.indexOf('.'));
			tiles.add(tile);
		}
		
		for (Tile tile : tiles)
		{
			for (Tile other : tiles)
			{
				if (tile == other) continue;
				
				if (matches(tile, N, other))
				{
					System.out.println(tile.name + " matches " + other.name + " to the north");
					tile.nNeighbors.add(other);
					other.sNeighbors.add(tile);
					showAdjacentTilesDialog(tile, other, N);
				}
				
				if (matches(tile, E, other))
				{
					System.out.println(tile.name + " matches " + other.name + " to the east");
					tile.eNeighbors.add(other);
					other.wNeighbors.add(tile);
					showAdjacentTilesDialog(tile, other, E);
				}
				
				if (matches(tile, S, other))
				{
					System.out.println(tile.name + " matches " + other.name + " to the south");
					tile.sNeighbors.add(other);
					other.nNeighbors.add(tile);
					showAdjacentTilesDialog(tile, other, S);
				}
				
				if (matches(tile, W, other))
				{
					System.out.println(tile.name + " matches " + other.name + " to the west");
					tile.wNeighbors.add(other);
					other.eNeighbors.add(tile);
					showAdjacentTilesDialog(tile, other, W);
				}
			}
		}
		
		System.exit(0);
	}
	
	public static void main2(String[] args) throws Exception
	{
		File root = new File(tileSetFolder, ogTileSet);
		
		Collection<Color> colors = new ArrayList<Color>();
		
		for (File file : root.listFiles())
		{
			if (!file.getName().endsWith(".bmp"))
				continue;
			
			BufferedImage img = ImageIO.read(file);
			Color avg = average(img.getRaster(), 0, 0, 32, 32);
			colors.add(avg);
			showPreviewDialog2(img, avg);
		}
		
		showPreviewDialog(null, average(colors));
		
		System.exit(0);
	}
	
	static void showAdjacentTilesDialog(final Tile ta,final  Tile tb,final int side)
	{
		final BufferedImage a = ta.img;
		final BufferedImage b = tb.img;
		JDialog dialog = new JDialog();
		JPanel panel = new JPanel(){
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g){
				g.drawImage(a, 64, 64, 128, 128, 0, 0, 32, 32, null);
				switch (side){
				case N: g.drawImage(b, 64, 0, 128, 64, 0, 0, 32, 32, null); break;
				case S: g.drawImage(b, 64, 128, 128, 192, 0, 0, 32, 32, null); break;//b
				case W: g.drawImage(b, 0, 64, 64, 128, 0, 0, 32, 32, null); break;//l
				case E: g.drawImage(b, 128, 64, 192, 128, 0, 0, 32, 32, null); break;//r
				}
				if (showGrid){
				g.setColor(Color.BLACK);
				g.drawLine(64, 0, 64, 191);
				g.drawLine(128, 0, 128, 191);
				g.drawLine(0, 64, 191, 64);
				g.drawLine(0, 128, 191, 128);}
			}
		};
		panel.setPreferredSize(new Dimension(192, 192));
		dialog.add(panel);
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(null);
		dialog.addMouseListener(new ClickListener(dialog));
		dialog.addKeyListener(new KeyListener(dialog));
		dialog.setVisible(true);
		dialog.dispose();
	}
	
	static void showPreviewDialog(BufferedImage img, Color avg)
	{
		float[] hsb = Color.RGBtoHSB(
			avg.getRed(), avg.getGreen(), avg.getBlue(), null);
		DecimalFormat f = new DecimalFormat("0.00");
		
		JLabel rgbLabel = new JLabel(
			avg.getRed() + ", " +
			avg.getGreen() + ", " +
			avg.getBlue());
		
		JLabel hsbLabel = new JLabel(
			f.format(hsb[0] * 360) + ", " +
			f.format(hsb[1]) + ", " +
			f.format(hsb[2]));
		
		JDialog dialog = new JDialog();
		dialog.getContentPane().setBackground(Color.WHITE);
		dialog.setModal(true);
		dialog.setLayout(new GridLayout(4, 1));
		dialog.add(new PreviewPanel(img, avg));
		dialog.add(rgbLabel);
		dialog.add(hsbLabel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.addMouseListener(new ClickListener(dialog));
		dialog.setVisible(true);
		dialog.dispose();
	}
	
	static void showPreviewDialog2(BufferedImage img, Color avg)
	{
		float[] hsb = Color.RGBtoHSB(
			avg.getRed(), avg.getGreen(), avg.getBlue(), null);
		DecimalFormat f = new DecimalFormat("0.00");
		
		JLabel rgbLabel = new JLabel(
			avg.getRed() + ", " +
			avg.getGreen() + ", " +
			avg.getBlue());
		
		JLabel hsbLabel = new JLabel(
			f.format(hsb[0] * 360) + ", " +
			f.format(hsb[1]) + ", " +
			f.format(hsb[2]));
		
		Color c1 = average(img.getRaster(), 0, 0, 2, 2);
		Color c2 = average(img.getRaster(), 28, 0, 2, 2);
		Color c3 = average(img.getRaster(), 0, 28, 2, 2);
		Color c4 = average(img.getRaster(), 28, 28, 2, 2);
		
		JDialog dialog = new JDialog();
		dialog.getContentPane().setBackground(Color.WHITE);
		dialog.setModal(true);
		dialog.setLayout(new GridLayout(5, 1));
		dialog.add(new PreviewPanel(img, avg));
		dialog.add(rgbLabel);
		dialog.add(hsbLabel);
		dialog.add(new PreviewPanel2(c1, c2, c3, c4));
		dialog.add(new JLabel(
			family(c1) + ", " +
			family(c2) + ", " +
			family(c3) + ", " +
			family(c4)));
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.addMouseListener(new ClickListener(dialog));
		dialog.setVisible(true);
		dialog.dispose();
	}
	
	static class ClickListener extends MouseAdapter
	{
		JDialog d;
		
		ClickListener(JDialog d)
		{
			this.d = d;
		}
		
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON3)
				System.exit(0);
			
			d.setVisible(false);
		}
	}
	
	static boolean showGrid = true;
	
	static class KeyListener extends KeyAdapter
	{
		JDialog d;
		
		KeyListener(JDialog d){this.d=d;}
		
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_G){
				showGrid = !showGrid;
				d.repaint();
			}
		}
	}
	
	static class PreviewPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		BufferedImage img;
		Color avg;
		
		PreviewPanel(BufferedImage img, Color avg)
		{
			this.img = img;
			this.avg = avg;
			setPreferredSize(new Dimension(140, 72));
		}
		
		public void paintComponent(Graphics g)
		{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			AffineTransform t = new AffineTransform();
			t.scale(2, 2);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setTransform(t);
			g.drawImage(img, 2, 2, null);
			g2d.setTransform(new AffineTransform());
			//g.setColor(avg);
			//g.fillRect(72, 4, 64, 64);
			//g.translate(64 + 8 + 6, 4 + 2);
			g2d.setStroke(new BasicStroke(
				8,
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND
			));
			new Tile(img, "").paint(g2d);
		}
	}
	
	static class PreviewPanel2 extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		Color c1, c2, c3, c4;
		
		PreviewPanel2(Color c1, Color c2, Color c3, Color c4)
		{
			this.c1 = c1;
			this.c2 = c2;
			this.c3 = c3;
			this.c4 = c4;
			setPreferredSize(new Dimension(32 * 4 + 4 * 5, 32 + 4 * 2));
		}
		
		public void paintComponent(Graphics g)
		{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(c1);
			g.fillRect(4,   4, 32, 32);
			g.setColor(c2);
			g.fillRect(40,  4, 32, 32);
			g.setColor(c3);
			g.fillRect(76,  4, 32, 32);
			g.setColor(c4);
			g.fillRect(112, 4, 32, 32);
			g.setColor(Color.BLACK);
			g.drawString("NW", 8,   48);
			g.drawString("NE", 44,  48);
			g.drawString("SW", 82,  48);
			g.drawString("SE", 116, 48);
		}
	}
	
	static Color average(Raster raster, int x0, int y0, int w, int h)
	{
		int[] pixel = new int[4];
		double[] averagePixel = new double[3];
		double countFactor = 1.0 / (double)(w * h);
		
		for (int x = x0; x < x0 + w; ++x)
		for (int y = y0; y < y0 + h; ++y)
		{
			raster.getPixel(x, y, pixel);
			averagePixel[0] += pixel[0] * countFactor;
			averagePixel[1] += pixel[1] * countFactor;
			averagePixel[2] += pixel[2] * countFactor;
		}
		
		return new Color(
			(int)averagePixel[0],
			(int)averagePixel[1],
			(int)averagePixel[2]
		);
	}
	
	static Family family(Color color)
	{
		int oDiff = difference(O_FAMILY, color);
		int gDiff = difference(G_FAMILY, color);
		int bDiff = difference(B_FAMILY, color);
		return Family.values()[minIndex(new int[]{oDiff, gDiff, bDiff})];
	}
	
	static Color familyColor(Family f){
		switch (f){
		case O : return O_FAMILY;
		case G : return G_FAMILY;
		case B : return B_FAMILY;
		}
		
		throw new Error();
	}
	
	static Color familyColor(Color c){
		return familyColor(family(c));
	}
	
	static int minIndex(int[] a){
		int minIndex = 0;
		int minValue = a[0];
		
		for (int i = 1; i < a.length; ++i){
			if (a[i] < minValue){
				minIndex = i;
				minValue = a[i];
			}
		}
		
		return minIndex;
	}
	
	static int difference(Color c1, Color c2)
	{
		return Math.abs(c1.getRed()   - c2.getRed())
			 + Math.abs(c1.getGreen() - c2.getGreen())
			 + Math.abs(c1.getBlue()  - c2.getBlue());
	}
	
	static Color average(Collection<Color> colors)
	{
		double[] averagePixel = new double[3];
		double countFactor = 1.0 / (double)(colors.size());
		
		for (Color c : colors)
		{
			averagePixel[0] += c.getRed()   * countFactor;
			averagePixel[1] += c.getGreen() * countFactor;
			averagePixel[2] += c.getBlue()  * countFactor;
		}
		
		return new Color(
			(int)averagePixel[0],
			(int)averagePixel[1],
			(int)averagePixel[2]
		);
	}
	
	static class Tile
	{
		static int tileSize = 32;
		static int pTileSize = 64;
		
		BufferedImage img;
		Color[] nEdge;
		Color[] sEdge;
		Color[] wEdge;
		Color[] eEdge;
		
		String name;
		
		Set<Tile> nNeighbors = new HashSet<Tile>();
		Set<Tile> sNeighbors = new HashSet<Tile>();
		Set<Tile> wNeighbors = new HashSet<Tile>();
		Set<Tile> eNeighbors = new HashSet<Tile>();
		
		Tile(BufferedImage img, String name)
		{
			this.img = img;
			Raster raster = img.getRaster();
			
			nEdge = new Color[4];
			sEdge = new Color[4];
			wEdge = new Color[4];
			eEdge = new Color[4];
			
			nEdge[0] = average(raster, 0, 0, tileSize/4, tileSize/8);
			nEdge[1] = average(raster, tileSize/4, 0, tileSize/4, tileSize/8);
			nEdge[2] = average(raster, tileSize/2, 0, tileSize/4, tileSize/8);
			nEdge[3] = average(raster, tileSize*3/4, 0, tileSize/4, tileSize/8);
			
			sEdge[0] = average(raster, 0, tileSize-tileSize/8, tileSize/4, tileSize/8);
			sEdge[1] = average(raster, tileSize/4, tileSize-tileSize/8, tileSize/4, tileSize/8);
			sEdge[2] = average(raster, tileSize/2, tileSize-tileSize/8, tileSize/4, tileSize/8);
			sEdge[3] = average(raster, tileSize*3/4, tileSize-tileSize/8, tileSize/4, tileSize/8);
			
			wEdge[0] = average(raster, 0, 0, tileSize/8, tileSize/4);
			wEdge[1] = average(raster, 0, tileSize/4, tileSize/8, tileSize/4);
			wEdge[2] = average(raster, 0, tileSize/2, tileSize/8, tileSize/4);
			wEdge[3] = average(raster, 0, tileSize*3/4, tileSize/8, tileSize/4);
			
			eEdge[0] = average(raster, tileSize-tileSize/8, 0, tileSize/8, tileSize/4);
			eEdge[1] = average(raster, tileSize-tileSize/8, tileSize/4, tileSize/8, tileSize/4);
			eEdge[2] = average(raster, tileSize-tileSize/8, tileSize/2, tileSize/8, tileSize/4);
			eEdge[3] = average(raster, tileSize-tileSize/8, tileSize*3/4, tileSize/8, tileSize/4);
		}
		
		public void paint(Graphics g)
		{
			for (int x = 0; x < 4; ++x)
			{
				g.setColor(familyColor(nEdge[x]));
				g.drawLine(x * (pTileSize/4), 0, x * (pTileSize/4) + (pTileSize/4), 0);
			}

			for (int x = 0; x < 4; ++x)
			{
				g.setColor(familyColor(sEdge[x]));
				g.drawLine(x * (pTileSize/4), (pTileSize-1), x * (pTileSize/4) + (pTileSize/4), (pTileSize-1));
			}

			for (int x = 0; x < 4; ++x)
			{
				g.setColor(familyColor(wEdge[x]));
				g.drawLine(0, x * (pTileSize/4), 0, x * (pTileSize/4) + (pTileSize/4));
			}

			for (int x = 0; x < 4; ++x)
			{
				g.setColor(familyColor(eEdge[x]));
				g.drawLine((pTileSize-1), x * (pTileSize/4), (pTileSize-1), x * (pTileSize/4) + (pTileSize/4));
			}
		}
	}
	
	static final int N = 1, S = 2, E = 4, W = 8;
	
	static boolean matches(Tile t1, int side, Tile t2)
	{
		switch (side)
		{
		case N:
			for (int x = 0; x < 4; ++x)
				if (family(t1.nEdge[x]) != family(t2.sEdge[x]))
					return false;
			break;
		case S:
			for (int x = 0; x < 4; ++x)
				if (family(t1.sEdge[x]) != family(t2.nEdge[x]))
					return false;
			break;
		case E:
			for (int x = 0; x < 4; ++x)
				if (family(t1.eEdge[x]) != family(t2.wEdge[x]))
					return false;
			break;
		case W:
			for (int x = 0; x < 4; ++x)
				if (family(t1.wEdge[x]) != family(t2.eEdge[x]))
					return false;
			break;
		}
		
		return true;
	}
}
