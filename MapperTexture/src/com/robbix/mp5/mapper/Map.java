package com.robbix.mp5.mapper;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.LinkedList;

public class Map
{
	private static final int MAP_VERSION = 1;
	
	public static Map load(File mapFile) throws IOException
	{
		RandomAccessFile in = new RandomAccessFile(mapFile, "r");
		
		int mapVersion = in.readInt();
		
		if (mapVersion != MAP_VERSION)
			throw new FileFormatException("Incorrect version: " + mapVersion + ", expected: " + MAP_VERSION);
		
		int w = in.readByte();
		int h = in.readByte();
		
		Map map = new Map(w, h);
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
		{
			int fixtureCode = in.readByte();
			double cost = in.readFloat();
			String tileCode = in.readUTF();
			
			if (fixtureCode >= 0)
				map.putFixture(x, y, Map.Fixture.values()[fixtureCode]);
			
			map.setCost(x, y, cost);
			map.setTileCode(x, y, tileCode);
		}
		
		in.close();
		
		return map;
	}
	
	public void save(File mapFile) throws IOException
	{
		if (mapFile.exists())
			mapFile.createNewFile();
		
		RandomAccessFile out = new RandomAccessFile(mapFile, "rw");
		
		out.writeInt(MAP_VERSION);
		out.writeByte(w);
		out.writeByte(h);
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
		{
			out.writeByte(fixtures[x][y] == null ? -1 : fixtures[x][y].ordinal());
			out.writeFloat((float) costs[x][y]);
			out.writeUTF(tileCodes[x][y] == null ? "" : tileCodes[x][y]);
		}
		
		out.close();
	}
	
	public static enum Fixture{WALL, TUBE}
	
	private Fixture[][] fixtures;
	private double[][] costs;
	private String[][] tileCodes;
	
	private Collection<ResourceMarker> res;
	
	public final int w, h;
	
	public Map(int w, int h)
	{
		this.w = w;
		this.h = h;
		fixtures = new Fixture[w][h];
		costs = new double[w][h];
		tileCodes = new String[w][h];
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			tileCodes[x][y] = "";
		
		res = new LinkedList<ResourceMarker>();
	}
	
	public void putResourceMarker(int x, int y, ResourceMarker.Type type)
	{
		res.add(new ResourceMarker(type, new Point(x, y)));
	}
	
	public Collection<ResourceMarker> getResourceMarkers()
	{
		return res;
	}
	
	public String getTileCode(int x, int y)
	{
		return tileCodes[x][y];
	}
	
	public void setTileCode(int x, int y, String tileCode)
	{
		tileCodes[x][y] = tileCode;
	}
	
	public void setCost(int x, int y, double cost)
	{
		if (cost < 0.0)
			throw new IllegalArgumentException(String.valueOf(cost));
		
		costs[x][y] = cost;
	}
	
	public void setScaleFactor(int x, int y, double scale)
	{
		if (scale < 0.0 || scale > 1.0)
			throw new IllegalArgumentException(String.valueOf(scale));
		
		costs[x][y] = (1.0 / scale) - 1.0;
	}
	
	public double getCost(int x, int y)
	{
		return costs[x][y];
	}
	
	public double getScaleFactor(int x, int y)
	{
		return 1.0 / (costs[x][y] + 1.0);
	}
	
	public boolean hasWall(int x, int y)
	{
		return fixtures[x][y] == Fixture.WALL;
	}
	
	public boolean hasTube(int x, int y)
	{
		return fixtures[x][y] == Fixture.TUBE;
	}
	
	public boolean hasFixture(int x, int y)
	{
		return fixtures[x][y] != null;
	}
	
	public Fixture getFixture(int x, int y)
	{
		return fixtures[x][y];
	}
	
	public void putWall(int x, int y)
	{
		fixtures[x][y] = Fixture.WALL;
	}
	
	public void putTube(int x, int y)
	{
		fixtures[x][y] = Fixture.TUBE;
	}
	
	public void putFixture(int x, int y, Fixture fixture)
	{
		fixtures[x][y] = fixture;
	}
	
	public boolean contains(int x, int y)
	{
		return x >= 0
			&& x < w
			&& y >= 0
			&& y < h;
	}
	
	public void darker(int x, int y, double darkness)
	{
		if (!contains(x, y)) return;
		
		if (darkness > 1 || darkness < 0)
			throw new IllegalArgumentException(String.valueOf(darkness));
		
		double factor = getScaleFactor(x, y);
		factor *= darkness;
		
		if (factor < 0.05) factor = 0;
		
		setScaleFactor(x, y, factor);
	}
	
	public void lighter(int x, int y, double lightness)
	{
		if (!contains(x, y)) return;

		if (lightness < 1)
			throw new IllegalArgumentException(String.valueOf(lightness));
		
		double factor = getScaleFactor(x, y);
		
		if (factor == 0) factor = 0.05;
		
		factor *= lightness;
		
		if (factor > 1) factor = 1;
		
		setScaleFactor(x, y, factor);
	}
	
	public void black(int x, int y)
	{
		setScaleFactor(x, y, 0);
	}

	public void white(int x, int y)
	{
		setScaleFactor(x, y, 1);
	}
	
	public void rotateFixture(int x, int y, boolean up)
	{
		Map.Fixture fixture = getFixture(x, y);
	
		if (up) // null -> wall -> tube -> null
		{
			if      (fixture == null)             fixture = Map.Fixture.WALL;
			else if (fixture == Map.Fixture.WALL) fixture = Map.Fixture.TUBE;
			else if (fixture == Map.Fixture.TUBE) fixture = null;
		}
		else // null -> tube -> wall -> null
		{
			if      (fixture == null)             fixture = Map.Fixture.TUBE;
			else if (fixture == Map.Fixture.TUBE) fixture = Map.Fixture.WALL;
			else if (fixture == Map.Fixture.WALL) fixture = null;
		}
		
		putFixture(x, y, fixture);
	}
}
