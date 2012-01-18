using System;
using System.Collections;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	public class Region : IEnumerable<GridPos>
	{
        public int X{get; private set;}
        public int Y{get; private set;}

        /// <summary>Enclosed area; number of GridPos returned by Enumerator.</summary>
        public int Area{get; private set;}

        public bool Contains(GridPos pos)
        {
            return false;
        }

        public IEnumerator<GridPos> GetEnumerator()
        {
            for (int i = 0; i < 5; ++i)
                yield return new GridPos(i, 4);
        }

        #region Boilerplate
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
        #endregion
    }
}
