package com.robbix.mp5.basics;

public class Tuple<A, B>
{
	public final A a;
	public final B b;
	
	public Tuple(A a, B b)
	{
		if (a == null || b == null)
			throw new IllegalArgumentException("null");
		
		this.a = a;
		this.b = b;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof Tuple)
		{
			Tuple<?, ?> that = (Tuple<?, ?>) obj;
			return this.a.equals(that.a) && this.b.equals(that.b);
		}
		
		return false;
	}
	
	public int hashCode()
	{
		return a.hashCode() ^ b.hashCode();
	}
}
