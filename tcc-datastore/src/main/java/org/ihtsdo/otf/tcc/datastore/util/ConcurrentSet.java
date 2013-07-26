package org.ihtsdo.otf.tcc.datastore.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSet<E> implements Set<E> {
	private ConcurrentHashMap<E, E> setMap;
	
	
	ConcurrentSet<E> replacement = null;
	public ConcurrentSet(int i) {
		setMap = new ConcurrentHashMap<>(5);
	}

	@Override
	public boolean add(E e) {
		if (replacement != null) {
			return replacement.add(e);
		}
		E old = setMap.putIfAbsent(e, e);
		if (old != null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (replacement != null) {
			return replacement.addAll(c);
		}
		boolean added = false;
		for (E e: c) {
			boolean addedAlready = add(e);
			if (addedAlready) {
				added = true;
			}
		}
		return added;
	}

	public E replace(E e) {
		if (replacement != null) {
			return replacement.replace(e);
		}
		E old = setMap.putIfAbsent(e, e);
		if (old != null && old != e) {
			return old;
		}
		return null;
	}

	@Override
	public void clear() {
		setMap.clear();
	}

	@Override
	public boolean contains(Object value) {
		if (replacement != null) {
			return replacement.contains(value);
		}
		return setMap.contains(value);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (replacement != null) {
			return replacement.containsAll(c);
		}
		for (Object e: c) {
			if (setMap.contains(e)) {
				// nothing to do
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		if (replacement != null) {
			return replacement.isEmpty();
		}
		return setMap.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return setMap.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		if (setMap.containsKey(o)) {
			setMap.remove(o);
			return true;
		}
		return false;
	}

	/**
	 * will throw <code>UnsupportedOperationException</code>;
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removedAnything = false;
		for (Object o: c) {
			if (remove(o)) {
				removedAnything = true;
			}
		}
		return removedAnything;
	}

	/**
	 * will throw <code>UnsupportedOperationException</code>;
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return setMap.size();
	}

	@Override
	public Object[] toArray() {
		return setMap.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return setMap.keySet().toArray(a);
	}

	public void setReplacement(ConcurrentSet<E> replacement) {
		this.replacement = replacement;
	}

	public void setCapacity(int i) {
		ConcurrentHashMap<E, E> old = setMap;
		setMap = new ConcurrentHashMap<>(i);
		addAll(old.keySet());
	}
	
	public String toString() {
		return setMap.keySet().toString();
	}
}
