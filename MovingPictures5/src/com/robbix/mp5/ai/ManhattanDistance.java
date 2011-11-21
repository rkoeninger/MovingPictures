package com.robbix.mp5.ai;

import com.robbix.utils.CostMap;
import com.robbix.utils.Position;

public class ManhattanDistance implements Heuristic
{
	public double project(CostMap course, Position start, Position end)
	{
		return Math.abs(start.x - end.x) + Math.abs(start.y - end.y);
	}
}
