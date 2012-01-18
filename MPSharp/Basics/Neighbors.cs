using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace MPSharp.Basics
{
	/// <summary>
	/// Idenitifies a set of neighbors, so NE refers to having neighbors
	/// both North and East, not a neighbor to the North-East.
	/// </summary>
	[Flags]
	public enum Neighbors
	{
		NONE = 0,
		N = 1,
		S = 2,
		E = 4,
		W = 8,
		ALL = 15
	}

	#region Extension Methods

	public static class NeighborsExtensions
	{
		public static Direction ToDirection(this Neighbors n)
		{
			switch (n)
			{
			case Neighbors.N : return Direction.N;
			case Neighbors.S : return Direction.S;
			case Neighbors.E : return Direction.E;
			case Neighbors.W : return Direction.W;
			}

			throw new NotSupportedException("Only single neighbor can refer to a direction");
		}
	}

	#endregion
}
