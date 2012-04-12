import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class TileGroup
{
	public static TileGroup load(File file) throws IOException
	{
		TileGroup group = new TileGroup();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String line = null;
		
		RectangleBuilder rb = new RectangleBuilder();
		
		while ((line = reader.readLine()) != null)
		{
			String[] lineParts = line.split(" ");
			
			Tile tile =	new Tile(
				ImageIO.read(new File(file.getParentFile(), lineParts[3])),//path
				Integer.parseInt(lineParts[2])//default family
			);
			
			tile.setGroup(group);
			
			Point p = 
				new Point(
					Integer.parseInt(lineParts[0]),//relative x
					Integer.parseInt(lineParts[1])//relative y
				);
			
			rb.add(p);
			
			group.tiles.put(p, tile);
		}
		
		group.r = rb.r;
		
		return group;
	}
	
	private Map<Point, Tile> tiles;
	
	private Rectangle r;
	
	public TileGroup()
	{
		tiles = new HashMap<Point, Tile>();
	}
	
	public Tile getTile(Point p)
	{
		return getTile(p.x, p.y);
	}
	
	public Point getPosition(Tile tile)
	{
		for (Map.Entry<Point, Tile> e : tiles.entrySet())
		{
			if (e.getValue().equals(tile))
				return e.getKey();
		}
		
		return null;
	}
	
	public Rectangle getRectangle()
	{
		return r;
	}
	
	public Tile getTile(int x, int y)
	{
		return tiles.get(new Point(x, y));
	}
}
