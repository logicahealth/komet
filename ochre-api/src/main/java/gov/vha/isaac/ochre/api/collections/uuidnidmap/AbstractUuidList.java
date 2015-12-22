/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.api.collections.uuidnidmap;

import java.util.Comparator;
import org.apache.mahout.math.Arrays;
import org.apache.mahout.math.Sorting;
import org.apache.mahout.math.list.AbstractList;

/**
 *
 * @author kec
 */
public abstract class AbstractUuidList extends AbstractList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static UuidComparatorBI c = new UuidUnsigned64BitComparator();
    /**
     * The size of the list. This is a READ_ONLY variable for all methods but setSizeRaw(int newSize) !!! If
     * you violate this principle in subclasses, you should exactly know what you are doing.
     *
     * @serial
     */
    protected int size;

    /**
     * Makes this class non instantiable, but still let's others inherit from it.
     */
    protected AbstractUuidList() {
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param element element to be appended to this list.
     */
    public void add(long[] element) {
        beforeInsert(size, element);
    }

    /**
     * Appends the part of the specified list between
     * {@code from} (inclusive) and
     * {@code to} (inclusive) to the receiver.
     *
     * @param other the list to be added to the receiver.
     * @param from the index of the first element to be appended (inclusive).
     * @param to the index of the last element to be appended (inclusive).
     * @exception IndexOutOfBoundsException index is out of range ( {@code other.size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=other.size())} ).
     */
    public void addAllOfFromTo(AbstractUuidList other, int from, int to) {
        beforeInsertAllOfFromTo(size, other, from, to);
    }

    /**
     * Inserts the specified element before the specified position into the receiver. Shifts the element
     * currently at that position (if any) and any subsequent elements to the right.
     *
     * @param index index before which the specified element is to be inserted (must be in [0,size]).
     * @param element element to be inserted.
     * @throws IndexOutOfBoundsException if {@code index &lt; 0 || index &gt; size()}.
     */
    public void beforeInsert(int index, long[] element) {
        beforeInsertDummies(index, 1);
        set(index, element);
    }

    /**
     * Inserts the part of the specified list between
     * {@code otherFrom} (inclusive) and
     * {@code otherTo} (inclusive) before the specified position into the receiver. Shifts the element
     * currently at that position (if any) and any subsequent elements to the right.
     *
     * @param index index before which to insert first element from the specified list (must be in [0,size])..
     * @param other list of which a part is to be inserted into the receiver.
     * @param from the index of the first element to be inserted (inclusive).
     * @param to the index of the last element to be inserted (inclusive).
     * @exception IndexOutOfBoundsException index is out of range ( {@code other.size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=other.size())} ).
     * @throws IndexOutOfBoundsException if {@code index &lt; 0 || index &gt; size()}.
     */
    public void beforeInsertAllOfFromTo(int index, AbstractUuidList other,
            int from, int to) {
        int length = to - from + 1;
        this.beforeInsertDummies(index, length);
        this.replaceFromToWithFrom(index, index + length - 1, other, from);
    }

    /**
     * Inserts {@code length} dummy elements before the specified position into the receiver. Shifts the
     * element currently at that position (if any) and any subsequent elements to the right. <b>This method
     * must set the new size to be {@code size()+length}.
     *
     * @param index index before which to insert dummy elements (must be in [0,size])..
     * @param length number of dummy elements to be inserted.
     * @throws IndexOutOfBoundsException if {@code index &lt; 0 || index &gt; size()}.
     */
    @Override
    protected void beforeInsertDummies(int index, int length) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        }
        if (length > 0) {
            ensureCapacity(size + length);
            setSizeRaw(size + length);
            replaceFromToWithFrom(index + length, size - 1, this, index);
        }
    }

    /**
     * Searches the receiver for the specified value using the binary search algorithm. The receiver must
     * <strong>must</strong> be sorted (as by the sort method) prior to making this call. If it is not sorted,
     * the results are undefined: in particular, the call may enter an infinite loop. If the receiver contains
     * multiple elements equal to the specified object, there is no guarantee which instance will be found.
     *
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the receiver; otherwise, {@code (-(<i>insertion
     * point</i>) - 1)}. The <i>insertion point</i> is defined as the the point at which the value would
     * be inserted into the receiver: the index of the first element greater than the key, or
     * {@code receiver.size()}, if all elements in the receiver are less than the specified key. Note that
     * this guarantees that the return value will be &gt;= 0 if and only if the key is found.
     * @see java.util.Arrays
     */
    public int binarySearch(long[] key) {
        return this.binarySearchFromTo(key, 0, size - 1);
    }

    /**
     * Searches the receiver for the specified value using the binary search algorithm. The receiver must
     * <strong>must</strong> be sorted (as by the sort method) prior to making this call. If it is not sorted,
     * the results are undefined: in particular, the call may enter an infinite loop. If the receiver contains
     * multiple elements equal to the specified object, there is no guarantee which instance will be found.
     *
     * @param key the value to be searched for.
     * @param from the leftmost search position, inclusive.
     * @param to the rightmost search position, inclusive.
     * @return index of the search key, if it is contained in the receiver; otherwise, {@code (-(<i>insertion
     * point</i>) - 1)}. The <i>insertion point</i> is defined as the the point at which the value would
     * be inserted into the receiver: the index of the first element greater than the key, or
     * {@code receiver.size()}, if all elements in the receiver are less than the specified key. Note that
     * this guarantees that the return value will be &gt;= 0 if and only if the key is found.
     * @see java.util.Arrays
     */
    public int binarySearchFromTo(long[] key, int from, int to) {
        int low = from;
        int high = to;
        while (low <= high) {
            int mid = (low + high) / 2;
            long[] midVal = get(mid);

            if (midVal[0] < key[0]) {
                low = mid + 1;
            } else if (midVal[0] > key[0]) {
                high = mid - 1;
            } else if (midVal[1] < key[1]) {
                low = mid + 1;
            } else if (midVal[1] > key[1]) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }

    /**
     * Returns a deep copy of the receiver.
     *
     * @return a deep copy of the receiver.
     */
    @Override
    public Object clone() {
        return partFromTo(0, size - 1);
    }

    /**
     *
     * @param elem element whose presence in the receiver is to be tested.
     * @return true if the receiver contains the specified element.
     */
    public boolean contains(long[] elem) {
        return indexOfFromTo(elem, 0, size - 1) >= 0;
    }

    /**
     * Deletes the first element from the receiver that is identical to the specified element. Does nothing,
     * if no such matching element is contained.
     *
     * @param element the element to be deleted.
     */
    public void delete(long[] element) {
        int index = indexOfFromTo(element, 0, size - 1);
        if (index >= 0) {
            remove(index);
        }
    }

    /**
     * Returns the elements currently stored, possibly including invalid elements between size and capacity.
     *
     * <b>WARNING:</b> For efficiency reasons and to keep memory usage low, this method may decide <b>not to
     * copy the array</b>. So if subsequently you modify the returned array directly via the [] operator, be
     * sure you know what you're doing.
     *
     * @return the elements currently stored.
     */
    public abstract long[] elements();

    /**
     * Sets the receiver's elements to be the specified array. The size and capacity of the list is the length
     * of the array. <b>WARNING:</b> For efficiency reasons and to keep memory usage low, this method may
     * decide <b>not to copy the array</b>. So if subsequently you modify the returned array directly via the
     * [] operator, be sure you know what you're doing.
     *
     * @param elements the new elements to be stored.
     * @return the receiver itself.
     */
    public AbstractUuidList elements(long[] elements) {
        clear();
        addAllOfFromTo(new UuidArrayList(elements), 0, elements.length - 1);
        return this;
    }

    /**
     * Ensures that the receiver can hold at least the specified number of elements without needing to
     * allocate new internal memory. If necessary, allocates new internal memory and increases the capacity of
     * the receiver.
     *
     * @param minCapacity the desired minimum capacity.
     */
    public abstract void ensureCapacity(int minCapacity);

    /**
     * Sets the specified range of elements in the specified array to the specified value.
     *
     * @param from the index of the first element (inclusive) to be filled with the specified value.
     * @param to the index of the last element (inclusive) to be filled with the specified value.
     * @param val the value to be stored in the specified elements of the receiver.
     */
    public void fillFromToWith(int from, int to, long[] val) {
        AbstractList.checkRangeFromTo(from, to, this.size);
        for (int i = from; i <= to;) {
            setQuick(i++, val);
        }
    }

    /**
     * Applies a procedure to each element of the receiver, if any. Starts at index 0, moving rightwards.
     *
     * @param procedure the procedure to be applied. Stops iteration if the procedure returns {@code false},
     * otherwise continues.
     * @return {@code false} if the procedure stopped before all elements where iterated over, {@code true}
     * otherwise.
     */
    public boolean forEach(UuidProcedure procedure) {
        for (int i = 0; i < size;) {
            long[] uuid = get(i);
            if (!procedure.apply(uuid)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param index index of element to return.
     * @return the element at the specified position in the receiver.
     * @exception IndexOutOfBoundsException index is out of range (index &lt; 0 || index &gt;= size()).
     */
    public long[] get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        }
        return getQuick(index);
    }

    /**
     * Returns the element at the specified position in the receiver; <b>WARNING:</b> Does not check
     * preconditions. Provided with invalid parameters this method may return invalid elements without
     * throwing any exception! <b>You should only use this method when you are absolutely sure that the index
     * is within bounds.</b> Precondition (unchecked): {@code index &gt;= 0 && index &lt; size()}.
     *
     * This method is normally only used internally in large loops where bounds are explicitly checked before
     * the loop and need no be rechecked within the loop. However, when desperately, you can give this method
     * {@code public} visibility in subclasses.
     *
     * @param index index of element to return.
     * @return the element at the specified position in the receiver;
     */
    protected abstract long[] getQuick(int index);

    /**
     * Returns the index of the first occurrence of the specified element. Returns
     * {@code -1} if the receiver does not contain this element.
     *
     * @param element the element to be searched for.
     * @return the index of the first occurrence of the element in the receiver; returns {@code -1} if
     * the element is not found.
     */
    public int indexOf(long[] element) { // delta
        return indexOfFromTo(element, 0, size - 1);
    }

    /**
     * Returns the index of the first occurrence of the specified element. Returns
     * {@code -1} if the receiver does not contain this element. Searches between
     * {@code from}, inclusive and
     * {@code to}, inclusive. Tests for identity.
     *
     * @param element element to search for.
     * @param from the leftmost search position, inclusive.
     * @param to the rightmost search position, inclusive.
     * @return the index of the first occurrence of the element in the receiver; returns {@code -1} if
     * the element is not found.
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    public int indexOfFromTo(long[] element, int from, int to) {
        AbstractList.checkRangeFromTo(from, to, size);

        for (int i = from; i <= to; i++) {
            long[] another = getQuick(i);
            if (element[0] == another[0]
                    && element[1] == another[1]) {
                return i; // found
            }
        }
        return -1; // not found
    }

    /**
     * Returns the index of the last occurrence of the specified element. Returns
     * {@code -1} if the receiver does not contain this element.
     *
     * @param element the element to be searched for.
     * @return the index of the last occurrence of the element in the receiver; returns {@code -1} if the
     * element is not found.
     */
    public int lastIndexOf(long[] element) {
        return lastIndexOfFromTo(element, 0, size - 1);
    }

    /**
     * Returns the index of the last occurrence of the specified element. Returns
     * {@code -1} if the receiver does not contain this element. Searches beginning at
     * {@code to}, inclusive until
     * {@code from}, inclusive. Tests for identity.
     *
     * @param element element to search for.
     * @param from the leftmost search position, inclusive.
     * @param to the rightmost search position, inclusive.
     * @return the index of the last occurrence of the element in the receiver; returns {@code -1} if the
     * element is not found.
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    public int lastIndexOfFromTo(long[] element, int from, int to) {
        AbstractList.checkRangeFromTo(from, to, size());

        for (int i = to; i >= from; i--) {
            if (element.equals(getQuick(i))) {
                return i; // found
            }
        }
        return -1; // not found
    }

    /**
     * Sorts the specified range of the receiver into ascending order.
     *
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in
     * the low sublist is less than the lowest element in the high sublist). This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly sorted lists.
     *
     * <p> <b>You should never call this method unless you are sure that this particular sorting algorithm is
     * the right one for your data set.</b> It is generally better to call {@code sort()} or
     * {@code sortFromTo(...)} instead, because those methods automatically choose the best sorting
     * algorithm.
     *
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to the index of the last element (inclusive) to be sorted.
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    @Override
    public void mergeSortFromTo(int from, int to) {
        int mySize = size();
        AbstractList.checkRangeFromTo(from, to, mySize);

        long[] myElements = elements();
        Sorting.mergeSort(myElements, from, to + 1);
        elements(myElements);
        setSizeRaw(mySize);
    }

    /**
     * Sorts the receiver according to the order induced by the specified comparator. All elements in the
     * range must be <i>mutually comparable</i> by the specified comparator (that is, {@code c.compare(e1,
     * e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and {@code e2} in
     * the range). <p>
     *
     * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the
     * sort. <p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in
     * the low sublist is less than the lowest element in the high sublist). This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly sorted lists.
     *
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to the index of the last element (inclusive) to be sorted.
     * @param c the comparator to determine the order of the receiver.
     * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> using
     * the specified comparator.
     * @throws IllegalArgumentException if {@code fromIndex &gt; toIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex &lt; 0} or {@code toIndex &gt; a.length}
     * @see Comparator
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    public void mergeSortFromTo(int from, int to, UuidComparatorBI c) {
        int mySize = size();
        AbstractList.checkRangeFromTo(from, to, mySize);

        long[] myElements = elements();
        UuidSorting.mergeSort(myElements, from, to + 1, c);
        elements(myElements);
        setSizeRaw(mySize);
    }

    /**
     * Returns a new list of the part of the receiver between
     * {@code from}, inclusive, and
     * {@code to}, inclusive.
     *
     * @param from the index of the first element (inclusive).
     * @param to the index of the last element (inclusive).
     * @return a new list
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    public AbstractUuidList partFromTo(int from, int to) {
        AbstractList.checkRangeFromTo(from, to, size);

        int length = to - from + 1;
        UuidArrayList part = new UuidArrayList(length);
        part.addAllOfFromTo(this, from, to);
        return part;
    }

    /**
     * Sorts the specified range of the receiver into ascending numerical order. The sorting algorithm is a
     * tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm offers
     * n*log(n) performance on many data sets that cause other quicksorts to degrade to quadratic performance.
     *
     * <p> <b>You should never call this method unless you are sure that this particular sorting algorithm is
     * the right one for your data set.</b> It is generally better to call {@code sort()} or
     * {@code sortFromTo(...)} instead, because those methods automatically choose the best sorting
     * algorithm.
     *
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to the index of the last element (inclusive) to be sorted.
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    @Override
    public void quickSortFromTo(int from, int to) {
        int mySize = size();
        AbstractList.checkRangeFromTo(from, to, mySize);
        long[] myElements = elements();
        UuidSorting.quickSort(myElements, from, to + 1, c);
        elements(myElements);
        setSizeRaw(mySize);
    }

    /**
     * Sorts the receiver according to the order induced by the specified comparator. All elements in the
     * range must be <i>mutually comparable</i> by the specified comparator (that is, {@code c.compare(e1,
     * e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and {@code e2} in
     * the range). <p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993). This algorithm offers n*log(n) performance on many data sets that cause other quicksorts to
     * degrade to quadratic performance.
     *
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to the index of the last element (inclusive) to be sorted.
     * @param c the comparator to determine the order of the receiver.
     * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> using
     * the specified comparator.
     * @throws IllegalArgumentException if {@code fromIndex &gt; toIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex &lt; 0} or {@code toIndex &gt; a.length}
     * @see Comparator
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    public void quickSortFromTo(int from, int to, UuidComparatorBI c) {
        int mySize = size();
        AbstractList.checkRangeFromTo(from, to, mySize);

        long[] myElements = elements();
        UuidSorting.quickSort(myElements, from, to + 1, c);
        elements(myElements);
        setSizeRaw(mySize);
    }

    /**
     * Removes from the receiver all elements that are contained in the specified list. Tests for identity.
     *
     * @param other the other list.
     * @return {@code true} if the receiver changed as a result of the call.
     */
    public boolean removeAll(AbstractUuidList other) {
        if (other.size() == 0) {
            return false; // nothing to do
        }
        int limit = other.size() - 1;
        int j = 0;

        for (int i = 0; i < size; i++) {
            if (other.indexOfFromTo(getQuick(i), 0, limit) < 0) {
                setQuick(j++, getQuick(i));
            }
        }

        boolean modified = (j != size);
        setSize(j);
        return modified;
    }

    /**
     * Removes from the receiver all elements whose index is between
     * {@code from}, inclusive and
     * {@code to}, inclusive. Shifts any succeeding elements to the left (reduces their index). This call
     * shortens the list by {@code (to - from + 1)} elements.
     *
     * @param from index of first element to be removed.
     * @param to index of last element to be removed.
     * @exception IndexOutOfBoundsException index is out of range ( {@code size()&gt;0 && (from&lt;0 ||
     * from&gt;to || to&gt;=size())} ).
     */
    @Override
    public void removeFromTo(int from, int to) {
        AbstractList.checkRangeFromTo(from, to, size);
        int numMoved = size - to - 1;
        if (numMoved > 0) {
            replaceFromToWithFrom(from, from - 1 + numMoved, this, to + 1);
            // fillFromToWith(from+numMoved, size-1, 0.0f); //delta
        }
        int width = to - from + 1;
        if (width > 0) {
            setSizeRaw(size - width);
        }
    }

    /**
     * Replaces a number of elements in the receiver with the same number of elements of another list.
     * Replaces elements in the receiver, between
     * {@code from} (inclusive) and
     * {@code to} (inclusive), with elements of
     * {@code other}, starting from
     * {@code otherFrom} (inclusive).
     *
     * @param from the position of the first element to be replaced in the receiver
     * @param to the position of the last element to be replaced in the receiver
     * @param other list holding elements to be copied into the receiver.
     * @param otherFrom position of first element within other list to be copied.
     */
    public void replaceFromToWithFrom(int from, int to, AbstractUuidList other,
            int otherFrom) {
        int length = to - from + 1;
        if (length > 0) {
            AbstractList.checkRangeFromTo(from, to, size());
            AbstractList.checkRangeFromTo(otherFrom, otherFrom + length - 1, other.size());

            // unambiguous copy (it may hold other==this)
            if (from <= otherFrom) {
                for (; --length >= 0;) {
                    setQuick(from++, other.getQuick(otherFrom++));
                }
            } else {
                int otherTo = otherFrom + length - 1;
                for (; --length >= 0;) {
                    setQuick(to--, other.getQuick(otherTo--));
                }
            }

        }
    }

    /**
     * Retains (keeps) only the elements in the receiver that are contained in the specified other list. In
     * other words, removes from the receiver all of its elements that are not contained in the specified
     * other list.
     *
     * @param other the other list to test against.
     * @return {@code true} if the receiver changed as a result of the call.
     */
    public boolean retainAll(AbstractUuidList other) {
        if (other.size() == 0) {
            if (size == 0) {
                return false;
            }
            setSize(0);
            return true;
        }

        int limit = other.size() - 1;
        int j = 0;
        for (int i = 0; i < size; i++) {
            if (other.indexOfFromTo(getQuick(i), 0, limit) >= 0) {
                setQuick(j++, getQuick(i));
            }
        }

        boolean modified = (j != size);
        setSize(j);
        return modified;
    }

    /**
     * Reverses the elements of the receiver. Last becomes first, second last becomes second first, and so on.
     */
    @Override
    public void reverse() {
        long[] tmp;
        int limit = size() / 2;
        int j = size() - 1;

        for (int i = 0; i < limit;) { // swap
            tmp = getQuick(i);
            setQuick(i++, getQuick(j));
            setQuick(j--, tmp);
        }
    }

    /**
     * Replaces the element at the specified position in the receiver with the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @throws IndexOutOfBoundsException if {@code index &lt; 0 || index &gt;= size()}.
     */
    public void set(int index, long[] element) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
        }
        setQuick(index, element);
    }

    /**
     * Replaces the element at the specified position in the receiver with the specified element;
     * <b>WARNING:</b> Does not check preconditions. Provided with invalid parameters this method may access
     * invalid indexes without throwing any exception! <b>You should only use this method when you are
     * absolutely sure that the index is within bounds.</b> Precondition (unchecked): {@code index &gt;= 0 &&
     * index &lt; size()}.
     *
     * This method is normally only used internally in large loops where bounds are explicitly checked before
     * the loop and need no be rechecked within the loop. However, when desperately, you can give this method
     * {@code public} visibility in subclasses.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     */
    protected abstract void setQuick(int index, long[] element);

    /**
     * Sets the size of the receiver without modifying it otherwise. This method should not release or
     * allocate new memory but simply set some instance variable like {@code size}.
     *
     * If your subclass overrides and delegates size changing methods to some other object, you must make sure
     * that those overriding methods not only update the size of the delegate but also of this class. For
     * example: public DatabaseList extends AbstractUuidList { ... public void removeFromTo(int from,int to) {
     * myDatabase.removeFromTo(from,to); this.setSizeRaw(size-(to-from+1)); } }
     * @param newSize the size of the receiver
     */
    protected void setSizeRaw(int newSize) {
        size = newSize;
    }

    /**
     *
     * @return the number of elements contained in the receiver.
     */
    @Override
    public int size() {
        return size;
    }


    /**
     * Returns a string representation of the receiver, containing the String representation of each element.
     */
    @Override
    public String toString() {
        return Arrays.toString(partFromTo(0, size() - 1).elements());
    }
}
