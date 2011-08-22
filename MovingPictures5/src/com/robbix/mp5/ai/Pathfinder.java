package com.robbix.mp5.ai;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Position;

public interface Pathfinder
{
	public CostMap getPath(CostMap course, Position start, Position end);
}
