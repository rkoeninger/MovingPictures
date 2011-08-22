package com.robbix.mp5.unit;

public class Staff
{
	private int workerCount;
	private int scientistCount;
	
	public Staff(int workerCount, int scientistCount)
	{
		if (workerCount < 0 || scientistCount < 0)
			throw new IllegalArgumentException();
		
		this.workerCount = workerCount;
		this.scientistCount = scientistCount;
	}
	
	public int getWorkerCount()
	{
		return workerCount;
	}
	
	public int getScientistCount()
	{
		return scientistCount;
	}
	
	public boolean satisfies(Staff required)
	{
		return this.workerCount    >= required.workerCount
			&& this.scientistCount >= required.scientistCount;
	}
}
