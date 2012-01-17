using System;
using System.Collections.Generic;

namespace MPSharp.Basics
{
    // grid[1, 2].IsNull()
    // grid[2, 3] == 5
    // grid[3, 4].Apply(Transform)
    // T t = grid[4, 5]

    public class Grid<T>
    {
        private T[,] cells;

        public Grid(int w, int h)
        {
            cells = new T[w, h];
        }

        public Grid(int w, int h, T initValue)
        {
            cells = new T[w, h];

            for (int x = 0; x < w; ++x)
            for (int y = 0; y < h; ++y)
                cells[x, y] = initValue;
        }

        public T this[GridPos pos]
        {
            get{ return cells[pos.X, pos.Y];  }
            set{ cells[pos.X, pos.Y] = value; }
        }

        public T this[int x, int y]
        {
            get{ return cells[x, y];  }
            set{ cells[x, y] = value; }
        }

        // public GridPos Find(T val)
        // public GridPos FindLast(T val)
        // FindAll
        // FindClosest
        // Copy/Clone
        // Iterator
        // Apply(Predicate, Transform)
        // public bool Contains(T val)
        // public void Fill(T val)
        // public void Fill(Region reg, T val)
    }
}
