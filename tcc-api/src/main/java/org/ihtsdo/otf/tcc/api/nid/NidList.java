/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.nid;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
public class NidList implements NidListBI {

    private List<Integer> listValues = new ArrayList<>(2);

    public NidList(int[] values) {
        super();
        for (int i : values) {
            this.listValues.add(i);
        }
    }

    public NidList() {
        super();
    }


    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#add(int, java.lang.Integer)
     */
    @Override
    public void add(int index, Integer element) {
        listValues.add(index, element);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#add(java.lang.Integer)
     */
    @Override
    public boolean add(Integer o) {
        boolean returnValue = listValues.add(o);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(c);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        boolean returnValue = listValues.addAll(index, c);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#clear()
     */
    @Override
    public void clear() {
        listValues.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return listValues.contains((Integer) o);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return listValues.containsAll(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#get(int)
     */
    @Override
    public Integer get(int index) {
        return listValues.get(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(Object o) {
        return listValues.indexOf(o);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return listValues.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#iterator()
     */
    @Override
    public Iterator<Integer> iterator() {
        return listValues.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object o) {
        return listValues.lastIndexOf(o);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#listIterator()
     */
    @Override
    public ListIterator<Integer> listIterator() {
        return listValues.listIterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#listIterator(int)
     */
    @Override
    public ListIterator<Integer> listIterator(int index) {
        return listValues.listIterator(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#remove(int)
     */
    @Override
    public Integer remove(int index) {
        Integer returnValue = listValues.remove(index);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        return listValues.remove((Integer) o);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean returnValue = listValues.removeAll(c);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean returnValue = listValues.retainAll(c);
        return returnValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#set(int, java.lang.Integer)
     */
    @Override
    public Integer set(int index, Integer element) {
        Integer old = listValues.set(index, element);
        return old;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#size()
     */
    @Override
    public int size() {
        return listValues.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#subList(int, int)
     */
    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        return listValues.subList(fromIndex, toIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#toArray()
     */
    @Override
    public Object[] toArray() {
        return listValues.toArray();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return listValues.toArray(a);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IntList#getListValues()
     */
    @Override
    public List<Integer> getListValues() {
        return listValues;
    }

    @Override
    public int[] getListArray() {
        int[] listArray = new int[listValues.size()];
        for (int i = 0; i < listArray.length; i++) {
            listArray[i] = listValues.get(i);
        }
        return listArray;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        int count = 0;
        for (int i : listValues) {
            try {
                buf.append(Ts.get().getConcept(i).toString());
                if (count++ < listValues.size() - 1) {
                    buf.append(", ");
                }
            } catch (IOException ex) {
                Logger.getLogger(NidList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buf.append("]");
        return buf.toString();
    }
}
