import java.awt.Point;
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
		
		while ((line = reader.readLine()) != null)
		{
			String[] lineParts = line.split(" ");
			
			Tile tile =	new Tile(
				ImageIO.read(new File(file.getParentFile(), lineParts[3])),//path
				Integer.parseInt(lineParts[2])//default family
			);
			
			tile.setGroup(group);
			
			group.tiles.put(
				new Point(
					Integer.parseInt(lineParts[0]),//relative x
					Integer.parseInt(lineParts[1])//relative y
				),
				tile
			);
		}
		
		return group;
	}
	
	private Map<Point, Tile> tiles;
	
	public TileGroup()
	{
		tiles = new HashMap<Point, Tile>();
	}
	
	public Tile getTile(Point p)
	{
		return getTile(p.x, p.y);
	}
	
	public Tile getTile(int x, int y)
	{
		return null;
	}
}
