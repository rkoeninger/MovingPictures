package com.robbix.mp5.ai;

import com.robbix.utils.CostMap;
import com.robbix.utils.Position;

public class EuclideanDistance implements Heuristic
{
	public double project(CostMap course, Position start, Position end)
	{
		return start.getDistance(end);
	}

}
