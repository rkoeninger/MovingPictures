package com.robbix.mp5.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class RIterator<T> implements Iterable<T>, Iterator<T>
{
	/**
	 * Creates a new IterableIterator wrapped around the given Iterator.
	 * All methods from the Iterator class are delegated to
	 * the given Iterator.
	 */
	public static <E> RIterator<E> wrap(final Iterator<E> itr)
	{
		if (itr == null)
			throw new IllegalArgumentException("null");
		
		return new RIterator<E>()
		{
			public boolean hasNext() { return itr.hasNext(); }
			public E next()          { return itr.next();    }
			public void remove()     { itr.remove();         }
		};
	}
	
	/**
	 * Creates a new IterableIterator for the given Iterable collection.
	 */
	public static <E> RIterator<E> iterate(Iterable<E> collection)
	{
		return wrap(collection.iterator());
	}
	
	/**
	 * Throws a NoSuchElementException if hasNext() returns false.
	 */
	protected void checkHasNext()
	{
		if (! hasNext())
			throw new NoSuchElementException();
	}
	
	/**
	 * Default implementation - always throws UnsupportedOperationException.
	 * All implementations in MP5 do this anyway.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns this Iterator.
	 */
	public RIterator<T> iterator()
	{
		return this;
	}
}
