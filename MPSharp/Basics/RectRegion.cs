using System;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public class RectRegion : Region
	{
		public int Width { get; protected set; }
		public int Height{ get; protected set; }
        public int MaxX  { get; protected set; }
        public int MaxY  { get; protected set; }

		public RectRegion(int x, int y, int w, int h)
		{
			if (w < 0)
			{
				x += w;
				w = -w;
			}
		    
			if (h < 0)
			{
				y += h;
				h = -h;
			}
			
			X = x;
			Y = y;
            MaxX = x + w;
            MaxY = y + h;
			Width = w;
			Height = h;
			Area = w * h;
		}

		public RectRegion(int w, int h) : this(0, 0, w, h) {}

		public override Region Shift(int dx, int dy)
		{
			return new RectRegion(X + dx, Y + dy, Width, Height);
		}

		public override Region Move(int x, int y)
		{
			return new RectRegion(x, y, Width, Height);
		}

		public override bool Contains(GridPos pos)
		{
			return (pos.X >= X) && (pos.X < X + Width) && (pos.Y >= Y) && (pos.Y < Y + Height);
		}

		public override IEnumerator<GridPos> GetEnumerator()
		{
			for (int x = X; x < X + Width;  ++x)
			for (int y = Y; y < Y + Height; ++y)
				yield return new GridPos(x, y);
		}

        public AbsArea ToAbsArea()
        {
            return new AbsArea(X, Y, Width, Height);
        }
	}
}
