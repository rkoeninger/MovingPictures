using System;
using System.Collections.Generic;
using System.Reflection;

namespace MPSharp.Basics
{
	public struct Direction : IEquatable<Direction>
	{
		public enum Enum
		{
			E = 0, ENE, NE, NNE, N, NNW, NW, WNW, W, WSW, SW, SSW, S, SSE, SE, ESE
		}

		[Flags]
		public enum Cardinality
		{
			Fourth = 1, Eighth = 2, Sixteenth = 4
		}
		
		private static readonly Direction[] list = new Direction[16];
		private static readonly Dictionary<String,Direction> dict = new Dictionary<String,Direction>(16);

		public static readonly Direction E   = new Direction( 0, +1,  0, 0.0000f, "e",   "East");
		public static readonly Direction ENE = new Direction( 1,  0,  0, 0.0625f, "ene", "East-North-East");
		public static readonly Direction NE  = new Direction( 2, +1, +1, 0.1250f, "ne",  "North-East");
		public static readonly Direction NNE = new Direction( 3,  0,  0, 0.1875f, "nne", "North-North-East");
		public static readonly Direction N   = new Direction( 4,  0, -1, 0.2500f, "n",   "North");
		public static readonly Direction NNW = new Direction( 5,  0,  0, 0.3125f, "nnw", "North-North-West");
		public static readonly Direction NW  = new Direction( 6, -1, -1, 0.3750f, "nw",  "North-West");
		public static readonly Direction WNW = new Direction( 7,  0,  0, 0.4375f, "wnw", "West-North-West");
		public static readonly Direction W   = new Direction( 8, -1,  0, 0.5000f, "w",   "West");
		public static readonly Direction WSW = new Direction( 9,  0,  0, 0.5625f, "wsw", "West-South-West");
		public static readonly Direction SW  = new Direction(10, -1, +1, 0.6250f, "sw",  "South-West");
		public static readonly Direction SSW = new Direction(11,  0,  0, 0.6875f, "ssw", "South-South-West");
		public static readonly Direction S   = new Direction(12,  0, +1, 0.7500f, "s",   "South");
		public static readonly Direction SSE = new Direction(13,  0,  0, 0.8125f, "sse", "South-South-East");
		public static readonly Direction SE  = new Direction(14, +1, +1, 0.8750f, "se",  "South-East");
		public static readonly Direction ESE = new Direction(15,  0,  0, 0.9375f, "ese", "East-South-East");

		public int Ordinal         { get; private set; }
		public int DX              { get; private set; }
		public int DY              { get; private set; }
		public Cardinality Cardinal{ get; private set; }
		public float Angle         { get; private set; }
		public float RadianAngle   { get; private set; }
		public float DegreeAngle   { get; private set; }
		public float Cos           { get; private set; }
		public float Sin           { get; private set; }
		public float Tan           { get; private set; }
		public String Name         { get; private set; }
		public String FullName     { get; private set; }

		private Direction(int ordinal, int dx, int dy, float angle, String name, String fullName) : this()
		{
			Ordinal = ordinal;
			DX = dx;
			DY = dy;
			Cardinal = Cardinality.Sixteenth;
			if (((int) Math.Round(angle * 16)) % 2 == 0) Cardinal |= Cardinality.Eighth;
			if (((int) Math.Round(angle * 16)) % 4 == 0) Cardinal |= Cardinality.Fourth;
			Angle = angle;
			RadianAngle = (float) (angle * 2.0 * Math.PI);
			DegreeAngle = (float) (angle * 360.0);
			Cos = (float) +Math.Cos(RadianAngle);
			Sin = (float) -Math.Sin(RadianAngle);
			Tan = (float) -Math.Tan(RadianAngle);
			Name = name;
			FullName = fullName;
			list[ordinal] = this;
			dict.Add(Name, this);
		}

		public static Direction ForEnumValue(Direction.Enum enumValue)
		{
			return list[(int) enumValue];
		}

		public Direction.Enum GetEnumValue()
		{
			return (Direction.Enum) Ordinal;
		}

		public override int GetHashCode()
		{
			return base.GetHashCode();
		}

		public override bool Equals(Object obj)
		{
			return base.Equals(obj);
		}

		public bool Equals(Direction dir)
		{
			return base.Equals(dir);
		}

		public static bool operator == (Direction a, Direction b)
		{
			return a.Equals(b);
		}

		public static bool operator != (Direction a, Direction b)
		{
			return !a.Equals(b);
		}

		/// <summary>Determines if this direction falls under the given order flag.</summary>
		/// <remarks>By default, a fourth turn counts as an eigth turn,
		/// but an eigth turn might not count as a fouth turn</remarks>
		public bool IsCardinality(Cardinality cardinal, bool exclusively = false)
		{
			return exclusively ? (cardinal | Cardinal) == cardinal : (cardinal | Cardinal) != 0;
		}

		public GridPos Apply(GridPos pos)
		{
			if (DX == 0 && DY == 0)
				throw new NotSupportedException("Direction must be 8th-turn to apply to GridPos");

			return new GridPos(pos.X + DX, pos.Y + DY);
		}

		public Direction Reverse(Direction dir)
		{
			return list[(dir.Ordinal + list.Length) % list.Length];
		}

		public Direction Rotate(int steps)
		{
			return list[((Ordinal + steps) % list.Length + list.Length) % list.Length];
		}

		public static Direction ForAngle(float angle)
		{
			return list[(int) (angle.NormalizeRevAngle() * 16)];
		}

		public static Direction ForAngle(float angle, int precision)
		{
			if (precision != 4 && precision != 8 && precision != 16)
				throw new ArgumentException("Must be one of: 4, 8, 16", "precision");

			return list[(int) Math.Round(angle.NormalizeRevAngle() * precision) * (16 / precision)];
		}

		public static Direction ForOffset(int dx, int dy)
		{
			return ForAngle((float)-Math.Atan2(dy, dx));
		}

		public static Direction ForName(String name)
		{
			try
			{
				return dict[name];
			}
			catch (KeyNotFoundException)
			{
				throw new ArgumentException("does not represent an existing Direction", "name");
			}
		}

		public override String ToString()
		{
			return Name;
		}

		public static IEnumerable<Direction> GetEnumerator()
		{
			return GetEnumerator(E, 1);
		}

		public static IEnumerable<Direction> GetEnumerator(Direction start)
		{
			return GetEnumerator(start, 1);
		}
		
		public static IEnumerable<Direction> GetEnumerator(Direction start, int increment)
		{
			if (!Math.Abs(increment).IsPowerOfTwo() || !Math.Abs(increment).IsInRange(1, 16))
				throw new ArgumentException("Magnitude must be a power of two and in the range [1, 16]", "increment");

			int count = 0;

			for (Direction current = start;
				current != start || count == 0;
				current = current.Rotate(increment), count++)
				yield return current;
		}
	}
}
