using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;

namespace MPSharp.Map
{
	public class TileSet
	{
		public static TileSet Load(String file)
		{
			TileSet tileSet = new TileSet();

			Bitmap plain0 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0000.bmp"));
			Bitmap plain1 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0001.bmp"));
			Bitmap plain2 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0002.bmp"));
			Bitmap plain3 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0003.bmp"));
			Bitmap plain4 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0004.bmp"));
			Bitmap plain5 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0005.bmp"));
			Bitmap plain6 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0006.bmp"));
			Bitmap plain7 = (Bitmap) Bitmap.FromFile(Path.Combine(file, "plain\\0007.bmp"));

			tileSet["plain0"] = new Tile(plain0);
			tileSet["plain1"] = new Tile(plain1);
			tileSet["plain2"] = new Tile(plain2);
			tileSet["plain3"] = new Tile(plain3);
			tileSet["plain4"] = new Tile(plain4);
			tileSet["plain5"] = new Tile(plain5);
			tileSet["plain6"] = new Tile(plain6);
			tileSet["plain7"] = new Tile(plain7);

			return tileSet;
		}

		private Dictionary<String,Tile> tiles;

		private TileSet()
		{
			tiles = new Dictionary<String,Tile>();
		}

		public Tile this[String code]
		{
			get
			{
				return tiles[code];
			}
			private set
			{
				tiles[code] = value;
			}
		}
	}
}
