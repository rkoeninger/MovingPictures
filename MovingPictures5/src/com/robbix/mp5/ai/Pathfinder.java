package com.robbix.mp5.ai;

import com.robbix.utils.CostMap;
import com.robbix.utils.Position;

public interface Pathfinder
{
	public CostMap getPath(CostMap course, Position start, Position end);
}
