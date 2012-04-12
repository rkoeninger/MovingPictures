import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

public class TileSet
{
	public static boolean automaticFamilyDetection = true;
	
	public static TileSet load() throws IOException
	{
		TileSet ts = new TileSet();
		
		if (automaticFamilyDetection)
		{
			for (File file : new File("./tiles").listFiles())
			{
				if (file.getName().endsWith(".bmp"))
				{
					ts.tree.put(loadTile(file.getName(), 0, 0, 0, 0));
				}
			}
		}
		else
		{
//			ts.tiles.add(loadTile("red.bmp", 1));
//			ts.tiles.add(loadTile("blue.bmp", 2));
//			ts.tiles.add(loadTile("green.bmp", 3));
//			
//			/*
//			 * Red/Blue transitions
//			 */
//			
//			// halvsies
//			ts.tiles.add(loadTile(5, 2, 2, 1, 1));
//			ts.tiles.add(loadTile(6, 1, 1, 2, 2));
//			ts.tiles.add(loadTile(7, 2, 1, 1, 2));
//			ts.tiles.add(loadTile(8, 1, 2, 2, 1));
//			ts.tiles.add(loadTile(50, 2, 2, 1, 1));
//			ts.tiles.add(loadTile(60, 1, 1, 2, 2));
//			ts.tiles.add(loadTile(70, 2, 1, 1, 2));
//			ts.tiles.add(loadTile(80, 1, 2, 2, 1));
//			
//			// red corners
//			ts.tiles.add(loadTile(17, 2, 2, 1, 2));
//			ts.tiles.add(loadTile(18, 2, 2, 2, 1));
//			ts.tiles.add(loadTile(19, 1, 2, 2, 2));
//			ts.tiles.add(loadTile(20, 2, 1, 2, 2));
//	
//			// blue corners
//			ts.tiles.add(loadTile(21, 1, 2, 1, 1));
//			ts.tiles.add(loadTile(22, 2, 1, 1, 1));
//			ts.tiles.add(loadTile(23, 1, 1, 1, 2));
//			ts.tiles.add(loadTile(24, 1, 1, 2, 1));
//			
//			//double corners
//			ts.tiles.add(loadTile(25, 2, 1, 2, 1));
//			ts.tiles.add(loadTile(26, 1, 2, 1, 2));
//			ts.tiles.add(loadTile(27, 1, 2, 1, 2));
//			ts.tiles.add(loadTile(28, 2, 1, 2, 1));
//			
//			/*
//			 * Blue/Green transitions
//			 */
//	
//			// halvsies
//			ts.tiles.add(loadTile(500, 2, 2, 3, 3));
//			ts.tiles.add(loadTile(600, 3, 3, 2, 2));
//			ts.tiles.add(loadTile(700, 2, 3, 3, 2));
//			ts.tiles.add(loadTile(800, 3, 2, 2, 3));
//	
//			// red corners
//			ts.tiles.add(loadTile(1700, 2, 2, 3, 2));
//			ts.tiles.add(loadTile(1800, 2, 2, 2, 3));
//			ts.tiles.add(loadTile(1900, 3, 2, 2, 2));
//			ts.tiles.add(loadTile(2000, 2, 3, 2, 2));
//	
//			// blue corners
//			ts.tiles.add(loadTile(2100, 3, 2, 3, 3));
//			ts.tiles.add(loadTile(2200, 2, 3, 3, 3));
//			ts.tiles.add(loadTile(2300, 3, 3, 3, 2));
//			ts.tiles.add(loadTile(2400, 3, 3, 2, 3));
//			
//			//double corners
//			ts.tiles.add(loadTile(2500, 2, 3, 2, 3));
//			ts.tiles.add(loadTile(2600, 3, 2, 3, 2));
//			ts.tiles.add(loadTile(2700, 3, 2, 3, 2));
//			ts.tiles.add(loadTile(2800, 2, 3, 2, 3));
//			
//			/*
//			 * Red/Green transitions
//			 */
//	
//			// halvsies
//			ts.tiles.add(loadTile(511, 3, 3, 1, 1));
//			ts.tiles.add(loadTile(611, 1, 1, 3, 3));
//			ts.tiles.add(loadTile(711, 3, 1, 1, 3));
//			ts.tiles.add(loadTile(811, 1, 3, 3, 1));
//			
//			// red corners
//			ts.tiles.add(loadTile(1711, 3, 3, 1, 3));
//			ts.tiles.add(loadTile(1811, 3, 3, 3, 1));
//			ts.tiles.add(loadTile(1911, 1, 3, 3, 3));
//			ts.tiles.add(loadTile(2011, 3, 1, 3, 3));
//	
//			// blue corners
//			ts.tiles.add(loadTile(2111, 1, 3, 1, 1));
//			ts.tiles.add(loadTile(2211, 3, 1, 1, 1));
//			ts.tiles.add(loadTile(2311, 1, 1, 1, 3));
//			ts.tiles.add(loadTile(2411, 1, 1, 3, 1));
//			
//			//double corners
//			ts.tiles.add(loadTile(2511, 3, 1, 3, 1));
//			ts.tiles.add(loadTile(2611, 1, 3, 1, 3));
//			ts.tiles.add(loadTile(2711, 1, 3, 1, 3));
//			ts.tiles.add(loadTile(2811, 3, 1, 3, 1));
//			
//			/*
//			 * Three-way transitions
//			 */
//			
//			ts.tiles.add(loadTile(9900, 3, 1, 3, 2));
//			ts.tiles.add(loadTile(9901, 3, 2, 3, 1));
//			ts.tiles.add(loadTile(9902, 1, 3, 2, 3));
//			ts.tiles.add(loadTile(9903, 2, 3, 1, 3));
//			
//			ts.tiles.add(loadTile(9904, 2, 1, 3, 3));
//			ts.tiles.add(loadTile(9905, 1, 2, 3, 3));
//			ts.tiles.add(loadTile(9906, 1, 3, 3, 2));
//			ts.tiles.add(loadTile(9907, 2, 3, 3, 1));
//			ts.tiles.add(loadTile(9908, 3, 3, 2, 1));
//			ts.tiles.add(loadTile(9909, 3, 3, 1, 2));
//			ts.tiles.add(loadTile(99010, 3, 2, 1, 3));
//			ts.tiles.add(loadTile(99011, 3, 1, 2, 3));
//			
//			ts.tiles.add(loadTile(99101, 1, 3, 2, 2));
//			ts.tiles.add(loadTile(99102, 3, 2, 2, 1));
//			ts.tiles.add(loadTile(99103, 2, 2, 1, 3));
//			ts.tiles.add(loadTile(99104, 2, 1, 3, 2));
//			ts.tiles.add(loadTile(99105, 3, 1, 2, 2));
//			ts.tiles.add(loadTile(99106, 1, 2, 2, 3));
//			ts.tiles.add(loadTile(99107, 2, 2, 3, 1));
//			ts.tiles.add(loadTile(99108, 2, 3, 1, 2));
//			
//			ts.tiles.add(loadTile(99201, 3, 2, 1, 1));
//			ts.tiles.add(loadTile(99202, 2, 1, 1, 3));
//			ts.tiles.add(loadTile(99203, 1, 1, 3, 2));
//			ts.tiles.add(loadTile(99204, 1, 3, 2, 1));
//			ts.tiles.add(loadTile(99205, 2, 3, 1, 1));
//			ts.tiles.add(loadTile(99206, 3, 1, 1, 2));
//			ts.tiles.add(loadTile(99207, 1, 1, 2, 3));
//			ts.tiles.add(loadTile(99208, 1, 2, 3, 1));
//	
//			ts.tiles.add(loadTile(99991, 2, 3, 2, 1));
//			ts.tiles.add(loadTile(99992, 2, 1, 2, 3));
//			ts.tiles.add(loadTile(99993, 1, 2, 3, 2));
//			ts.tiles.add(loadTile(99994, 3, 2, 1, 2));
//			ts.tiles.add(loadTile(99995, 3, 1, 2, 1));
//			ts.tiles.add(loadTile(99996, 2, 1, 3, 1));
//			ts.tiles.add(loadTile(99997, 1, 2, 1, 3));
//			ts.tiles.add(loadTile(99998, 1, 3, 1, 2));
		}
		
		for (File gFile : new File("./tiles/groups").listFiles())
		{
			if (gFile.getName().endsWith(".txt"))
			{
				TileGroup tg = TileGroup.load(gFile);
				ts.groups.add(tg);
				ts.groupTable.put(gFile.getName().substring(0, gFile.getName().indexOf(".")), tg);
			}
		}
		
		return ts;
	}
	
