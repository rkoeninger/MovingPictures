package com.robbix.utils;

/**
 * Special version of Iterable that requires an RIterator.
 * 
 * @author bort
 */
public interface RIterable<E> extends Iterable<E>
{
	public RIterator<E> iterator();
}
