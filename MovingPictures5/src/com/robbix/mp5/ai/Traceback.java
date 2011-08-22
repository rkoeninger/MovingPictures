package com.robbix.mp5.ai;

import java.util.LinkedList;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;

public class Traceback implements Pathfinder
{
	// TODO: start is ignored, does entire map
	//       could possibly just go until start position(s) are visited
	//       or for only a few more iterations following.
	public CostMap getPath(CostMap course, Position start, Position end)
	{
		CostMap result = new CostMap(course.w, course.h, 1.0 / 0.0);
		
		LinkedList<Position> current = new LinkedList<Position>();
		LinkedList<Position> next = new LinkedList<Position>();
		
		/*
		 * Initialize list of Positions to visit - algorithm starts with end.
		 * Set ending Position to be free - its free to get there from itself.
		 */
		current.add(end);
		result.set(end, 0);
		
		/*
		 * Loop while there are still more Positions to visit.
		 */
		while (!current.isEmpty())
		{
			/*
			 * For each Position that was visited last time,
			 * examine all neighbor Positions.
			 */
			for (Position pos : current)
			{
				for (Direction dir : Direction.getIterator(Direction.E, 2))
				{
					Position neighbor = dir.apply(pos);
					
					if (course.getBounds().contains(neighbor))
					{
						/*
						 * For each neighbor that is passable, see if a lower
						 * cost path can be taken from this Position ("pos") to
						 * that neigbor.
						 * 
						 * Moving diagonally to a neighbor Position causes that
						 * Position to cost sqrt(2) its base cost.
						 */
						final double baseCost = result.get(pos);
						final double moveCost = course.get(neighbor);
						final double moveFactor = dir.isDiagonal() ? 1.414 : 1;
						
						final double cost = baseCost + moveCost * moveFactor;

						if ((! Double.isInfinite(cost))
						&& (cost < result.get(neighbor)))
//						if ((! Double.isInfinite(cost))
//						&& (Double.isInfinite(result.get(neighbor))))
						{
							/*
							 * If so, assign the neighbor the lower cost in the
							 * result set and add it to the list of Positions
							 * to be visited next time.
							 */
							result.set(neighbor, cost);
							next.add(neighbor);
						}
					}
				}
			}
			
			/*
			 * Clear list of Positions previously branched two and swap lists.
			 * Now the next becomes the current and the new next is empty.
			 */
			LinkedList<Position> temp = current;
			current.clear();
			current = next;
			next = temp;
		}
		
		/*
		 * If there is no route from the starting Position, return null.
		 */
		return (Double.isInfinite(result.get(start))) ? null : result;
	}
}
