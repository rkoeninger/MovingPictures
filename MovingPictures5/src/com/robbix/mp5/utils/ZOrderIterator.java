package com.robbix.mp5.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.robbix.mp5.unit.Unit;

public class ZOrderIterator
{
	private Iterator<Unit> itr;
	private LinkedList<Unit> queue;
	private Unit next;
	
	public ZOrderIterator(Iterator<Unit> baseIterator)
	{
		this.itr = baseIterator;
		this.queue = new LinkedList<Unit>();
	}
	
	public boolean hasNext()
	{
		if (next == null && queue.isEmpty() && !getNext())
			return !queue.isEmpty();
		
		return true;
	}

	public Unit next()
	{
		if (next == null && !getNext() && queue.isEmpty())
			throw new NoSuchElementException();
		
		if (shouldDispenseQueue())
			return queue.removeFirst();
		
		try     { return next; }
		finally { next = null; }
	}

	/**
	 * Retrieves items from the underlying Iterator. Returns true
	 * only if next will not be null, no matter how many units were
	 * read and added to the structure queue.
	 */
	private boolean getNext()
	{
		while (itr.hasNext())
		{
			if ((next = itr.next()) == null)
			{
				continue;
			}
			else if (next.isStructure())
			{
				queue.addLast(next);
				next = null;
			}
			else
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Determines if the Unit referenced by next has a higher z-value
	 * than the structures in the queue.
	 */
	private boolean shouldDispenseQueue()
	{
		if (next == null)
			return true;
		
		if (queue.isEmpty())
			return false;
		
		final int structureY = queue.getFirst().getPosition().y;
		final int unitY = next.getPosition().y;
		
		return structureY < unitY;
	}
	
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
