using System;
using System.Collections;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public abstract class Region : IEnumerable<GridPos>
	{
		// Upper-left corner of anchor pos, usually upper-left corner
        public int X{get; protected set;}
        public int Y{get; protected set;}

        /// <summary>Enclosed area; number of GridPos returned by Enumerator.</summary>
        public int Area{get; protected set;}

		public abstract Region Move(int x, int y);

		public Region Move(GridPos pos)
		{
			return Move(pos.X, pos.Y);
		}

		public abstract Region Shift(int dx, int dy);

        public abstract bool Contains(GridPos pos);

        public abstract IEnumerator<GridPos> GetEnumerator();

        #region Boilerplate
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
        #endregion
    }
}
