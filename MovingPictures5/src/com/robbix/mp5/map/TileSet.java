package com.robbix.mp5.map;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Neighbors;
import com.robbix.mp5.unit.HealthBracket;

public class TileSet
{
	/**
	 * Loads a TileSet from the given root directory.
	 */
	public static TileSet load(File rootDir, String name) throws IOException
	{
		rootDir = new File(rootDir, name);
		
		TileSet set = new TileSet();
		
		File[] plains = new File(rootDir, "plain").listFiles(Utils.BMP);
		
		for (int x = 0; x < plains.length; ++x)
		{
			set.tiles.put("plain/" + x, ImageIO.read(plains[x]));
			set.plainCount++;
		}
		
		File[] bulldozed = new File(rootDir, "bulldozed").listFiles(Utils.BMP);
		
		for (int x = 0; x < bulldozed.length; ++x)
		{
			set.tiles.put("bulldozed/" + x, ImageIO.read(bulldozed[x]));
			set.bulldozedCount++;
		}
		
		Neighbors[] tileOrder2 = new Neighbors[]
		{
			Neighbors.EW,
			Neighbors.NS,
			Neighbors.SW,
			Neighbors.SE,
			Neighbors.NW,
			Neighbors.NE,
			Neighbors.NEW,
			Neighbors.SEW,
			Neighbors.NSEW,
			Neighbors.NSW,
			Neighbors.NSE,
			Neighbors.S,
			Neighbors.N,
			Neighbors.E,
			Neighbors.W,
			Neighbors.NONE
		};
		
		File[] wallsG = new File(rootDir, "wall/greenhealth").listFiles(Utils.BMP);
		File[] wallsY = new File(rootDir, "wall/yellowhealth").listFiles(Utils.BMP);
		File[] wallsR = new File(rootDir, "wall/redhealth").listFiles(Utils.BMP);
		File[] tubes  = new File(rootDir, "tube").listFiles(Utils.BMP);
		
		sortFiles(wallsG);
		sortFiles(wallsY);
		sortFiles(wallsR);
		sortFiles(tubes);
		
		for (int x = 0; x < tileOrder2.length; ++x)
		{
			Image wallGreen  = ImageIO.read(wallsG[x]);
			Image wallYellow = ImageIO.read(wallsY[x]);
			Image wallRed    = ImageIO.read(wallsR[x]);
			Image tube       = ImageIO.read(tubes[x]);
			
			set.tiles.put("wall/green/"  + tileOrder2[x], wallGreen);
			set.tiles.put("wall/yellow/" + tileOrder2[x], wallYellow);
			set.tiles.put("wall/red/"    + tileOrder2[x], wallRed);
			set.tiles.put("tube/"        + tileOrder2[x], tube);
		}
		
		return set;
	}
	
	private static void sortFiles(File[] files)
	{
		Arrays.sort(files, new Comparator<File>()
		{
			public int compare(File a, File b)
			{
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
	}
	
	private Map<String, Image> tiles;
	private Random rand;
	
	private int bulldozedCount;
	private int plainCount;
	
	public TileSet()
	{
		tiles = new HashMap<String, Image>();
		rand = new Random();
		bulldozedCount = 0;
		plainCount = 0;
	}
	
	public Image getTile(String code)
	{
		return tiles.get(code);
	}
	
	public String getWallTile(Neighbors neighbors)
	{
		return getWallTile(neighbors, HealthBracket.GREEN);
	}
	
	public String getWallTile(Neighbors neighbors, HealthBracket health)
	{
		String healthCode = null;
		
		switch (health)
		{
			case GREEN:  healthCode = "green";  break;
			case YELLOW: healthCode = "yellow"; break;
			case RED:    healthCode = "red";    break;
			default: throw new IllegalArgumentException("Invalid wall health");
		}
		
		return Utils.getPath("wall", healthCode, neighbors);
	}

	public String getTubeTile(Neighbors neighbors)
	{
		return Utils.getPath("tube", neighbors);
	}

	public String getBulldozedTile()
	{
		return Utils.getPath(
			"bulldozed",
			rand.nextInt(bulldozedCount)
		);
	}
	
	/**
	 * Returns a semi-random plain tile in the given tile family.
	 */
	public String getPlainTile()
	{
		return Utils.getPath("plain", rand.nextInt(plainCount));
	}

	/**
	 * Returns specified plain tile in the given tile family.
	 */
	public String getPlainTile(int code)
	{
		return Utils.getPath("plain", "grass", code);
	}
	
	/**
	 * Returns semi-random tile that is appropriate for the specifed
	 * unmarked tile.
	 */
	public String getBlastMarkTile(String code, int blastCount)
	{
		return null;
	}
}
