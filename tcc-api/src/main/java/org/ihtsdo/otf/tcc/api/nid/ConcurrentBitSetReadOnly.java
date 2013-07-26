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

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 *
 * @author kec
 */
public class ConcurrentBitSetReadOnly extends ConcurrentBitSet {

    public ConcurrentBitSetReadOnly(BitSet bitSet) {
        super(bitSet);
    }

    public ConcurrentBitSetReadOnly(NativeIdSetBI nativeIdSet) {
        super(nativeIdSet);
    }

    public ConcurrentBitSetReadOnly(String bitString) {
        super(bitString);
    }

    @Override
    public AtomicLongArray getUnits() {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void set(int bit, boolean value) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void clear(int bit) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void clearAll() {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void and(ConcurrentBitSet with) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void or(ConcurrentBitSet with) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void xor(ConcurrentBitSet with) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void andNot(ConcurrentBitSet with) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void setMember(int nid) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void and(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void or(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void xor(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void add(int nid) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void addAll(int[] nids) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void remove(int nid) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void removeAll(int[] nids) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void union(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void setNotMember(int nid) {
        throw new UnsupportedOperationException("Read-only set");
    }

    @Override
    public void andNot(NativeIdSetBI other) {
        throw new UnsupportedOperationException("Read-only set");
    }
    
}
