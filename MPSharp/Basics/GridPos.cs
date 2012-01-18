using System;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public struct GridPos : IEquatable<GridPos>
	{
		private readonly int x;
		private readonly int y;

		public int X{ get{ return x; } }
		public int Y{ get{ return y; } }

		public GridPos(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public IEnumerable<GridPos> Get4Neighbors()
		{
			return Get4Neighbors(Direction.E);
		}

		public IEnumerable<GridPos> Get4Neighbors(Direction start, bool cw = false)
		{
			if (!start.HasCardinality(Direction.Cardinality.Fourth))
				throw new ArgumentException("Must be 4th turn", "start");

			IEnumerable<Direction> fourWays = Direction.GetEnumerator(start, cw ? -4 : 4);

			foreach (Direction dir in fourWays)
				yield return dir.Apply(this);
		}
		
		public IEnumerable<GridPos> Get8Neighbors()
		{
			return Get8Neighbors(Direction.E);
		}

		public IEnumerable<GridPos> Get8Neighbors(Direction start, bool cw = false)
		{
			if (!start.HasCardinality(Direction.Cardinality.Eighth))
				throw new ArgumentException("Must be 4th or 8th turn", "start");

			IEnumerable<Direction> eightWays = Direction.GetEnumerator(start, cw ? -2 : 2);

			foreach (Direction dir in eightWays)
				yield return dir.Apply(this);
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
		
		public float GetAngle(GridPos pos)
		{
			float dx = x - pos.x;
			float dy = y - pos.y;
			return ((float) -Math.Atan2(dy, dx)).NormalizeRevAngle();
		}

        public GridPos Shift(int dx, int dy)
        {
            return new GridPos(x + dx, y + dy);
        }

		public GridPos Shift(Direction dir)
		{
			return new GridPos(x + dir.DX, y + dir.DY);
		}

        public GridPos Project(Direction dir, int distance)
        {
            return new GridPos(x + dir.DX * distance, y + dir.DY * distance);
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
		
		public AbsPos ToCornerAbsPos()
		{
			return new AbsPos(x, y);
		}

		public AbsPos ToCenterAbsPos()
		{
			return new AbsPos(x + 0.5f, y + 0.5f);
		}

		public override String ToString()
		{
			return String.Format("({0}, {1})", x, y);
		}
	}
}