	private static Tile loadTile(String path, int familyNE, int familyNW, int familySW, int familySE) throws IOException
	{
		Image img = null;
		
		try
		{
			img = ImageIO.read(new File("./tiles/", path));
		}
		catch (IIOException iioe)
		{
			System.err.println(path);
		}
		
		if (automaticFamilyDetection)
		{
			Raster raster = ((BufferedImage) img).getRaster();
			
			int w = raster.getWidth();
			int h = raster.getHeight();
			
			int[] pixel = new int[4];
			
			raster.getPixel(w - 1, 0, pixel);
			familyNE = getFamily(pixel);

			raster.getPixel(0, 0, pixel);
			familyNW = getFamily(pixel);

			raster.getPixel(0, h - 1, pixel);
			familySW = getFamily(pixel);

			raster.getPixel(w - 1, h - 1, pixel);
			familySE = getFamily(pixel);
		}
		
		return new Tile(img, familyNE, familyNW, familySW, familySE);
	}
	
	private static int getFamily(int[] pixel)
	{
		if (pixel[0] == 255)
			return 1;
		if (pixel[1] == 255)
			return 3;
		if (pixel[2] == 255)
			return 2;
		
		throw new Error("can't detect family");
	}
	
	private Collection<TileGroup> groups;
	
	private Map<String, TileGroup> groupTable;
	
