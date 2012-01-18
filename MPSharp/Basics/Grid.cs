using System;
using System.Collections;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	// TODO: Subgrids with shared memory

    public class Grid<T> : IEnumerable<T>
	{
		public delegate T Transform(T val);
		
		#region Members and Constructors

		private Cell[,] cells;

		public int Width    { get; private set; }
		public int Height   { get; private set; }
		public Region Bounds{ get; private set; }

        public Grid(int w, int h)
        {
			if (w < 1 || h < 1)
				throw new ArgumentException("Grid area must be greater than zero");

            cells = new Cell[w,h];
			Width = w;
			Height = h;
			Bounds = new RectRegion(w, h);
        }

        public Grid(int w, int h, T val) : this(w, h)
        {
			Fill(val);
        }

		#endregion

		#region Accessors, Fill, Apply

		public Cell this[GridPos pos]
        {
            get{ return cells[pos.X,pos.Y];  }
            set{ cells[pos.X,pos.Y] = value; }
        }

        public Cell this[int x, int y]
        {
            get{ return cells[x,y];  }
            set{ cells[x,y] = value; }
        }

		public void Fill(T val)
		{
            for (int x = 0; x < Width; ++x)
            for (int y = 0; y < Height; ++y)
                cells[x,y] = val;
		}

		public void Fill(Region region, T val)
		{
			foreach (GridPos pos in region)
				this[pos] = val;
		}

		public void Apply(Func<T,T> transform)
		{
            for (int x = 0; x < Width; ++x)
            for (int y = 0; y < Height; ++y)
				cells[x,y].Apply(transform);
		}

		public void Apply(Predicate<T> predicate, Func<T,T> transform)
		{
            for (int x = 0; x < Width; ++x)
            for (int y = 0; y < Height; ++y)
				cells[x,y].Apply(predicate, transform);
		}

		#endregion

		#region Find

		public List<GridPos> Find(Predicate<T> predicate)
		{
			List<GridPos> results = new List<GridPos>((Width + Height) / 2);

            for (int x = 0; x < Width; ++x)
            for (int y = 0; y < Height; ++y)
                if (predicate(cells[x,y]))
					results.Add(new GridPos(x, y));

			return results;
		}

		// TODO: Add conveinence methods for TryFindClosest

		public bool TryFindClosest(
			Predicate<T> predicate,
			out GridPos pos,
			Region region,
			float minDistance = 0,
			float maxDistance = Single.PositiveInfinity)
		{


			/*
			int iteration = (int) (minDistance * 1.414);
			int maxIteration =
				Double.isInfinite(maxDistance) || maxDistance > Integer.MAX_VALUE
				? -1
				: (int) maxDistance;
		
			if (iteration == 0)
			{
				if (cond.accept(get(pos)))
					return pos;
			
				iteration = 1;
			}
		
			double closestDistance = Double.POSITIVE_INFINITY;
			Position closestPos = null;
		
			int startX, stopX, startY, stopY;
			boolean nEdge, sEdge, eEdge, wEdge;

			// TODO: comment algorithm
			do
			{
				wEdge = pos.x - iteration >= 0;
				startX = Math.max(pos.x - iteration, 0);

				nEdge = pos.y - iteration >= 0;
				startY = Math.max(pos.y - iteration, 0);
			
				eEdge = pos.x + iteration < w;
				stopX = Math.min(pos.x + iteration, w - 1);

				sEdge = pos.y + iteration < h;
				stopY = Math.min(pos.y + iteration, h - 1);
			
				if (nEdge || sEdge)
					for (int x = startX; x <= stopX; ++x)
					{
						if (nEdge && cond.accept(get(x, startY)))
						{
							double distSq = distanceSq(pos.x, pos.y, x, startY);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new Position(x, startY);
							}
						}
					
						if (sEdge && cond.accept(get(x, stopY)))
						{
							double distSq = distanceSq(pos.x, pos.y, x, stopY);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new Position(x, stopY);
							}
						}
					}
			
				if (nEdge) startY += 1;
				if (sEdge) stopY -= 1;
			
				if (wEdge || eEdge)
					for (int y = startY; y <= stopY; ++y) 
					{
						if (wEdge && cond.accept(get(startX, y)))
						{
							double distSq = distanceSq(pos.x, pos.y, startX, y);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new Position(startX, y);
							}
						}
					
						if (eEdge && cond.accept(get(stopX, y)))
						{
							double distSq = distanceSq(pos.x, pos.y, stopX, y);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new Position(stopX, y);
							}
						}
					}

				if (closestPos != null && maxIteration == -1)
				{
					double dy = Math.abs(pos.y - closestPos.y);
					double dx = Math.abs(pos.x - closestPos.x);
					maxIteration = (int) Math.sqrt(dx*dx + dy*dy);
				}
			
				boolean allOutOfBounds = !(nEdge || sEdge || wEdge || eEdge);
				boolean finalIteration = maxIteration != -1
									  && iteration >= maxIteration;
			
				if (finalIteration || allOutOfBounds)
				{
					if (closestPos == null)
						return null;
				
					double distance = closestPos.getDistance(pos);
					return distance <= maxDistance && distance >= minDistance
						? closestPos
						: null;
				}

				iteration++;
			}
			while (nEdge || sEdge || wEdge || eEdge);

			if (closestPos == null)
				return null;
		
			double distance = closestPos.getDistance(pos);
			return distance <= maxDistance && distance >= minDistance
				? closestPos
				: null;
			*/



			pos = new GridPos();
			return false;
		}

		#endregion

		// public GridPos FindLast(T val)
        // FindAll
        // FindClosest
		// public bool Contains(T val)

		#region Enumeration

		public IEnumerator<T> GetEnumerator()
		{
            for (int x = 0; x < Width; ++x)
            for (int y = 0; y < Height; ++y)
				yield return this[x,y];
		}
		
		public IEnumerator<T> GetEnumerator(Region region)
		{
			foreach (GridPos pos in region)
				yield return this[pos];
		}

        #region Boilerplate
        IEnumerator IEnumerable.GetEnumerator()
        {
            return GetEnumerator();
        }
        #endregion

		#endregion

		#region Cell class

		public struct Cell
		{
			public T Value{ get; set; }

			public static implicit operator T(Cell cell)
			{
				return cell.Value;
			}

			public static implicit operator Cell(T val)
			{
				Cell cell = new Cell();
				cell.Value = val;
				return cell;
			}

			public bool IsNull()
			{
				return Value == null;
			}
			
			public bool Apply(Predicate<T> pred)
			{
				return pred(Value);
			}

			public T Apply(Func<T,T> transform)
			{
				return Value = transform(Value);
			}

			public T Apply(Predicate<T> predicate, Func<T,T> transform)
			{
				return predicate(Value) ? transform(Value) : Value;
			}
		}

		#endregion
	}
}
