package com.robbix.mp5.ai;

import com.robbix.mp5.utils.CostMap;
import com.robbix.mp5.utils.Position;

public interface Pathfinder
{
	public CostMap getPath(CostMap course, Position start, Position end);
}
