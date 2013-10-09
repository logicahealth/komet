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

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylangrald
 */
public class ConcurrentBitSet implements NativeIdSetBI {

    private static final int BITS_PER_UNIT = 64;
    private static final int UNITS_PER_SET = 10240;
    private static final int BITS_PER_SET = BITS_PER_UNIT * UNITS_PER_SET;
    private static final int MAX_STRING_LENGTH = 512;
    private static final OrProcessUnits orProcessor = new OrProcessUnits();
    private static final XorProcessUnits xorProcessor = new XorProcessUnits();
    private static final AndProcessUnits andProcessor = new AndProcessUnits();
    private static final AndNotProcessUnits andNotProcessor = new AndNotProcessUnits();
    private final int offset = Integer.MIN_VALUE;
    private final ReentrantLock expansionLock = new ReentrantLock();
    private AtomicInteger usedBits = new AtomicInteger(0);
    private int maxPossibleId = Integer.MIN_VALUE + BITS_PER_SET - 1;
    private final CopyOnWriteArrayList<AtomicLongArray> bitSetList;
    private int bitCapacity;
    Boolean moveToNextUnit = false;
    Boolean iterate = false;

    public ConcurrentBitSet() {
        this(BITS_PER_SET - 1);
    }

    public ConcurrentBitSet(int bitCapacity) {
        if (bitCapacity < 0) {
            bitCapacity = bitCapacity - Integer.MIN_VALUE;
        }

        int numberOfSets = (bitCapacity / BITS_PER_SET) + 1;

        AtomicLongArray[] initialSets = new AtomicLongArray[numberOfSets];

        this.bitCapacity = numberOfSets * BITS_PER_SET;

        maxPossibleId = this.bitCapacity + Integer.MIN_VALUE - 1;

        for (int i = 0; i < numberOfSets; i++) {
            initialSets[i] = new AtomicLongArray(UNITS_PER_SET);
        }

        bitSetList = new CopyOnWriteArrayList<>(initialSets);
    }

