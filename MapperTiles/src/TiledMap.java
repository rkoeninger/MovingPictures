import java.awt.Point;
import java.awt.Rectangle;


public class TiledMap
{
	static class TileGroupInstance
	{
		static int nextSerial = 0;
		
		TileGroup g;
		int serial = nextSerial++;
		
		public TileGroupInstance(TileGroup g)
		{
			this.g = g;
		}
	}
	
	public static boolean validation = true;
	
	public final int w;
	public final int h;
	
	private Tile[][] tiles;
	
	@SuppressWarnings("unused")
	private TileGroupInstance[][] metadata;
	
	public TiledMap(int w, int h, TileSet ts, int defaultFamily)
	{
		tiles = new Tile[w][h];
		this.w = w;
		this.h = h;
		
		metadata = new TileGroupInstance[w][h];
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			tiles[x][y] = ts.getPlainTile(defaultFamily);
	}
	
	/**
	 * If this fails, then something has been set wrong.
	 */
	public void validate()
	{
		if (!validation)
			return;
		
		TiledMap map = this;
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
		{
			Tile prevTile = getTile(x, y);
			
			int familyNE = prevTile.familyNE;
			
			if (map.contains(x + 1, y - 1) && map.getTile(x + 1, y - 1).familySW != 0)
			{
				if (familyNE != map.getTile(x + 1, y - 1).familySW)
					throw new IllegalStateException();
			}
			if (map.contains(x + 0, y - 1) && map.getTile(x + 0, y - 1).familySE != 0)
			{
				if (familyNE != map.getTile(x + 0, y - 1).familySE)
					throw new IllegalStateException();
			}
			if (map.contains(x + 1, y + 0) && map.getTile(x + 1, y + 0).familyNW != 0)
			{
				if (familyNE != map.getTile(x + 1, y + 0).familyNW)
					throw new IllegalStateException();
			}
			
			int familyNW = prevTile.familyNW;
			
			if (map.contains(x - 1, y - 1) && map.getTile(x - 1, y - 1).familySE != 0)
			{
				if (familyNW != map.getTile(x - 1, y - 1).familySE)
					throw new IllegalStateException();
			}
			if (map.contains(x + 0, y - 1) && map.getTile(x + 0, y - 1).familySW != 0)
			{
				if (familyNW != map.getTile(x + 0, y - 1).familySW)
					throw new IllegalStateException();
			}
			if (map.contains(x - 1, y + 0) && map.getTile(x - 1, y + 0).familyNE != 0)
			{
				if (familyNW != map.getTile(x - 1, y + 0).familyNE)
					throw new IllegalStateException();
			}
			
			int familySW = prevTile.familySW;
			
			if (map.contains(x - 1, y + 1) && map.getTile(x - 1, y + 1).familyNE != 0)
			{
				if (familySW != map.getTile(x - 1, y + 1).familyNE)
					throw new IllegalStateException();
			}
			if (map.contains(x + 0, y + 1) && map.getTile(x + 0, y + 1).familyNW != 0)
			{
				if (familySW != map.getTile(x + 0, y + 1).familyNW)
					throw new IllegalStateException();
			}
			if (map.contains(x - 1, y + 0) && map.getTile(x - 1, y + 0).familySE != 0)
			{
				if (familySW != map.getTile(x - 1, y + 0).familySE)
					throw new IllegalStateException();
			}
			
			int familySE = prevTile.familySE;
			
			if (map.contains(x + 1, y + 1) && map.getTile(x + 1, y + 1).familyNW != 0)
			{
				if (familySE != map.getTile(x + 1, y + 1).familyNW)
					throw new IllegalStateException();
			}
			if (map.contains(x + 0, y + 1) && map.getTile(x + 0, y + 1).familyNE != 0)
			{
				if (familySE != map.getTile(x + 0, y + 1).familyNE)
					throw new IllegalStateException();
			}
			if (map.contains(x + 1, y + 0) && map.getTile(x + 1, y + 0).familySW != 0)
			{
				if (familySE != map.getTile(x + 1, y + 0).familySW)
					throw new IllegalStateException();
			}
			
			// TODO: map is also invalid if a hanging part of a group is left behind
		}
	}
	
	public boolean contains(int x, int y)
	{
		return x >= 0 && x < w && y >= 0 && y < h;
	}
	
	public boolean contains(Point p)
	{
		return contains(p.x, p.y);
	}
	
	public Rectangle setTile(int x, int y, Tile tile)
	{
		RectangleBuilder rb = new RectangleBuilder();
		rb.add(new Point(x, y));
		
		Tile prev = tiles[x][y];
		
		if (prev.isGroupMember())
		{
			TileGroup group = prev.getGroup();
			Rectangle groupArea = group.getRectangle();
			Point groupPos = prev.getGroupPosition();
			
			// Right now, TileGroups are limited to being rectangular in shape
			groupArea = new Rectangle(
				groupArea.x + x - groupPos.x,
				groupArea.y + y - groupPos.y,
				groupArea.width,
				groupArea.height
			);
			
			for (int xx = groupArea.x; xx < groupArea.x + groupArea.width;  ++xx)
			for (int yy = groupArea.y; yy < groupArea.y + groupArea.height; ++yy)
			{
				if (getTile(xx, yy).isGroupMember())
				{
					tiles[xx][yy] = MapperTiles.ts.getPlainTile(prev.getFamily());
					rb.add(new Point(xx, yy));
				}
			}
		}
		
		tiles[x][y] = tile;
		
		return rb.r;
	}
	
	public Rectangle setTile(Point p, Tile tile)
	{
		return setTile(p.x, p.y, tile);
	}
	
	public Tile getTile(int x, int y)
	{
		return tiles[x][y];
	}
	
	public Tile getTile(Point p)
	{
		return getTile(p.x, p.y);
	}
}
