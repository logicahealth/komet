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
package org.ihtsdo.otf.tcc.datastore.uuidnidmap;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.mahout.collections.Arithmetic;
import org.apache.mahout.math.Arrays;

/**
 *
 * @author kec
 */
public class UuidArrayList  extends AbstractUuidList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The array buffer into which the elements of the list are stored. The
	 * capacity of the list is the length of this array buffer.
	 * 
	 * @serial
	 */
	protected long[] elements;

	/**
	 * Constructs an empty list.
	 */
	public UuidArrayList() {
		this(10);
	}

	/**
	 * Constructs a list containing the specified elements. The initial size and
	 * capacity of the list is the length of the array.
	 * 
	 * <b>WARNING:</b> For efficiency reasons and to keep memory usage low,
	 * <b>the array is not copied</b>. So if subsequently you modify the
	 * specified array directly via the [] operator, be sure you know what
	 * you're doing.
	 * 
	 * @param elements
	 *            the array to be backed by the the constructed list
	 */
	public UuidArrayList(long[] elements) {
		elements(elements);
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *            the number of elements the receiver can hold without
	 *            auto-expanding itself by allocating new internal memory.
	 */
	public UuidArrayList(int initialCapacity) {
		this(new long[initialCapacity*2]);
		setSizeRaw(0);
	}

	/**
	 * Appends the specified element to the end of this list.
	 * 
	 * @param element
	 *            element to be appended to this list.
	 */
        @Override
	public void add(long[] element) {
		// overridden for performance only.
		int msbIndex = size * 2;
		int lsbIndex = msbIndex + 1;
		if (lsbIndex >= elements.length)
			ensureCapacity(lsbIndex + 1);
		elements[msbIndex] = element[0];
		elements[lsbIndex] = element[1];
		size++;
	}

	public void add(UUID element) {
		// overridden for performance only.
		int msbIndex = size * 2;
		int lsbIndex = msbIndex + 1;
		if (lsbIndex >= elements.length)
			ensureCapacity(lsbIndex + 1);
		elements[msbIndex] = element.getMostSignificantBits();
		elements[lsbIndex] = element.getLeastSignificantBits();
		size++;
	}


	/**
	 * Inserts the specified element before the specified position into the
	 * receiver. Shifts the element currently at that position (if any) and any
	 * subsequent elements to the right.
	 * 
	 * @param index
	 *            index before which the specified element is to be inserted
	 *            (must be in [0,size]).
	 * @param element
	 *            element to be inserted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (
	 *                <tt>index &lt; 0 || index &gt; size()</tt>).
	 */
        @Override
	public void beforeInsert(int index, long[] element) {
		// overridden for performance only.
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ size);
		int elementLength = size * 2 + 1;
		ensureCapacity(elementLength);
		int indexMsb = index * 2;
		int indexLsb = indexMsb + 1;
		System.arraycopy(elements, indexLsb, elements, indexLsb + 1, size - index);
		elements[indexMsb] = element[0];
		elements[indexLsb] = element[1];
		size++;
	}

	/**
	 * Searches the receiver for the specified value using the binary search
	 * algorithm. The receiver must <strong>must</strong> be sorted (as by the
	 * sort method) prior to making this call. If it is not sorted, the results
	 * are undefined: in particular, the call may enter an infinite loop. If the
	 * receiver contains multiple elements equal to the specified object, there
	 * is no guarantee which instance will be found.
	 * 
	 * @param key
	 *            the value to be searched for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @return index of the search key, if it is contained in the receiver;
	 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
	 *         <i>insertion point</i> is defined as the the point at which the
	 *         value would be inserted into the receiver: the index of the first
	 *         element greater than the key, or <tt>receiver.size()</tt>, if all
	 *         elements in the receiver are less than the specified key. Note
	 *         that this guarantees that the return value will be &gt;= 0 if and
	 *         only if the key is found.
	 * @see org.ihtsdo.cern.colt.Sorting
	 * @see java.util.Arrays
	 */
        @Override
	public int binarySearchFromTo(long[] key, int from, int to) {
		return UuidSorting.binarySearchFromTo(this.elements, key, from,
				to);
	}

	/**
	 * Returns a deep copy of the receiver.
	 * 
	 * @return a deep copy of the receiver.
	 */
        @Override
	public Object clone() {
		// overridden for performance only.
		UuidArrayList clone = new UuidArrayList((long[]) elements.clone());
		clone.setSizeRaw(size);
		return clone;
	}

	/**
	 * Returns a deep copy of the receiver; uses <code>clone()</code> and casts
	 * the result.
	 * 
	 * @return a deep copy of the receiver.
	 */
	public UuidArrayList copy() {
		return (UuidArrayList) clone();
	}


	/**
	 * Returns the elements currently stored, including invalid elements between
	 * size and capacity, if any.
	 * 
	 * <b>WARNING:</b> For efficiency reasons and to keep memory usage low,
	 * <b>the array is not copied</b>. So if subsequently you modify the
	 * returned array directly via the [] operator, be sure you know what you're
	 * doing.
	 * 
	 * @return the elements currently stored.
	 */
        @Override
	public long[] elements() {
		return elements;
	}

	/**
	 * Sets the receiver's elements to be the specified array (not a copy of
	 * it).
	 * 
	 * The size and capacity of the list is the length of the array.
	 * <b>WARNING:</b> For efficiency reasons and to keep memory usage low,
	 * <b>the array is not copied</b>. So if subsequently you modify the
	 * specified array directly via the [] operator, be sure you know what
	 * you're doing.
	 * 
	 * @param elements
	 *            the new elements to be stored.
	 * @return the receiver itself.
	 */
        @Override
	public AbstractUuidList elements(long[] elements) {
		this.elements = elements;
		this.size = elements.length;
		return this;
	}

	/**
	 * Ensures that the receiver can hold at least the specified number of
	 * elements without needing to allocate new internal memory. If necessary,
	 * allocates new internal memory and increases the capacity of the receiver.
	 * 
	 * @param minCapacity
	 *            the desired minimum capacity.
	 */
        @Override
	public void ensureCapacity(int minCapacity) {
		elements = Arrays.ensureCapacity(elements, minCapacity);
	}

	/**
	 * Compares the specified Object with the receiver. Returns true if and only
	 * if the specified Object is also an ArrayList of the same type, both Lists
	 * have the same size, and all corresponding pairs of elements in the two
	 * Lists are identical. In other words, two Lists are defined to be equal if
	 * they contain the same elements in the same order.
	 * 
	 * @param otherObj
	 *            the Object to be compared for equality with the receiver.
	 * @return true if the specified Object is equal to the receiver.
	 */
        @Override
	public boolean equals(Object otherObj) { // delta
		// overridden for performance only.
		if (!(otherObj instanceof UuidArrayList))
			return super.equals(otherObj);
		if (this == otherObj)
			return true;
		if (otherObj == null)
			return false;
		UuidArrayList other = (UuidArrayList) otherObj;
		if (size() != other.size())
			return false;

		long[] theElements = elements();
		long[] otherElements = other.elements();
		for (int i = size(); --i >= 0;) {
			if (theElements[i] != otherElements[i])
				return false;
		}
		return true;
	}

	/**
	 * Applies a procedure to each element of the receiver, if any. Starts at
	 * index 0, moving rightwards.
	 * 
	 * @param procedure
	 *            the procedure to be applied. Stops iteration if the procedure
	 *            returns <tt>false</tt>, otherwise continues.
	 * @return <tt>false</tt> if the procedure stopped before all elements where
	 *         iterated over, <tt>true</tt> otherwise.
	 */
        @Override
	public boolean forEach(UuidProcedure procedure) {
		// overridden for performance only.
		long[] theElements = elements;
		int theSize = size;
		for (int i = 0; i < theSize; i++) {
			int msb = i * 2;
			int lsb = msb + 1;
			long[] uuid = new long[2];
			uuid[0] = theElements[msb];
			uuid[0] = theElements[lsb];
			if (!procedure.apply(uuid))
				return false;
		}
		return true;
	}

	/**
	 * Returns the element at the specified position in the receiver.
	 * 
	 * @param index
	 *            index of element to return.
	 * @param nid 
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (index &lt; 0 || index &gt;=
	 *                size()).
	 */
	public long[] get(int index, int nid) {
		// overridden for performance only.
		assert index < size: " index out of bounds. index: " + index + 
			" current size: " + size + " nid: " + nid;
		assert index >= 0:  " index out of bounds (cannot be netagive). index: " + index + 
			" current size: " + size + " nid: " + nid;
		return getUuid(index);
	}

	private long[] getUuid(int index) {
		int msb = index * 2;
		int lsb = msb + 1;
		long[] returnValue = new long[2];
		returnValue[0] = elements[msb];
		returnValue[1] = elements[lsb];
		return returnValue;
	}

	/**
	 * Returns the element at the specified position in the receiver;
	 * <b>WARNING:</b> Does not check preconditions. Provided with invalid
	 * parameters this method may return invalid elements without throwing any
	 * exception! <b>You should only use this method when you are absolutely
	 * sure that the index is within bounds.</b> Precondition (unchecked):
	 * <tt>index &gt;= 0 && index &lt; size()</tt>.
	 * 
	 * @param index
	 *            index of element to return.
	 */
        @Override
	public long[] getQuick(int index) {
		return getUuid(index);
	}

	/**
	 * Returns the index of the first occurrence of the specified element.
	 * Returns <code>-1</code> if the receiver does not contain this element.
	 * Searches between <code>from</code>, inclusive and <code>to</code>,
	 * inclusive. Tests for identity.
	 * 
	 * @param element
	 *            element to search for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @return the index of the first occurrence of the element in the receiver;
	 *         returns <code>-1</code> if the element is not found.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (
	 *                <tt>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</tt>
	 *                ).
	 */
	public int indexOfFromTo(long element, int from, int to) {
		// overridden for performance only.
		if (size == 0)
			return -1;
		checkRangeFromTo(from, to, size);

		long[] theElements = elements;
		for (int i = from; i <= to; i++) {
			if (element == theElements[i]) {
				return i;
			} // found
		}
		return -1; // not found
	}

	/**
	 * Returns the index of the last occurrence of the specified element.
	 * Returns <code>-1</code> if the receiver does not contain this element.
	 * Searches beginning at <code>to</code>, inclusive until <code>from</code>,
	 * inclusive. Tests for identity.
	 * 
	 * @param element
	 *            element to search for.
	 * @param from
	 *            the leftmost search position, inclusive.
	 * @param to
	 *            the rightmost search position, inclusive.
	 * @return the index of the last occurrence of the element in the receiver;
	 *         returns <code>-1</code> if the element is not found.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (
	 *                <tt>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</tt>
	 *                ).
	 */
	public int lastIndexOfFromTo(long element, int from, int to) {
		// overridden for performance only.
		if (size == 0)
			return -1;
		checkRangeFromTo(from, to, size);

		long[] theElements = elements;
		for (int i = to; i >= from; i--) {
			if (element == theElements[i]) {
				return i;
			} // found
		}
		return -1; // not found
	}

	/**
	 * Returns a new list of the part of the receiver between <code>from</code>,
	 * inclusive, and <code>to</code>, inclusive.
	 * 
	 * @param from
	 *            the index of the first element (inclusive).
	 * @param to
	 *            the index of the last element (inclusive).
	 * @return a new list
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (
	 *                <tt>size()&gt;0 && (from&lt;0 || from&gt;to || to&gt;=size())</tt>
	 *                ).
	 */
        @Override
	public AbstractUuidList partFromTo(int from, int to) {
		if (size == 0)
			return new UuidArrayList(0);

		checkRangeFromTo(from, to, size);

		long[] part = new long[to - from + 1];
		System.arraycopy(elements, from, part, 0, to - from + 1);
		return new UuidArrayList(part);
	}

	/**
	 * Removes from the receiver all elements that are contained in the
	 * specified list. Tests for identity.
	 * 
	 * @param other
	 *            the other list.
	 * @return <code>true</code> if the receiver changed as a result of the
	 *         call.
	 */
        @Override
	public boolean removeAll(AbstractUuidList other) {
		// overridden for performance only.
		if (!(other instanceof UuidArrayList))
			return super.removeAll(other);

		/*
		 * There are two possibilities to do the thing a) use other.indexOf(...)
		 * b) sort other, then use other.binarySearch(...)
		 * 
		 * Let's try to figure out which one is faster. Let M=size,
		 * N=other.size, then a) takes O(M*N) steps b) takes O(N*logN + M*logN)
		 * steps (sorting is O(N*logN) and binarySearch is O(logN))
		 * 
		 * Hence, if N*logN + M*logN < M*N, we use b) otherwise we use a).
		 */
		if (other.size() == 0) {
			return false;
		} // nothing to do
		int limit = other.size() - 1;
		int j = 0;
		long[] theElements = elements;
		int mySize = size();

		double N = (double) other.size();
		double M = (double) mySize;
		if ((N + M) * Arithmetic.log2(N) < M * N) {
			// it is faster to sort other before searching in it
			UuidArrayList sortedList = (UuidArrayList) other.clone();
			sortedList.quickSort();

			for (int i = 0; i < mySize; i++) {
				int msb = i * 2;
				int lsb = msb + 1;
				long[] key = new long[2];
				key[0] = theElements[msb];
				key[1] = theElements[lsb];
				if (sortedList.binarySearchFromTo(key, 0, limit) < 0) {
					theElements[2 * j] = theElements[2 * i];
					theElements[2 * j + 1] = theElements[2 * i + 1];
					j++;
				}
			}
		} else {
			// it is faster to search in other without sorting
			for (int i = 0; i < mySize; i++) {
				int msb = i * 2;
				int lsb = msb + 1;
				long[] key = new long[2];
				key[0] = theElements[msb];
				key[1] = theElements[lsb];
				if (other.indexOfFromTo(key, 0, limit) < 0) {
					theElements[2 * j] = theElements[2 * i];
					theElements[2 * j + 1] = theElements[2 * i + 1];
					j++;
				}
			}
		}

		boolean modified = (j != mySize);
		setSize(j);
		return modified;
	}

	/**
	 * Replaces a number of elements in the receiver with the same number of
	 * elements of another list. Replaces elements in the receiver, between
	 * <code>from</code> (inclusive) and <code>to</code> (inclusive), with
	 * elements of <code>other</code>, starting from <code>otherFrom</code>
	 * (inclusive).
	 * 
	 * @param from
	 *            the position of the first element to be replaced in the
	 *            receiver
	 * @param to
	 *            the position of the last element to be replaced in the
	 *            receiver
	 * @param other
	 *            list holding elements to be copied into the receiver.
	 * @param otherFrom
	 *            position of first element within other list to be copied.
	 */
        @Override
	public void replaceFromToWithFrom(int from, int to, AbstractUuidList other,
			int otherFrom) {
		// overridden for performance only.
		if (!(other instanceof UuidArrayList)) {
			// slower
			super.replaceFromToWithFrom(from, to, other, otherFrom);
			return;
		}
		int length = to - from + 1;
		if (length > 0) {
			checkRangeFromTo(from, to, size());
			checkRangeFromTo(otherFrom, otherFrom + length - 1, other.size());
			System.arraycopy(((UuidArrayList) other).elements, otherFrom,
					elements, from, length);
		}
	}

	/**
	 * Retains (keeps) only the elements in the receiver that are contained in
	 * the specified other list. In other words, removes from the receiver all
	 * of its elements that are not contained in the specified other list.
	 * 
	 * @param other
	 *            the other list to test against.
	 * @return <code>true</code> if the receiver changed as a result of the
	 *         call.
	 */
        @Override
	public boolean retainAll(AbstractUuidList other) {
		// overridden for performance only.
		if (!(other instanceof UuidArrayList))
			return super.retainAll(other);

		/*
		 * There are two possibilities to do the thing a) use other.indexOf(...)
		 * b) sort other, then use other.binarySearch(...)
		 * 
		 * Let's try to figure out which one is faster. Let M=size,
		 * N=other.size, then a) takes O(M*N) steps b) takes O(N*logN + M*logN)
		 * steps (sorting is O(N*logN) and binarySearch is O(logN))
		 * 
		 * Hence, if N*logN + M*logN < M*N, we use b) otherwise we use a).
		 */
		int limit = other.size() - 1;
		int j = 0;
		long[] theElements = elements;
		int mySize = size();

		double N = (double) other.size();
		double M = (double) mySize;
		if ((N + M) * Arithmetic.log2(N) < M * N) {
			// it is faster to sort other before searching in it
			UuidArrayList sortedList = (UuidArrayList) other.clone();
			sortedList.quickSort();

			for (int i = 0; i < mySize; i++) {
				int msb = i * 2;
				int lsb = msb + 1;
				long[] key = new long[2];
				key[0] = theElements[msb];
				key[1] = theElements[lsb];
				if (sortedList.binarySearchFromTo(key, 0, limit) >= 0) {
					theElements[2 * j] = theElements[2 * i];
					theElements[2 * j + 1] = theElements[2 * i + 1];
					j++;
				}
			}
		} else {
			// it is faster to search in other without sorting
			for (int i = 0; i < mySize; i++) {
				int msb = i * 2;
				int lsb = msb + 1;
				long[] key = new long[2];
				key[0] = theElements[msb];
				key[1] = theElements[lsb];
				if (other.indexOfFromTo(key, 0, limit) >= 0) {
					theElements[2 * j] = theElements[2 * i];
					theElements[2 * j + 1] = theElements[2 * i + 1];
					j++;
				}
			}
		}

		boolean modified = (j != mySize);
		setSize(j);
		return modified;
	}

	/**
	 * Reverses the elements of the receiver. Last becomes first, second last
	 * becomes second first, and so on.
	 */
        @Override
	public void reverse() {
		// overridden for performance only.
		long[] tmp = new long[2];
		int limit = size / 2;
		int j = size - 1;

		long[] theElements = elements;
		for (int i = 0; i < limit;) { // swap
			tmp[0] = theElements[i*2];
			tmp[1] = theElements[i*2 + 1];
			theElements[i*2] = theElements[j*2];
			theElements[i*2 + 1] = theElements[j*2 + 1];
			i++;
			theElements[j*2] = tmp[0];
			theElements[j*2 + 1] = tmp[1];
			j--;
		}
	}

	/**
	 * Replaces the element at the specified position in the receiver with the
	 * specified element.
	 * 
	 * @param index
	 *            index of element to replace.
	 * @param element
	 *            element to be stored at the specified position.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (index &lt; 0 || index &gt;=
	 *                size()).
	 */
	public void set(int index, long element) {
		// overridden for performance only.
		if (index >= size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ size);
		elements[index] = element;
	}

	/**
	 * Replaces the element at the specified position in the receiver with the
	 * specified element; <b>WARNING:</b> Does not check preconditions. Provided
	 * with invalid parameters this method may access invalid indexes without
	 * throwing any exception! <b>You should only use this method when you are
	 * absolutely sure that the index is within bounds.</b> Precondition
	 * (unchecked): <tt>index &gt;= 0 && index &lt; size()</tt>.
	 * 
	 * @param index
	 *            index of element to replace.
	 * @param element
	 *            element to be stored at the specified position.
	 */
	public void setQuick(int index, long element) {
		elements[index] = element;
	}

	/**
	 * Sorts the specified range of the receiver into ascending order.
	 * 
	 * The sorting algorithm is dynamically chosen according to the
	 * characteristics of the data set. Currently quicksort and countsort are
	 * considered. Countsort is not always applicable, but if applicable, it
	 * usually outperforms quicksort by a factor of 3-4.
	 * 
	 * <p>
	 * Best case performance: O(N).
	 * <dt>Worst case performance: O(N^2) (a degenerated quicksort).
	 * <dt>Best case space requirements: 0 KB.
	 * <dt>Worst case space requirements: 40 KB.
	 * 
	 * @param from
	 *            the index of the first element (inclusive) to be sorted.
	 * @param to
	 *            the index of the last element (inclusive) to be sorted.
	 * @exception IndexOutOfBoundsException
	 *                index is out of range (<tt>size()&gt;0 && (from&lt;0 ||
	 *                from&gt;to || to&gt;=size())</tt>).
	 */
        @Override
	public void sortFromTo(int from, int to) {
		if (size == 0)
			return;
		checkRangeFromTo(from, to, size);
		quickSortFromTo(from, to);
	}

	/**
	 * Trims the capacity of the receiver to be the receiver's current size.
	 * Releases any superfluous internal memory. An application can use this
	 * operation to minimize the storage of the receiver.
	 */
        @Override
	public void trimToSize() {
		elements = Arrays.trimToCapacity(elements, size() * 2 + 1);
	}

	@Override
	protected void setQuick(int index, long[] element) {
		elements[index * 2] = element[0];
		elements[index * 2 + 1] = element[1];
	}

	public ArrayList<UUID> toList() {
		ArrayList<UUID> resultList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			resultList.add(new UUID(elements[i * 2], elements[i * 2 + 1]));
		}
		return resultList;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	public int getCapacity() {
		return elements.length / 2;
	}
}
