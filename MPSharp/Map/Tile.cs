using System;
using System.Collections.Generic;
using System.Drawing;

namespace MPSharp.Map
{
    public struct Tile
    {
        public Bitmap Bitmap {get; set;}

        public Tile(Bitmap bitmap) : this()
        {
            Bitmap = bitmap;
        }
    }
}
