package com.robbix.mp5.map;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.utils.Neighbors;
import com.robbix.mp5.utils.Utils;

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
			Tile pTile = new Tile(ImageIO.read(plains[x]));
			set.tiles.put("plain/" + x, pTile);
			set.plainCount++;
		}
		
		File[] bulldozed = new File(rootDir, "bulldozed").listFiles(Utils.BMP);
		
		for (int x = 0; x < bulldozed.length; ++x)
		{
			Tile bTile = new Tile(ImageIO.read(bulldozed[x]));
			set.tiles.put("bulldozed/" + x, bTile);
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
		
		Arrays.sort(wallsG, Utils.FILENAME);
		Arrays.sort(wallsY, Utils.FILENAME);
		Arrays.sort(wallsR, Utils.FILENAME);
		Arrays.sort(tubes, Utils.FILENAME);
		
		for (int x = 0; x < tileOrder2.length; ++x)
		{
			Image wallGreen  = ImageIO.read(wallsG[x]);
			Image wallYellow = ImageIO.read(wallsY[x]);
			Image wallRed    = ImageIO.read(wallsR[x]);
			Image tube       = ImageIO.read(tubes[x]);
			
			Tile wgTile = new Tile(wallGreen);
			Tile wyTile = new Tile(wallYellow);
			Tile wrTile = new Tile(wallRed);
			Tile tuTile = new Tile(tube);
			
			set.tiles.put("wall/green/"  + tileOrder2[x], wgTile);
			set.tiles.put("wall/yellow/" + tileOrder2[x], wyTile);
			set.tiles.put("wall/red/"    + tileOrder2[x], wrTile);
			set.tiles.put("tube/"        + tileOrder2[x], tuTile);
		}
		
		return set;
	}
	
	private Map<String, Tile> tiles;
	private Random rand;
	
	private int bulldozedCount;
	private int plainCount;
	
	public TileSet()
	{
		tiles = new HashMap<String, Tile>();
		rand = new Random();
		bulldozedCount = 0;
		plainCount = 0;
	}
	
	public int getTileSize()
	{
		return 32;
	}
	
	public Tile getTile(String code)
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
	
	public boolean isBulldozed(String tileCode)
	{
		return tileCode.contains("bulldozed");
	}
}
