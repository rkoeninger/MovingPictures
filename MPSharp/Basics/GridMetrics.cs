using System;
using System.Drawing;

namespace MPSharp.Basics
{
	public class GridMetrics
	{
		private readonly int rootSpotSize;

		public int SpotSize
		{
			get{ return _spotSize; }
			set
			{
				_scale = value.Log2();
				_spotSize = value;
			}
		}
		private int _spotSize;

		public int Scale
		{
			get{ return _scale; }
			set
			{
				_spotSize = value.Pow2();
				_scale = value;
			}
		}
		private int _scale;

		public int XOffset{ get; set; }
		public int YOffset{ get; set; }

		public GridMetrics(int rootSpotSize)
		{
			if (rootSpotSize.IsPowerOfTwo())
				throw new ArgumentException("Must be a power of 2", "rootSpotSize");

			this.rootSpotSize = rootSpotSize;
			SpotSize = rootSpotSize;
			XOffset = 0;
			YOffset = 0;
		}

		public Point ToPoint(AbsPos pos)
		{
			int x = XOffset + (int) (pos.X * SpotSize);
			int y = YOffset + (int) (pos.Y * SpotSize);
			return new Point(x, y);
		}

		public AbsPos ToAbsPos(Point point)
		{
			float x = (point.X - XOffset) / (float) SpotSize;
			float y = (point.Y - YOffset) / (float) SpotSize;
			return new AbsPos(x, y);
		}
	}
}
