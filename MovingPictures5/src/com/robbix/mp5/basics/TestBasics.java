package com.robbix.mp5.basics;

import com.robbix.mp5.Utils;

public class TestBasics
{
	public static void main(String[] args)
	{
		Grid<Integer> grid = new Grid<Integer>(8, 8);
		
		for (int x = 0; x < grid.w; ++x)
		for (int y = 0; y < grid.h; ++y)
			grid.set(x, y, Utils.RNG.nextInt(200));
		
		System.out.println(grid.toString());
		
		long time = System.nanoTime();
		
		System.out.println(grid.findClosest(
			new Position(0, 0),
			new Filter<Integer>()
			{
				public boolean accept(Integer arg)
				{
					return arg <= 5;
				}
			},
			1,
			4
		));
		
		System.out.println((System.nanoTime() - time) / 1000000.0);
	}
}
