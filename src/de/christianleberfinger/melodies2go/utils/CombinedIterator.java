package de.christianleberfinger.melodies2go.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * An iterator for iterating/combining elements of several given collections.
 * The combined iterator will take one item of the first collection and then
 * traverse to the next collection. The iterator will iterate each and every
 * element of all the given collections.
 * 
 * @author krizleebear
 *
 * @param <E>
 */
public class CombinedIterator<E> implements Iterator<E>
{
	private final Iterator<E>[] iterators;
	private int currentIteratorIndex = 0;

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public CombinedIterator(final Collection<E>... collections)
	{
		iterators = new Iterator[collections.length];

		int i = 0;
		for (Collection<E> c : collections)
		{
			iterators[i++] = c.iterator();
		}
	}

	private Iterator<E> currentIterator()
	{
		return iterators[currentIteratorIndex];
	}

	/**
	 * move to the next iterator that still has elements
	 */
	private void nextIterator()
	{
		for (int i = 1; i <= iterators.length; i++)
		{
			int newIndex = (currentIteratorIndex + i) % iterators.length;
			if (iterators[newIndex].hasNext())
			{
				currentIteratorIndex = newIndex;
				break;
			}
		}
	}

	@Override
	public boolean hasNext()
	{
		if (currentIterator().hasNext())
		{
			return true;
		}
		else
		{
			// if current iterator is at its end, try the next one
			nextIterator();
		}

		return currentIterator().hasNext();
	}

	@Override
	public E next()
	{
		E next = currentIterator().next();
		nextIterator();
		return next;
	}
}
