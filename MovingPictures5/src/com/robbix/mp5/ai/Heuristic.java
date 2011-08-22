package com.robbix.mp5.ai;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Position;

// TODO: More powerful heuristics will have to be initialized with the
//       course info (this could be done on the first call).
//       An initialization method will have to be introduced and perhaps
//       a HeuristicProvider that is initialized for the entire map that
//       creates individual Heuristics that are each initialized for each
//       path find.
public interface Heuristic
{
	/**
	 * Returns a heuristically projected cost from start to end in the given
	 * course. Return value must be non-negative and finite.
	 */
	public double project(CostMap course, Position start, Position end);
}
