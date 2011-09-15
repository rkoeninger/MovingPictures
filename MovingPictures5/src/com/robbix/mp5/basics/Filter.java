package com.robbix.mp5.basics;

/**
 * Class that implements a simple accept/reject filter.
 * 
 * Static methods provide common filters.
 */
public abstract class Filter<T>
{
	/**
	 * Checks to see if an argument is accepted by this Filter.
	 */
	public abstract boolean accept(T arg);
	
	/**
	 * Checks all filtered arguments for equality with the given object.
	 */
	public static <E> Filter<E> getEqualityFilter(final E compareTo)
	{
		if (compareTo == null)
			throw new IllegalArgumentException();
		
		return new Filter<E>()
		{
			public boolean accept(E arg)
			{
				return compareTo.equals(arg);
			}
		};
	}
	
	/**
	 * Checks all filtered arguments to see if they are null.
	 * 
	 * If {@code shouldBeNull} is false, then all null references are
	 * rejected.
	 * 
	 * If {@code shouldBeNull} is true, then only null references
	 * are accepted.
	 */
	public static Filter<?> getNullFilter(final boolean shouldBeNull)
	{
		return new Filter<Object>()
		{
			public boolean accept(Object arg)
			{
				return shouldBeNull == (arg == null);
			}
		};
	}
	
	/**
	 * Allows all arguments.
	 */
	public static <E> Filter<E> getAllFilter()
	{
		return new Filter<E>()
		{
			public boolean accept(E arg)
			{
				return true;
			}
		};
	}

	/**
	 * Allows no arguments.
	 */
	public static <E> Filter<E> getNoneFilter()
	{
		return new Filter<E>()
		{
			public boolean accept(E arg)
			{
				return false;
			}
		};
	}

	/**
	 * Allows only true.
	 */
	public static Filter<Boolean> getTrueFilter()
	{
		return new Filter<Boolean>()
		{
			public boolean accept(Boolean arg)
			{
				return arg;
			}
		};
	}

	/**
	 * Allows only false.
	 */
	public static Filter<Boolean> getFalseFilter()
	{
		return new Filter<Boolean>()
		{
			public boolean accept(Boolean arg)
			{
				return !arg;
			}
		};
	}
}
