using System;
using System.Drawing;

namespace MPSharp.Basics
{
	public class GridMetrics
	{
		public int SpotSize{ get; set; }

		public Point ToPoint(AbsPos pos)
		{
			return pos.ToPoint(SpotSize);
		}

		public AbsPos ToAbsPos(Point point)
		{
			return point.ToAbsPos(SpotSize);
		}
	}

	public static class PointExtensions
	{
		public static AbsPos ToAbsPos(this Point point, int spotSize)
		{
			return new AbsPos(point.X / (float) spotSize, point.Y / (float) spotSize);
		}
	}
}
