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

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author dylangrald
 */
public class ConcurrentBitSet implements NativeIdSetBI {

    private final int          offset      = Integer.MIN_VALUE;

    private static final int BITS_PER_UNIT = 64;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile AtomicLongArray units;

    public ConcurrentBitSet() {
        this(BITS_PER_UNIT);
    }

    public ConcurrentBitSet(int bitCapacity) {
        units = new AtomicLongArray(1 + (bitCapacity - 1) / BITS_PER_UNIT);
    }

    public ConcurrentBitSet(BitSet bitSet) {
        this(bitSet.length());
        for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
            set(bit);
        }
    }
    
    public ConcurrentBitSet(NativeIdSetBI nativeIdSet) {
        if (nativeIdSet instanceof ConcurrentBitSet) {
            ConcurrentBitSet other = (ConcurrentBitSet) nativeIdSet;
            units = new AtomicLongArray(1 + (other.size() - 1) / BITS_PER_UNIT);
            privateOr(other);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public ConcurrentBitSet(String bitString) {
        this(bitString.length());
        for (int ii = 0; ii < bitString.length(); ii++) {
            if (bitString.charAt(ii) == '1') {
                set(ii);
            }
        }
    }

    private ConcurrentBitSet(AtomicLongArray array) {
        units = array;
    }

    public AtomicLongArray getUnits() {
        return units;
    }

    //CONDITION: no lock held
    private void ensureCapacityAndAquireReadLock(int unitLen) {
        while (true) {
            lock.readLock().lock();
            if (units.length() >= unitLen) {
                return;//yes, we keep the read lock
            }
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (units.length() <= unitLen) {
                    final AtomicLongArray newUnits = new AtomicLongArray(unitLen);
                    for (int i = 0; i < units.length(); i++) {
                        newUnits.set(i, units.get(i));
                    }
                    units = newUnits;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public boolean get(int bit) {
        bit = bit + offset;
        final int unit = bit / BITS_PER_UNIT;
        final int index = bit % BITS_PER_UNIT;
        final long mask = 1L << index;

        lock.readLock().lock();
        try {
            if (unit >= units.length()) {
                return false;
            }
            return 0 != (units.get(unit) & mask);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void set(int bit, boolean value) {
        bit = bit + offset;
        if (value) {
            set(bit);
        } else {
            clear(bit);
        }
    }

    public final void set(int bit) {
        bit = bit + offset;
        final int unit = bit / BITS_PER_UNIT;
        final int index = bit % BITS_PER_UNIT;
        final long mask = 1L << index;
        ensureCapacityAndAquireReadLock(unit + 1);
        try {
            long old = units.get(unit);
            long upd = old | mask;
            while (!units.compareAndSet(unit, old, upd)) {
                old = units.get(unit);
                upd = old | mask;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear(int bit) {
        bit = bit + offset;
        int unit = bit / BITS_PER_UNIT;
        int index = bit % BITS_PER_UNIT;
        long mask = 1L << index;

        lock.readLock().lock();
        try {
            long old = units.get(unit);
            long upd = old | mask;
            while (!units.compareAndSet(unit, old, upd)) {
                old = units.get(unit);
                upd = old & ~mask;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clearAll() {
        lock.readLock().lock();
        try {
            for (int i = 0; i < units.length(); i++) {
                units.set(i, 0);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public int nextSetBit(int from) {
        from = from + offset;
        final int fromBit = from % BITS_PER_UNIT;
        final int fromUnit = from / BITS_PER_UNIT;
        lock.readLock().lock();
        try {
            final int len = units.length();
            for (int i = Math.max(0, fromUnit); i < len; i++) {
                long nextBit = units.get(i);
                if (nextBit != 0L) {
                    if (i == fromUnit) {
                        nextBit &= (0xffffffffffffffffL << fromBit);
                    }
                    if (nextBit != 0L) {
                        return i * BITS_PER_UNIT + Long.numberOfTrailingZeros(nextBit) + offset;
                    }
                }
            }
            return -1;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int length() {
        int index;
        long last = 0;
        lock.readLock().lock();
        try {
            index = units.length();
            do {
                index--;
            } while (index >= 0 && 0 == (last = units.get(index)));
        } finally {
            lock.readLock().unlock();
        }
        return index < 0 ? 0 : index * BITS_PER_UNIT + bitLenL(last);
    }

    private static int bitLenL(long l) {
        int high = (int) (l >>> 32);
        int low = (int) l;
        return high == 0 ? bitLenI(low) : 32 + bitLenI(high);
    }

    private static int bitLenI(int w) {
        // Binary search - decision tree (5 tests, rarely 6)
        return (w < 1 << 15
                ? (w < 1 << 7
                ? (w < 1 << 3
                ? (w < 1 << 1 ? (w < 1 << 0 ? (w < 0 ? 32 : 0) : 1) : (w < 1 << 2 ? 2 : 3))
                : (w < 1 << 5 ? (w < 1 << 4 ? 4 : 5) : (w < 1 << 6 ? 6 : 7)))
                : (w < 1 << 11
                ? (w < 1 << 9 ? (w < 1 << 8 ? 8 : 9) : (w < 1 << 10 ? 10 : 11))
                : (w < 1 << 13 ? (w < 1 << 12 ? 12 : 13) : (w < 1 << 14 ? 14 : 15))))
                : (w < 1 << 23
                ? (w < 1 << 19
                ? (w < 1 << 17 ? (w < 1 << 16 ? 16 : 17) : (w < 1 << 18 ? 18 : 19))
                : (w < 1 << 21 ? (w < 1 << 20 ? 20 : 21) : (w < 1 << 22 ? 22 : 23)))
                : (w < 1 << 27
                ? (w < 1 << 25 ? (w < 1 << 24 ? 24 : 25) : (w < 1 << 26 ? 26 : 27))
                : (w < 1 << 29 ? (w < 1 << 28 ? 28 : 29) : (w < 1 << 30 ? 30 : 31)))));
    }

    public void and(ConcurrentBitSet with) {
        if (this == with) {
            return;
        }

        while (true) {
            lock.readLock().lock();
            try {
                if (with.lock.readLock().tryLock()) {
                    final int len;
                    try {
                        len = Math.min(units.length(), with.units.length());
                        for (int ii = 0; ii < len; ii++) {
                            long old = units.get(ii);
                            long upd = old & with.units.get(ii);
                            while (!units.compareAndSet(ii, old, upd)) {
                                old = units.get(ii);
                                upd = old & with.units.get(ii);
                            }
                        }
                    } finally {
                        with.lock.readLock().unlock();
                    }
                    for (int ii = len; ii < units.length(); ii++) {
                        units.set(ii, 0);
                    }
                    return;
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    public void or(ConcurrentBitSet with) {
        privateOr(with);
    }

    /**
     * 347 * This set becomes this ^ with 348
     */
    public void xor(ConcurrentBitSet with) {
        while (true) {
            final int withLength;
            with.lock.readLock().lock();
            try {
                withLength = with.units.length();
            } finally {
                with.lock.readLock().lock();
            }
            //(i) see comment below
            ensureCapacityAndAquireReadLock(withLength);
            try {
                if (with.lock.readLock().tryLock()) {
                    try {
                        final int len = with.units.length();
                        if (units.length() >= len) {//else could happen if changed at (i)
                            for (int i = 0; i < len; i++) {
                                long old = units.get(i);
                                long upd = old ^ with.units.get(i);
                                while (!units.compareAndSet(i, old, upd)) {
                                    old = units.get(i);
                                    upd = old ^ with.units.get(i);
                                }
                            }
                            return;
                        }
                    } finally {
                        with.lock.readLock().unlock();
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    /**
     * This set becomes this & not with
     */
    public void andNot(ConcurrentBitSet with) {
        //with is always true in the large parts, and thus always larger
        //thus, this is always directing the new length
        while (true) {
            lock.readLock().lock();
            try {
                if (with.lock.readLock().tryLock()) {
                    try {
                        final int len = Math.min(units.length(), with.units.length());
                        for (int ii = 0; ii < len; ii++) {
                            long old = units.get(ii);
                            long upd = old & ~with.units.get(ii);
                            while (!units.compareAndSet(ii, old, upd)) {
                                old = units.get(ii);
                                upd = old & ~with.units.get(ii);
                            }
                        }
                        return;
                    } finally {
                        with.lock.readLock().unlock();
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    public long[] toLongArray() {
        return toLongArray(null, 0);
    }

    public long[] toLongArray(long[] arr, int offset) {
        lock.readLock().lock();
        try {
            final int len = units.length();
            if (arr == null || arr.length < len + offset) {
                arr = new long[len + offset];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = units.get(i);
            }
            return arr;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int[] toIntArray() {
        int lengthOfBitSet = this.cardinality();
        int[] intArray = new int[this.cardinality()];
        int bit = this.nextSetBit(0);
        int count = 0;
        while (count < lengthOfBitSet) {
            intArray[count] = this.nextSetBit(bit);
            bit = this.nextSetBit(bit + 1);
            count++;
        }
        return intArray;

    }

    public int[] toIntArray(int[] arr, int offset) {
        lock.readLock().lock();
        try {
            final int len = units.length();
            if (arr == null || arr.length < len + offset) {
                arr = new int[len + offset];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = (int) units.get(i) + this.offset;
            }
            return arr;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int cardinality() {
        lock.readLock().lock();
        try {
            int card = 0;
            final int len = units.length();
            for (int i = 0; i < len; i++) {
                card += Long.bitCount(units.get(i));
            }
            return card;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * public I_IterateIds iterator() { return new
     * NidIterator(bitSet.iterator()); }
     *
     * private class NidIterator implements I_IterateIds {
     *
     * private DocIdSetIterator docIterator;
     *
     * //~--- constructors
     * ----------------------------------------------------- private
     * NidIterator(DocIdSetIterator docIterator) { super(); this.docIterator =
     * docIterator; }
     *
     * //~--- methods
     * ----------------------------------------------------------
     *
     * @Override public boolean next() throws IOException { return
     * docIterator.nextDoc() != DocIdSetIterator.NO_MORE_DOCS; }
     *
     * @Override public int nid() { return docIterator.docID(); }
     *
     * @Override public boolean skipTo(int target) throws IOException { return
     * docIterator.advance(target) != DocIdSetIterator.NO_MORE_DOCS; }
     *
     * @Override public String toString() { StringBuilder buff = new
     * StringBuilder();
     *
     * buff.append("NidIterator: nid: "); buff.append(nid()); return
     * buff.toString(); } }
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');

        lock.readLock().lock();
        try {
            final int unitLen = units.length();
            for (int ii = 0; ii < unitLen; ii++) {
                final int len = ii == unitLen - 1 ? 1 + (length() - 1) % BITS_PER_UNIT : BITS_PER_UNIT;
                final long unit = units.get(ii);
                if (unit != 0) {
                    long bit = 1L;
                    for (int jj = 0; jj < len; jj++, bit <<= 1) {
                        if ((unit & bit) != 0) {
                            sb.append('1');
                        } else {
                            sb.append('0');
                        }
                    }
                } else {
                    sb.append("0000000000000000000000000000000000000000000000000000000000000000", 0, len);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        sb.append('}');
        return sb.toString();
    }

    private class Iterator implements NativeIdSetItrBI {
        
        int currentBit = 0;

        @Override
        public int nid() {
            return currentBit;
        }

        @Override
        public boolean next() throws IOException {
            if (currentBit != -1) {
                currentBit = nextSetBit(currentBit+1);
            }
            return currentBit != -1;
        }
        
    }
    @Override
    public NativeIdSetItrBI getIterator() {
        return new Iterator();
    }

    @Override
    public int size() {
        return this.cardinality();
    }

    @Override
    public boolean isMember(int nid) {
        return this.get(nid);
    }

    @Override
    public void setMember(int nid) {
        this.set(nid);
    }

    @Override
    public void and(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void or(NativeIdSetBI other) {
        if (other instanceof ConcurrentBitSet) {
            or((ConcurrentBitSet) other);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public void xor(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(int nid) {
        return this.get(nid);
    }

    @Override
    public int[] getSetValues() {
        return this.toIntArray();

    }

    @Override
    public void add(int nid) {
        this.set(nid);
    }

    @Override
    public void addAll(int[] nids) {
        for (int i : nids) {
            this.set(i);
        }
    }

    @Override
    public void remove(int nid) {
        nid = nid + offset;
        this.clear(nid);
    }

    @Override
    public void removeAll(int[] nids) {
        for (int i : nids) {
            int index = i + offset;
            this.clear(index);
        }
    }

    @Override
    public void clear() {
        this.clearAll();
    }

    @Override
    public int getMax() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMin() {
        if (this.cardinality() == 0) {
            return offset;
        } else {
            int min = 0;
            return this.nextSetBit(min) + offset;
        }
    }

    @Override
    public boolean contiguous() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void union(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setNotMember(int nid) {
        nid = nid + offset;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void andNot(NativeIdSetBI other) {
    }

    @Override
    public boolean isEmpty() {
        int index;
        lock.readLock().lock();
        try {
            index = units.length();
            do {
                index--;
            } while (index >= 0 && 0 == units.get(index));
        } finally {
            lock.readLock().unlock();
        }
        return index < 0;
    }

    private void privateOr(ConcurrentBitSet with) {
        if (this == with) {
            return;
        }
        while (true) {
            final int withLength;
            with.lock.readLock().lock();
            try {
                withLength = with.units.length();
            } finally {
                with.lock.readLock().unlock();
            }
            //(i) see comment below
            ensureCapacityAndAquireReadLock(withLength);
            try {
                if (with.lock.readLock().tryLock()) {
                    try {
                        final int len = with.units.length();
                        if (units.length() >= len) {
                            for (int i = 0; i < len; i++) {//else could happen if changed at (i)                                                    for (int i = 0; i < len; i++) {
                                long old = units.get(i);
                                long upd = old | with.units.get(i);
                                while (!units.compareAndSet(i, old, upd)) {
                                    old = units.get(i);
                                    upd = old | with.units.get(i);
                                }
                            }
                            return;
                        }
                    } finally {
                        with.lock.readLock().unlock();
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