    public ConcurrentBitSet(NativeIdSetBI nativeIdSet) {
        this(nativeIdSet.getMaxPossibleId());

        if (nativeIdSet instanceof ConcurrentBitSet) {
            ConcurrentBitSet other = (ConcurrentBitSet) nativeIdSet;

            this.usedBits.set(other.usedBits.get());
            privateOr(other);
        } else {
            NativeIdSetItrBI iter = nativeIdSet.getSetBitIterator();

            try {
                while (iter.next()) {
                    set(iter.nid());
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    @Override
    public int hashCode() {

        // collection values may change.
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ConcurrentBitSet other = (ConcurrentBitSet) obj;
        NativeIdSetItrBI thisIterator = this.getSetBitIterator();
        NativeIdSetItrBI otherIterator = other.getSetBitIterator();

        try {
            while (thisIterator.next() && otherIterator.next()) {
                if (thisIterator.nid() != otherIterator.nid()) {
                    return false;
                }
            }

            if (thisIterator.next() != otherIterator.next()) {
                return false;
            }

            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void ensureCapacity(int bits) {
        if (bits < 0) {
            bits = bits + offset;
        }

        if (bitCapacity > bits) {
            return;
        }

        expansionLock.lock();

        try {
            while (true) {
                if (bitCapacity <= bits) {
                    bitSetList.add(new AtomicLongArray(UNITS_PER_SET));
                    bitCapacity = bitSetList.size() * BITS_PER_SET;
                    maxPossibleId = bitCapacity + Integer.MIN_VALUE - 1;
                } else {
                    return;
                }
            }
        } finally {
            expansionLock.unlock();
        }
    }

    public boolean get(int bit) {
        if (bit < 0) {
            bit = bit + offset;
        }

        if (bit > usedBits.get()) {
            return false;
        }

        final int set = bit / BITS_PER_SET;
        if (set >= bitSetList.size()) {
            return false;
        }
        final int unit = (bit - (set * BITS_PER_SET)) / BITS_PER_UNIT;
        final int index = bit % BITS_PER_UNIT;
        final long mask = 1L << index;

        return 0 != (bitSetList.get(set).get(unit) & mask);
    }

    public void set(int bit, boolean value) {
        if (bit < 0) {
            bit = bit + offset;
        }

        if (value) {
            set(bit);
        } else {
            clear(bit);
        }
    }

    public final void set(int bit) {
        if (bit < 0) {
            bit = bit + offset;
        }

        if (bit > usedBits.get()) {
            usedBits.set(bit);
        }

        ensureCapacity(bit);

        final int set = bit / BITS_PER_SET;
        final int unit = (bit - (set * BITS_PER_SET)) / BITS_PER_UNIT;
        final int unitIndex = bit % BITS_PER_UNIT;
        final long mask = 1L << unitIndex;

        try {
            long old = bitSetList.get(set).get(unit);
            long upd = old | mask;

            while (!bitSetList.get(set).compareAndSet(unit, old, upd)) {
                old = bitSetList.get(set).get(unit);
                upd = old | mask;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw e;
        }
    }

    public void clear(int bit) {
        if (bit < 0) {
            bit = bit + offset;
        }

        if (bit > usedBits.get()) {
            usedBits.set(bit + 1);
        }

        ensureCapacity(bit);

        final int set = bit / BITS_PER_SET;
        final int unit = (bit - (set * BITS_PER_SET)) / BITS_PER_UNIT;
        final int index = bit % BITS_PER_UNIT;
        final long mask = 1L << index;
        long old = bitSetList.get(set).get(unit);
        long upd = old & (~mask);

        while (!bitSetList.get(set).compareAndSet(unit, old, upd)) {
            old = bitSetList.get(set).get(unit);
            upd = old & ~mask;
        }
    }

    public void clearAll() {
        for (int i = 0; i < bitSetList.size(); i++) {
            bitSetList.set(i, new AtomicLongArray(UNITS_PER_SET));
        }
    }

    public int nextSetBit(int from) {

        if (from < 0) {
            from = from + offset;
        }

        if (from != 0 || iterate) {
            from++;
        } else {
            iterate = true;
        }

        if (from > this.usedBits.get()) {
            return -1;
        }

        final int sets = bitSetList.size();
        final int fromSet = from / BITS_PER_SET;
        final int fromUnit = (from - 1 - (fromSet * BITS_PER_SET)) / BITS_PER_UNIT;
        final int fromIndex = from % BITS_PER_UNIT;
        int unitStart = fromUnit;
        int indexStart = fromIndex;

        for (int setIndex = fromSet; setIndex < sets; setIndex++) {
            AtomicLongArray set = bitSetList.get(setIndex);
            for (int i = unitStart; i < UNITS_PER_SET; i++) {
                if (moveToNextUnit) {
                    i++;
                    moveToNextUnit = false;
                    if (i > UNITS_PER_SET) {
                        break;
                    }
                }
                long nextBit = set.get(i);
                if (nextBit != 0L) {
                    if (i == fromUnit) {
                        nextBit &= (0xffffffffffffffffL << indexStart);
                    }
                    if (nextBit != 0L) {
                        int trailingZeros = Long.numberOfTrailingZeros(nextBit);
                        int nextSetBit = (i * BITS_PER_UNIT) + (setIndex * BITS_PER_SET) + trailingZeros;
                        if (nextSetBit % BITS_PER_UNIT + 1 == BITS_PER_UNIT && i != UNITS_PER_SET - 1) {
                            moveToNextUnit = true;
                        }
                        return nextSetBit + offset;
                    }
                }
                indexStart = 0;
                unitStart = 0;
            }
        }

        return -1;

    }

    public int length() {
        return usedBits.get() + 1;
    }

    public void and(ConcurrentBitSet with) {
        if (this == with) {
            return;
        }

        logicallyProcessUnits(with, andProcessor);
    }

    public void or(ConcurrentBitSet with) {
        privateOr(with);
    }

    /**
     *
     */
    public void xor(ConcurrentBitSet other) {
        if (this == other) {
            this.clear();

            return;
        }

        logicallyProcessUnits(other, xorProcessor);
    }

    /**
     * This setIndex becomes this & not with
     */
    public void andNot(ConcurrentBitSet with) {
        logicallyProcessUnits(with, andNotProcessor);
    }

    public int[] toIntArray() {
        int lengthOfBitSet = this.cardinality();
        int[] intArray = new int[this.cardinality()];
        int bit = this.nextSetBit(0);

        intArray[0] = bit;

        for (int i = 1; i < lengthOfBitSet; i++) {
            intArray[i] = nextSetBit(intArray[i - 1]);
        }

        return intArray;
    }

    public int cardinality() {
        int card = 0;
        final int sets = bitSetList.size();

        for (int i = 0; i < sets; i++) {
            AtomicLongArray units = bitSetList.get(i);

            for (int j = 0; j < UNITS_PER_SET; j++) {
                card += Long.bitCount(units.get(j));
            }
        }

        return card;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append('{');

        for (int i = 0; i < bitSetList.size(); i++) {
            AtomicLongArray units = bitSetList.get(i);

            for (int ii = 0; ii < UNITS_PER_SET; ii++) {
                int length = (i * BITS_PER_SET) + (ii * BITS_PER_UNIT);

                if (length < MAX_STRING_LENGTH) {
                    int usedBitCount = usedBits.get();

                    if (length < usedBitCount + 63) {
                        final long unitValue = units.get(ii);
                        int bitsToProcess = Math.min(64, usedBitCount + 63 - length);

                        if (unitValue != 0) {
                            long bit = 1L;

                            for (int jj = 0; jj < bitsToProcess; jj++, bit <<= 1) {
                                if ((unitValue & bit) != 0) {
                                    sb.append('1');
                                } else {
                                    sb.append('0');
                                }
                            }
                        } else {
                            sb.append("0000000000000000000000000000000000000000000000000000000000000000", 0,
                                    bitsToProcess);
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        sb.append('}');

        return sb.toString();
    }

    @Override
    public void setAll(int max) {
        for (int i = Integer.MIN_VALUE; i < max; i++) {
            add(i);
        }
    }

    @Override
    public NativeIdSetItrBI getSetBitIterator() {
        return new SetBitsIterator();
    }

    @Override
    public NativeIdSetItrBI getAllBitIterator() {
        return new AllBitsIterator();
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
        if (other instanceof ConcurrentBitSet) {
            and((ConcurrentBitSet) other);
        } else {
            NativeIdSetItrBI iter = this.getSetBitIterator();

            try {
                while (iter.next()) {
                    if (!other.isMember(iter.nid())) {
                        this.remove(iter.nid());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void or(NativeIdSetBI other) {
        if (other instanceof ConcurrentBitSet) {
            or((ConcurrentBitSet) other);
        } else {
            NativeIdSetItrBI iter = other.getSetBitIterator();

            try {
                while (iter.next()) {
                    if (!this.contains(iter.nid())) {
                        this.add(iter.nid());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void xor(NativeIdSetBI other) {
        if (other instanceof ConcurrentBitSet) {
            xor((ConcurrentBitSet) other);
        } else {
            NativeIdSetItrBI iter = other.getAllBitIterator();

            try {
                while (iter.next()) {
                    if (!this.isMember(iter.nid())) {
                        this.add(iter.nid());
                    } else {
                        this.remove(iter.nid());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean contains(int nid) {
        return this.get(nid);
    }

    public int getOffSet() {
        return this.offset;
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
        if (nid < 0) {
            nid = nid + offset;
        }

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
        if (this.cardinality() == 0) {
            return offset;
        } else {
            NativeIdSetItrBI iter = this.getSetBitIterator();
            int max = 0;

            try {
                while (iter.next()) {
                    max = iter.nid();
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }

            return max;
        }
    }

    @Override
    public int getMin() {
        if (this.cardinality() == 0) {
            return offset;
        } else {
            int min = 0;

            return this.nextSetBit(min);
        }
    }

    @Override
    public boolean contiguous() {
        if ((this.cardinality() == 0) || (this.cardinality() == 1)) {
            return true;
        } else {
            NativeIdSetItrBI iter = this.getSetBitIterator();
            int temp = this.getMin();

            try {
                while (iter.next()) {
                    if (iter.nid() - temp > 1) {
                        return false;
                    }

                    temp = iter.nid();
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }

            return true;
        }
    }

    @Override
    public void union(NativeIdSetBI other) {
        if (other instanceof ConcurrentBitSet) {
            or((ConcurrentBitSet) other);
        } else {
            NativeIdSetItrBI iter = other.getSetBitIterator();
            int max = Integer.MIN_VALUE;
            try {
                while (iter.next()) {
                    if (!this.contains(iter.nid())) {
                        this.add(iter.nid());
                        max = iter.nid();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.maxPossibleId = max + BITS_PER_SET - (max % BITS_PER_SET);
        }

    }

    @Override
    public void setNotMember(int nid) {
        if (nid < 0) {
            nid = nid + offset;
        }

        this.clear(nid);
    }

    @Override
    public void andNot(NativeIdSetBI other) {
        if (other instanceof ConcurrentBitSet) {
            andNot((ConcurrentBitSet) other);
        } else {
            NativeIdSetItrBI iter = other.getSetBitIterator();

            try {
                while (iter.next()) {
                    if (this.contains(iter.nid())) {
                        this.remove(iter.nid());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcurrentBitSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (AtomicLongArray units : bitSetList) {
            for (int i = 0; i < UNITS_PER_SET; i++) {
                long unit = units.get(i);

                if (unit != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private void privateOr(ConcurrentBitSet other) {
        if (this == other) {
            return;
        }

        logicallyProcessUnits(other, orProcessor);
    }

    @Override
    public int getMaxPossibleId() {
        return maxPossibleId;
    }

    @Override
    public int getMinPossibleId() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void setMaxPossibleId(int maxPossibleId) {
        this.maxPossibleId = maxPossibleId;
        this.usedBits.set(maxPossibleId - offset + 1);
    }

    private void logicallyProcessUnits(ConcurrentBitSet other, ProcessUnits processor) {
        int maxId = Math.max(this.maxPossibleId, other.maxPossibleId);

        ensureCapacity(maxId);

        other.ensureCapacity(maxId);

        int maxUsedBits = Math.max(this.usedBits.get(), other.usedBits.get());

        this.usedBits.set(maxUsedBits);
        other.usedBits.set(maxUsedBits);

        final int len = maxUsedBits;
        final int maxSet = bitSetList.size();


        for (int set = 0; set < maxSet; set++) {
            final AtomicLongArray units = bitSetList.get(set);
            final AtomicLongArray otherUnits = other.bitSetList.get(set);
            int maxUnit = UNITS_PER_SET;

            if ((set + 1) * BITS_PER_SET > len) {
                maxUnit = ((len - (set * BITS_PER_SET)) / BITS_PER_UNIT) + 1;
            }

            for (int unit = 0; unit < maxUnit; unit++) {
                long old = units.get(unit);
                long upd = processor.process(old, otherUnits.get(unit));

                if (old != upd) {
                    while (!units.compareAndSet(unit, old, upd)) {
                        old = units.get(unit);
                        upd = processor.process(old, otherUnits.get(unit));
                    }
                }
            }
        }
    }

    private interface ProcessUnits {

        long process(long long1, long long2);
    }

    private class AllBitsIterator implements NativeIdSetItrBI {

        int currentBit = Integer.MIN_VALUE;

        @Override
        public int nid() {
            return currentBit;
        }

        @Override
        public boolean next() throws IOException {
            currentBit++;

            if (currentBit < offset + usedBits.get()) {
                return true;
            }

            return false;
        }
    }

    private static class AndNotProcessUnits implements ProcessUnits {

        @Override
        public long process(long long1, long long2) {
            return long1 & ~long2;
        }
    }

    private static class AndProcessUnits implements ProcessUnits {

        @Override
        public long process(long long1, long long2) {
            return long1 & long2;
        }
    }

    private static class OrProcessUnits implements ProcessUnits {

        @Override
        public long process(long long1, long long2) {
            return long1 | long2;
        }
    }

    private class SetBitsIterator implements NativeIdSetItrBI {

        int currentBit = 0;

        @Override
        public int nid() {
            return currentBit;
        }

        @Override
        public boolean next() throws IOException {
            if (currentBit != -1) {
                currentBit = nextSetBit(currentBit);
            }

            return currentBit != -1;
        }
    }

    private static class XorProcessUnits implements ProcessUnits {

        @Override
        public long process(long long1, long long2) {
            return long1 ^ long2;
        }
    }
}
