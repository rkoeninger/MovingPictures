using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using MPSharp.Basics;

namespace MPSharp.Map
{
    public class MapGrid
    {
		private Grid<String> tileCodes;
        public CostMap CostMap { get; set; }
		public Region Bounds { get { return tileCodes.Bounds; } private set {} }

        public MapGrid(int w, int h)
        {
			tileCodes = new Grid<String>(w, h);
			CostMap = new CostMap(w, h);
        }

		public void SetTileCode(GridPos pos, String tileCode)
		{
			tileCodes[pos] = tileCode;
		}

		public String GetTileCode(GridPos pos)
		{
			return tileCodes[pos];
		}
    }
}
