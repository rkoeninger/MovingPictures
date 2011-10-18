package com.robbix.mp5.ai;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Position;

public interface Heuristic
{
	/**
	 * Returns a heuristically projected cost from start to end in the given
	 * course. Return value must be non-negative and finite.
	 */
	public double project(CostMap course, Position start, Position end);
}
