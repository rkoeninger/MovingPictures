using System;
using System.Collections.Generic;
using System.Drawing;

namespace MPSharp.Basics
{
	public struct AbsPos : IEquatable<AbsPos>
	{
		private readonly float x;
		private readonly float y;

		public float X{ get{ return x; } }
		public float Y{ get{ return y; } }

		public AbsPos(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public override int GetHashCode()
		{
			return (x.GetHashCode() * 3571) ^ (y.GetHashCode() * 181081);
		}

		public override bool Equals(Object obj)
		{
			return (obj is AbsPos) ? Equals((AbsPos) obj) : false;
		}

		public bool Equals(AbsPos pos)
		{
			return x == pos.x && y == pos.y;
		}
		
		public static bool operator == (AbsPos a, AbsPos b)
		{
			return a.Equals(b);
		}

		public static bool operator != (AbsPos a, AbsPos b)
		{
			return ! a.Equals(b);
		}

		public float GetDistance(AbsPos pos)
		{
			float dx = x - pos.x;
			float dy = y - pos.y;
			return (float) Math.Sqrt(dx * dx + dy * dy);
		}
		
		public float GetAngle(AbsPos pos)
		{
			float dx = x - pos.x;
			float dy = y - pos.y;
			return ((float) -Math.Atan2(dy, dx)).NormalizeRevAngle();
		}

		public static AbsPos operator + (AbsPos a, AbsPos b)
		{
			return new AbsPos(a.x + a.y, b.x + b.y);
		}

		public static AbsPos operator - (AbsPos a, AbsPos b)
		{
			return new AbsPos(a.x - a.y, b.x - b.y);
		}

		public static AbsPos operator - (AbsPos pos)
		{
			return new AbsPos(-pos.x, -pos.y);
		}
		
		public GridPos ToGridPos()
		{
			return new GridPos((int) Math.Round(x), (int) Math.Round(y));
		}

		public override String ToString()
		{
			return String.Format("({0:0.00}, {1:0.00})", x, y);
		}
	}
}
