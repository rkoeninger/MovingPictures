using System;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public class CostMap : Grid<float>
	{
		public CostMap(int w, int h) : base(w, h) {}

		public CostMap(int w, int h, float val) : base(w, h, val) {}
	}
}
