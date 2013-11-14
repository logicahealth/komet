/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

/**
 *
 * @author kec
 */
public interface NativeIdSetBI {

    /**
     * For iteration over only the member bits in the set.
     *
     * @return
     */
    NativeIdSetItrBI getSetBitIterator();

    /**
     * For iteration over all bits in the set (member or not).
     *
     * @return
     */
    NativeIdSetItrBI getAllBitIterator();

    /**
     *
     * @return the number of members in the set. Sometimes referred to as
     * cardinality.
     */
    int size();

    /**
     *
     * @return the largest possible id in this set,      * or <code>Integer.MAX_VALUE</code> if the largest possible id is
     * unknown. Knowing the span of identifiers is required to
     * support <code>not</code> operations and <code>xor</code> operations.
     */
    int getMaxPossibleId();

    /**
     *
     * @return the smallest possible id in this set,      * or <code>Integer.MIN_VALUE</code> if the smallest possible id is
     * unknown. Knowing the span of identifiers is required to
     * support <code>not</code> operations and <code>xor</code> operations.
     */
    int getMinPossibleId();

    /**
     *
     * @param nid the largest possible id in this set, or Integer.MAX_VALUE if
     * the largest possible id is unknown. Knowing the span of identifiers is
     * required to support <code>not</code> operations and <code>xor</code>
     * operations.
     */
    void setMaxPossibleId(int nid);

    boolean isMember(int nid);

    void setMember(int nid);

    void and(NativeIdSetBI other);

    void or(NativeIdSetBI other);

    void xor(NativeIdSetBI other);

    boolean contains(int nid);

    int[] getSetValues();

    void add(int nid);

    void addAll(int[] nids);

    void remove(int nid);

    void removeAll(int[] nids);

    void clear();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    /**
     *
     * @return the largest nid that is a member of the set.
     */
    int getMax();

    int getMin();

    boolean contiguous();

    @Override
    String toString();

    void union(NativeIdSetBI other);

    void setNotMember(int nid);

    void andNot(NativeIdSetBI other);

    boolean isEmpty();

    /**
     * Adds all of the
     * <code>int</code> values from Integer.MIN_VALUE + 1 to (Integer.MIN_VALUE
     * + max) to the set
     *
     * @param max
     */
    void setAll(int max);
}
