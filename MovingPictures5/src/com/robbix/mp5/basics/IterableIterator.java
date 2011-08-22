package com.robbix.mp5.basics;

import java.util.Iterator;

public abstract class IterableIterator<T> implements Iterable<T>, Iterator<T>
{
	/**
	 * Creates a new IterableIterator wrapped around the given Iterator.
	 * The methods {@code hasNext()} and {@code next()} are delegated to
	 * the given Iterator - the methods {@code remove()} and {@code iterator()}
	 * use the default implementations of the IterableIterator class.
	 */
	public static <E> IterableIterator<E> wrap(final Iterator<E> itr)
	{
		if (itr == null)
			throw new IllegalArgumentException("null Iterator");
		
		return new IterableIterator<E>()
		{
			public boolean hasNext() { return itr.hasNext(); }
			public E next()          { return itr.next();    }
		};
	}
	
	/**
	 * Creates a new IterableIterator for the given Iterable collection.
	 */
	public static <E> IterableIterator<E> iterate(Iterable<E> collection)
	{
		return wrap(collection.iterator());
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
	public Iterator<T> iterator()
	{
		return this;
	}
}
