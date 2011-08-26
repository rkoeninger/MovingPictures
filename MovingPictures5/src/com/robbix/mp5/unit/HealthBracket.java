package com.robbix.mp5.unit;

public enum HealthBracket
{
	GREEN, YELLOW, RED;
	
	public static HealthBracket getDefault(double hpFactor)
	{
		if      (hpFactor < 0.25) return HealthBracket.RED;
		else if (hpFactor < 0.5)  return HealthBracket.YELLOW;
		else                      return HealthBracket.GREEN;
	}
}
