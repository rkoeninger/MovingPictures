package com.robbix.mp5.basics;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SkipNullIterator<T> extends IterableIterator<T>
{
	private Iterator<T> itr;
	private T next;
	
	public SkipNullIterator(Iterator<T> baseIterator)
	{
		this.itr = baseIterator;
	}
	
	public boolean hasNext()
	{
		return (next != null || getNext());
	}

	public T next()
	{
		if (next == null && !getNext())
			throw new NoSuchElementException();
		
		try     { return next; }
		finally { next = null; }
	}

	/**
	 * Retrieves items from the underlying Iterator. Returns true
	 * if a non-null item was found.
	 */
	private boolean getNext()
	{
		while (itr.hasNext())
			if ((next = itr.next()) != null)
				return true;
		
		return false;
	}
}
