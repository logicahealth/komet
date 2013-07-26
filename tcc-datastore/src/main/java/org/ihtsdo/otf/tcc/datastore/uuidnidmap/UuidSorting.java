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

/**
 *
 * @author kec
 */
public class UuidSorting extends Object {

    private static final int SMALL = 7;
    private static final int MEDIUM = 40;

    /**
     * Makes this class non instantiable, but still let's others inherit from it.
     */
    protected UuidSorting() {
    }

    /**
     * Searches the list for the specified value using the binary search algorithm. The list must
     * <strong>must</strong> be sorted (as by the sort method) prior to making this call. If it is not sorted,
     * the results are undefined: in particular, the call may enter an infinite loop. If the list contains
     * multiple elements equal to the specified key, there is no guarantee which of the multiple elements will
     * be found.
     *
     * @param list the list to be searched.
     * @param key the value to be searched for.
     * @param from the leftmost search position, inclusive.
     * @param to the rightmost search position, inclusive.
     * @return index of the search key, if it is contained in the list; otherwise, <tt>(-(<i>insertion
     * point</i>) - 1)</tt>. The <i>insertion point</i> is defined as the the point at which the value would
     * be inserted into the list: the index of the first element greater than the key, or
     * <tt>list.length</tt>, if all elements in the list are less than the specified key. Note that this
     * guarantees that the return value will be &gt;= 0 if and only if the key is found.
     * @see java.util.Arrays
     */
    public static int binarySearchFromTo(long[] list, long[] key, int from, int to) {
        long[] midVal = new long[2];
        while (from <= to) {
            int mid = (from + to) / 2;
            midVal[0] = list[mid * 2];
            midVal[1] = list[mid * 2 + 1];
            if (midVal[0] < key[0] || midVal[0] == key[0] && midVal[1] < key[1]) {
                from = mid + 1;
            } else if (midVal[0] > key[0] || midVal[0] == key[0] && midVal[1] > key[1]) {
                to = mid - 1;
            } else {
                return mid;
            } // key found
        }
        return -(from + 1); // key not found.
    }

