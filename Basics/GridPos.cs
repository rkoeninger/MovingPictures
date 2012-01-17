using System;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public struct GridPos : IEquatable<GridPos>
	{
		private int x;
		private int y;

		public int X{ get{ return x; } }
		public int Y{ get{ return y; } }

		public GridPos(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public List<GridPos> Get4Neighbors()
		{
			List<GridPos> neighbors = new List<GridPos>(4);
			neighbors.Add(new GridPos(x + 1, y    ));
			neighbors.Add(new GridPos(x,     y - 1));
			neighbors.Add(new GridPos(x - 1, y    ));
			neighbors.Add(new GridPos(x,     y + 1));
			return neighbors;
		}

		public List<GridPos> Get8Neighbors()
		{
			List<GridPos> neighbors = new List<GridPos>(8);
			neighbors.Add(new GridPos(x + 1, y    ));
			neighbors.Add(new GridPos(x + 1, y - 1));
			neighbors.Add(new GridPos(x,     y - 1));
			neighbors.Add(new GridPos(x - 1, y - 1));
			neighbors.Add(new GridPos(x - 1, y    ));
			neighbors.Add(new GridPos(x - 1, y + 1));
			neighbors.Add(new GridPos(x,     y + 1));
			neighbors.Add(new GridPos(x + 1, y + 1));
			return neighbors;
		}

		public bool IsColinear(GridPos pos)
		{
			return x == pos.x || y == pos.y;
		}

		public bool IsHortizontal(GridPos pos)
		{
			return y == pos.y;
		}

		public bool IsVertical(GridPos pos)
		{
			return x == pos.x;
		}

		public bool Is4Neighbor(GridPos pos)
		{
			int dx = Math.Abs(x - pos.x);
			int dy = Math.Abs(x - pos.y);
			return (dx == 1) ^ (dy == 1);
		}

		public bool Is8Neighbor(GridPos pos)
		{
			int dx = Math.Abs(x - pos.x);
			int dy = Math.Abs(y - pos.y);
			return (dx == 1) || (dy == 1);
		}
		
		public override int GetHashCode()
		{
			return (x.GetHashCode() * 3571) ^ (y.GetHashCode() * 181081);
		}

		public override bool Equals(Object obj)
		{
			return (obj is GridPos) ? Equals((GridPos) obj) : false;
		}

		public bool Equals(GridPos pos)
		{
			return x == pos.x && y == pos.y;
		}
		
		public static bool operator == (GridPos a, GridPos b)
		{
			return a.Equals(b);
		}

		public static bool operator != (GridPos a, GridPos b)
		{
			return ! a.Equals(b);
		}

		private const float Sqrt2 = 1.4142135623730950488016887242097f;

		public float GetDistance(GridPos pos)
		{
			int dx = Math.Abs(x - pos.x);
			int dy = Math.Abs(y - pos.y);
			int max = Math.Max(dx, dy);
			int min = Math.Min(dx, dy);
			return (max - min) + min * Sqrt2;
		}
		
		public static GridPos operator + (GridPos a, GridPos b)
		{
			return new GridPos(a.x + a.y, b.x + b.y);
		}

		public static GridPos operator - (GridPos a, GridPos b)
		{
			return new GridPos(a.x - a.y, b.x - b.y);
		}

		public static GridPos operator - (GridPos pos)
		{
			return new GridPos(-pos.x, -pos.y);
		}
		
		public AbsPos ToAbsPosCorner()
		{
			return new AbsPos(x, y);
		}

		public AbsPos ToAbsPosCentered()
		{
			return new AbsPos(x + 0.5f, y + 0.5f);
		}

		public override String ToString()
		{
			return String.Format("({0}, {1})", x, y);
		}
	}
}
