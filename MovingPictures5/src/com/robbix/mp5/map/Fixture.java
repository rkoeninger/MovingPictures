package com.robbix.mp5.map;

public enum Fixture
{
	WALL(false), TUBE(true), GEYSER(false), MAGMA(false);
	
	public final boolean passable;
	
	private Fixture(boolean passable)
	{
		this.passable = passable;
	}
}