    /**
     * Returns the index of the median of the three indexed chars.
     */
    private static int med3(long x[], int a, int b, int c, UuidComparatorBI comp) {
        int ab = comp.compare(x[2 * a], x[2 * a + 1], x[2 * b], x[2 * b + 1]);
        int ac = comp.compare(x[2 * a], x[2 * a + 1], x[2 * c], x[2 * c + 1]);
        int bc = comp.compare(x[2 * b], x[2 * b + 1], x[2 * c], x[2 * c + 1]);
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b
                : ac > 0 ? c : a));
    }

    /**
     * Sorts the specified range of the specified array of elements.
     *
     * <p> This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of
     * the sort. <p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in
     * the low sublist is less than the lowest element in the high sublist). This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly sorted lists.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; a.length</tt>
     */
    public static void mergeSort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        long aux[] = (long[]) a.clone();
        mergeSort1(aux, a, fromIndex, toIndex);
    }

    /**
     * Sorts the specified range of the specified array of elements according to the order induced by the
     * specified comparator. All elements in the range must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt> for any
     * elements <tt>e1</tt> and <tt>e2</tt> in the range). <p>
     *
     * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the
     * sort. <p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in
     * the low sublist is less than the lowest element in the high sublist). This algorithm offers guaranteed
     * n*log(n) performance, and can approach linear performance on nearly sorted lists.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @param c the comparator to determine the order of the array.
     * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> using
     * the specified comparator.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; a.length</tt>
     * @see Comparator
     */
    public static void mergeSort(long[] a, int fromIndex, int toIndex,
            UuidComparatorBI c) {
        rangeCheck(a.length, fromIndex, toIndex);
        long aux[] = (long[]) a.clone();
        mergeSort1(aux, a, fromIndex, toIndex, c);
    }

    private static void mergeSort1(long src[], long dest[], int low, int high) {
        int length = high - low;
        // Insertion sort on smallest arrays
        if (length < SMALL) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low
                        && (dest[2 * j - 2] > dest[2 * j]
                        || (dest[2 * j - 2] == dest[2 * j] && dest[2 * j - 1] > dest[2 * j + 1])); j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) / 2;
        mergeSort1(dest, src, low, mid);
        mergeSort1(dest, src, mid, high);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (src[2 * mid - 2] < src[2 * mid] || (src[2 * mid - 2] == src[2 * mid] && src[2 * mid - 1] <= src[2 * mid + 1])) {
            System.arraycopy(src, 2 * low, dest, 2 * low, length * 2 + 1);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && (src[2 * p] < src[2 * q])
                    || ((src[2 * p] == src[2 * q]) && (src[2 * p + 1] <= src[2 * q + 1]))) {
                dest[2 * i] = src[p * 2];
                dest[2 * i + 1] = src[p * 2 + 1];
                p++;
            } else {
                dest[2 * i] = src[2 * q];
                dest[2 * i + 1] = src[2 * q + 1];
                q++;
            }
        }
    }

    private static void mergeSort1(long src[], long dest[], int low, int high,
            UuidComparatorBI c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < SMALL) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && c.compare(dest[2 * j - 2], dest[2 * j - 1], dest[j * 2], dest[j * 2 + 1]) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) / 2;
        mergeSort1(dest, src, low, mid, c);
        mergeSort1(dest, src, mid, high, c);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[2 * mid - 2], src[2 * mid - 1], src[2 * mid], src[2 * mid + 1]) <= 0) {
            System.arraycopy(src, low * 2, dest, low * 2, length * 2 + 1);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && c.compare(src[2 * p], src[2 * p + 1], src[2 * q], src[2 * q + 1]) <= 0) {
                dest[2 * i] = src[2 * p];
                dest[2 * i + 1] = src[2 * p + 1];
                p++;
            } else {
                dest[2 * i] = src[2 * q];
                dest[2 * i + 1] = src[2 * q + 1];
                q++;
            }
        }
    }

    /**
     * Sorts the specified range of the specified array of elements according to the order induced by the
     * specified comparator. All elements in the range must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt> for any
     * elements <tt>e1</tt> and <tt>e2</tt> in the range). <p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's
     * "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993). This algorithm offers n*log(n) performance on many data sets that cause other quicksorts to
     * degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @param c the comparator to determine the order of the array.
     * @throws ClassCastException if the array contains elements that are not <i>mutually comparable</i> using
     * the specified comparator.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt; a.length</tt>
     * @see Comparator
     */
    public static void quickSort(long[] a, int fromIndex, int toIndex,
            UuidComparatorBI c) {
        rangeCheck(a.length, fromIndex, toIndex);
        quickSort1(a, fromIndex, toIndex - fromIndex, c);
    }

    /**
     * Sorts the specified sub-array of chars into ascending order.
     */
    private static void quickSort1(long x[], int off, int len,
            UuidComparatorBI comp) {
        // Insertion sort on smallest arrays
        if (len < SMALL) {
            for (int i = off; i < len + off; i++) {
                for (int j = i; j > off && comp.compare(x[ 2 * j - 2], x[ 2 * j - 1], x[2 * j], x[2 * j + 1]) > 0; j--) {
                    swap(x, j, j - 1);
                }
            }
            return;
        }

        // Choose a partition element, v
        int m = off + len / 2; // Small arrays, middle element
        if (len > SMALL) {
            int l = off;
            int n = off + len - 1;
            if (len > MEDIUM) { // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s, comp);
                m = med3(x, m - s, m, m + s, comp);
                n = med3(x, n - 2 * s, n - s, n, comp);
            }
            m = med3(x, l, m, n, comp); // Mid-size, med of 3
        }
        long[] v = new long[2];
        v[0] = x[m * 2];
        v[1] = x[m * 2 + 1];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(x[2 * b], x[2 * b + 1], v[0], v[1])) <= 0) {
                if (comparison == 0) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = comp.compare(x[2 * c], x[2 * c + 1], v[0], v[1])) >= 0) {
                if (comparison == 0) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort1(x, off, s, comp);
        }
        if ((s = d - c) > 1) {
            quickSort1(x, n - s, s, comp);
        }
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an appropriate exception if they aren't.
     */
    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLen * 2 - 1) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(long x[], int a, int b) {
        int aMsb = a * 2;
        int aLsb = aMsb + 1;
        int bMsb = b * 2;
        int bLsb = bMsb + 1;
        long[] t = new long[2];
        t[0] = x[aMsb];
        t[1] = x[aLsb];
        x[aMsb] = x[bMsb];
        x[aLsb] = x[bLsb];
        x[bMsb] = t[0];
        x[bLsb] = t[1];
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(long x[], int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, a, b);
        }
    }
}
