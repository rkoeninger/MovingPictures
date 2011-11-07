package com.robbix.mp5.utils;

import java.util.Iterator;

public class SkipNullIterator<T> extends RIterator<T>
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
		checkHasNext();
		
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
