using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using MPSharp.Basics;

namespace MPSharp.Map
{
    public struct MapSpot
    {
        public String TileCode { get; set; }
    }

    public class MapGrid : Grid<MapSpot>
    {
        public CostMap CostMap { get; set; }

        public MapGrid(int w, int h) : base(w, h)
        {

        }
    }
}
