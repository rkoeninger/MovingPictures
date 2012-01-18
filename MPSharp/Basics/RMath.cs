using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace MPSharp.Basics
{
	/// <summary>
	/// Extensions for primitives.
	/// </summary>
	public static class RMath
	{
		public static float NormalizeRevAngle(this float f)
		{
			return (f % 1.0f + 1.0f) % 1.0f;
		}
		
        public static int RevsToNSteps(this float f, int n)
        {
            return (((int) Math.Round(f.NormalizeRevAngle() * n)) % n + n) % n;
        }

		public static bool IsPowerOfTwo(this int i)
		{
			return (i > 0) && ((i & (i - 1)) == 0);
		}

		public static bool IsInRange(this int i, int min, int max)
		{
			return (min <= i) && (max >= i);
		}

		public static int Pow2(this int i)
		{
			return i >= 0 ? 1 << i : 1 >> -i;
		}

		public static int Log2(this int i)
		{
			if (((i & (i - 1)) != 0) || (i <= 0))
				throw new ArithmeticException(i + " is not a power of 2");

			for (int log = 0; true; log++, i >>= 1)
				if (i == 1)
					return log;
		}
	}
}
