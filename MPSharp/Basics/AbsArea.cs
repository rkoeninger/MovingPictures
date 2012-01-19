using System;

namespace MPSharp.Basics
{
    public struct AbsArea
    {
        public float X     { get; private set; }
        public float Y     { get; private set; }
        public float Width { get; private set; }
        public float Height{ get; private set; }

        public AbsArea(float x, float y, float w, float h) : this()
        {
            if (w < 0)
			{
				x += w;
				w = -w;
			}
		    
			if (h < 0)
			{
				y += h;
				h = -h;
			}

            X = x;
            Y = y;
            Width = w;
            Height = h;
        }
    }
}