	private TileTree tree;
	
	private TileSet()
	{
		tree = new TileTree();
		groups = new ArrayList<TileGroup>();
		groupTable = new HashMap<String, TileGroup>();
	}
	
	public TileGroup getGroup()
	{
		return groups.iterator().next();
	}
	
	public TileGroup getGroup(String name)
	{
		return groupTable.get(name);
	}
	
	/**
	 * Doesn't accept wildcard: 0.
	 */
	public Tile getPlainTile(int family)
	{
		List<Tile> applicableTiles = tree.get(family, family, family, family);
		
		return applicableTiles.get((int) (Math.random() * applicableTiles.size()));
	}

	public Tile getTransitionTile(int familyNE, int familyNW, int familySW, int familySE)
	{
		List<Tile> applicableTiles = tree.get(familyNE, familyNW, familySW, familySE);
		
		if (applicableTiles.isEmpty())
			throw new Error("No applicable tiles: " + familyNE + ", " + familyNW + ", " + familySW + ", " + familySE);

		return applicableTiles.get((int) (Math.random() * applicableTiles.size()));
	}

	public Tile getAltTransitionTile(int familyNE, int familyNW, int familySW, int familySE, Tile current)
	{
		List<Tile> applicableTiles = tree.get(familyNE, familyNW, familySW, familySE);
		
		if (applicableTiles.isEmpty())
			throw new Error("No applicable tiles: " + familyNE + ", " + familyNW + ", " + familySW + ", " + familySE);
		
		List<Tile> choiceList = new ArrayList<Tile>(applicableTiles);
		choiceList.remove(current);
		return choiceList.get((int) (Math.random() * choiceList.size()));
	}
	
	public static Tile getRandomTile(int family)
	{
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = img.getGraphics();
		g.setColor(new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
		g.fillRect(0, 0, 32, 32);
		
		return new Tile(img, family);
	}
	
	public static int N = 1, S = 2, E = 4, W = 8;
}
