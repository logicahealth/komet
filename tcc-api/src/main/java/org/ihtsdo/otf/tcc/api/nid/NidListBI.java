package org.ihtsdo.otf.tcc.api.nid;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface NidListBI {

    public void add(int index, Integer element);

    public boolean add(Integer o);

    public boolean addAll(Collection<? extends Integer> c);

    public boolean addAll(int index, Collection<? extends Integer> c);

    public void clear();

    public boolean contains(Object o);

    public boolean containsAll(Collection<?> c);

    public Integer get(int index);

    public int indexOf(Object o);

    public boolean isEmpty();

    public Iterator<Integer> iterator();

    public int lastIndexOf(Object o);

    public ListIterator<Integer> listIterator();

    public ListIterator<Integer> listIterator(int index);

    public Integer remove(int index);

    public boolean remove(Object o);

    public boolean removeAll(Collection<?> c);

    public boolean retainAll(Collection<?> c);

    public Integer set(int index, Integer element);

    public int size();

    public List<Integer> subList(int fromIndex, int toIndex);

    public Object[] toArray();

    public <T> T[] toArray(T[] a);

    public List<Integer> getListValues();

    public int[] getListArray();

}
