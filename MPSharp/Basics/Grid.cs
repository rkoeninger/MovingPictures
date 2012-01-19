using System;
using System.Collections;
using System.Collections.Generic;

namespace MPSharp.Basics
{
	// TODO: Subgrids with shared memory

    public class Grid<T> : IEnumerable<T>
	{
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

		#region Contains, Find

        public bool Contains(Predicate<T> predicate)
        {
            for (int x = 0; x < Width;  ++x)
            for (int y = 0; y < Height; ++y)
                if (predicate(cells[x,y]))
                    return true;

            return false;
        }

		public List<GridPos> Find(Predicate<T> predicate)
		{
			List<GridPos> results = new List<GridPos>((Width + Height) / 2);

            for (int x = 0; x < Width;  ++x)
            for (int y = 0; y < Height; ++y)
                if (predicate(cells[x,y]))
					results.Add(new GridPos(x, y));

			return results;
		}

        public bool TryFindClosest(Predicate<T> predicate, GridPos pos, out GridPos result)
        {
            return TryFindClosest(predicate, pos, out result, Bounds);
        }

        // TODO: implement limitation of Region (should be just RectRegion or generic Region?)

		public bool TryFindClosest(
			Predicate<T> predicate,
			GridPos pos,
            out GridPos result,
			Region region,
			float minDistance = 0,
			float maxDistance = Single.PositiveInfinity)
		{
			int iteration = (int) (minDistance * 1.414);
			int maxIteration =
				Double.IsPositiveInfinity(maxDistance) || maxDistance > Int32.MaxValue
				? -1
				: (int) maxDistance;
		    
			if (iteration == 0)
			{
				if (predicate(this[pos]))
				{
                    result = pos;
                    return true;
                }
			    
				iteration = 1;
			}
		    
			double closestDistance = Double.PositiveInfinity;
			GridPos? closestPos = null;
		
			int startX, stopX, startY, stopY;
			bool nEdge, sEdge, eEdge, wEdge;

			// TODO: comment algorithm
			do
			{
				wEdge = pos.X - iteration >= 0;
				startX = Math.Max(pos.X - iteration, 0);

				nEdge = pos.Y - iteration >= 0;
				startY = Math.Max(pos.Y - iteration, 0);
			
				eEdge = pos.X + iteration < Width;
				stopX = Math.Min(pos.X + iteration, Width - 1);

				sEdge = pos.Y + iteration < Height;
				stopY = Math.Min(pos.Y + iteration, Height - 1);
			
				if (nEdge || sEdge)
					for (int x = startX; x <= stopX; ++x)
					{
						if (nEdge && predicate(this[x, startY]))
						{
							double distSq = DistanceSq(pos.X, pos.Y, x, startY);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new GridPos(x, startY);
							}
						}
					
						if (sEdge && predicate(this[x, stopY]))
						{
							double distSq = DistanceSq(pos.X, pos.Y, x, stopY);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new GridPos(x, stopY);
							}
						}
					}
			
				if (nEdge) startY += 1;
				if (sEdge) stopY -= 1;
			
				if (wEdge || eEdge)
					for (int y = startY; y <= stopY; ++y) 
					{
						if (wEdge && predicate(this[startX, y]))
						{
							double distSq = DistanceSq(pos.X, pos.Y, startX, y);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new GridPos(startX, y);
							}
						}
					
						if (eEdge && predicate(this[stopX, y]))
						{
							double distSq = DistanceSq(pos.X, pos.Y, stopX, y);
							if (distSq < closestDistance)
							{
								closestDistance = distSq;
								closestPos = new GridPos(stopX, y);
							}
						}
					}

				if (closestPos.HasValue && maxIteration == -1)
				{
					double dy = Math.Abs(pos.Y - closestPos.Value.Y);
					double dx = Math.Abs(pos.X - closestPos.Value.X);
					maxIteration = (int) Math.Sqrt(dx*dx + dy*dy);
				}
			
				bool allOutOfBounds = !(nEdge || sEdge || wEdge || eEdge);
				bool finalIteration = maxIteration != -1
								   && iteration >= maxIteration;
			
				if (finalIteration || allOutOfBounds)
				{
					if (!closestPos.HasValue)
                    {
                        result = new GridPos();
                        return false;
                    }
				
					double distance = closestPos.Value.GetDistance(pos);
					
                    if (distance <= maxDistance && distance >= minDistance)
                    {
                        result = closestPos.Value;
                        return true;
                    }
                    else
                    {
                        result = new GridPos();
                        return false;
                    }
				}

				iteration++;
			}
			while (nEdge || sEdge || wEdge || eEdge);

			if (!closestPos.HasValue)
            {
                result = new GridPos();
				return false;
            }
		
			double distance2 = closestPos.Value.GetDistance(pos);
			
            if (distance2 <= maxDistance && distance2 >= minDistance)
            {
                result = closestPos.Value;
                return true;
            }
            else
            {
                result = new GridPos();
                return false;
            }
		}

        private double DistanceSq(int x1, int y1, int x2, int y2)
        {
            int dx = x1 - x2;
            int dy = y1 - y2;
            return (dx * dx) + (dy * dy);
        }

		#endregion

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
