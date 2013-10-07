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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author dylangrald
 */
public class IntSet implements NativeIdSetBI {

    OpenIntHashSet hashSet;
    int maxPossibleId = Integer.MAX_VALUE;

    public IntSet() {
        this.hashSet = new OpenIntHashSet();
    }

    public IntSet(int[] nids) {
        this.hashSet = new OpenIntHashSet();
        for (int i : nids) {
            this.hashSet.add(i);
        }
    }

    public IntSet(ConcurrentBitSet other) throws IOException {
        this.hashSet = new OpenIntHashSet();
        NativeIdSetItrBI iter = other.getSetBitIterator();
        while (iter.next()) {
            this.hashSet.add(iter.nid());
        }
    }

    public IntSet(NativeIdSetBI other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return this.hashSet.size();
    }

    @Override
    public boolean isMember(int nid) {
        return this.hashSet.contains(nid);
    }

    @Override
    public void setMember(int nid) {
        this.hashSet.add(nid);
    }

    @Override
    public void and(NativeIdSetBI other) {
        if (other.isEmpty()) {
            this.clear();
        } else {
            NativeIdSetItrBI iter = this.getSetBitIterator();
            try {
                while (iter.next()) {
                    if (!other.isMember(iter.nid())) {
                        this.remove(iter.nid());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(IntSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void or(NativeIdSetBI other) {
        this.union(other);
    }

    @Override
    public void xor(NativeIdSetBI other) {
        NativeIdSetItrBI iter = other.getSetBitIterator();
        try {
            while (iter.next()) {
                if (!this.isMember(iter.nid())) {
                    this.add(iter.nid());
                } else {
                    this.remove(iter.nid());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IntSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean contains(int nid) {
        return this.hashSet.contains(nid);
    }

    @Override
    public int[] getSetValues() {
        IntArrayList setValues = this.hashSet.keys();
        int[] ret = new int[setValues.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = setValues.get(i);
        }
        Arrays.sort(ret);
        return ret;

    }

    @Override
    public void add(int nid) {
        this.hashSet.add(nid);
    }

    @Override
    public void addAll(int[] nids) {
        for (int i : nids) {
            this.hashSet.add(i);
        }
    }

    @Override
    public void remove(int nid) {
        this.hashSet.remove(nid);
    }

    @Override
    public void removeAll(int[] nids) {
        for (int i : nids) {
            this.hashSet.remove(i);
        }
    }

    @Override
    public void clear() {
        this.hashSet.clear();
    }

    @Override
    public int getMax() {
        if (this.hashSet.size() == 0) {
            return Integer.MAX_VALUE;
        } else {
            int[] setValues = this.getSetValues();
            return setValues[this.size() - 1];
        }
    }

    @Override
    public int getMin() {
        if (this.hashSet.size() == 0) {
            return Integer.MIN_VALUE;
        } else {
            int[] setValues = this.getSetValues();
            return setValues[0];
        }
    }

    @Override
    public boolean contiguous() {
        int[] setValues = this.getSetValues();
        if (setValues.length < 2) {
            return true;
        }

        NativeIdSetItrBI iter = this.getSetBitIterator();
        int temp = Integer.MIN_VALUE;
        try {
            while (iter.next()) {
                if (temp - iter.nid() > 1) {
                    return false;
                }
                temp = iter.nid();
            }
        } catch (IOException ex) {
            Logger.getLogger(IntSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;

    }

    @Override
    public void union(NativeIdSetBI other) {
        NativeIdSetItrBI iter = other.getSetBitIterator();
        try {
            while (iter.next()) {
                if (!this.isMember(iter.nid())) {
                    this.add(iter.nid());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IntSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setNotMember(int nid) {
        this.hashSet.remove(nid);
    }

    @Override
    public void andNot(NativeIdSetBI other) {
        NativeIdSetItrBI iter = this.getSetBitIterator();
        try {
            while (iter.next()) {
                if (other.isMember(iter.nid())) {
                    this.remove(iter.nid());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IntSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return "IntSet{" + "hashSet=" + hashSet + '}';
    }

    @Override
    public NativeIdSetItrBI getSetBitIterator() {
        return new Iterator();
    }

    @Override
    public NativeIdSetItrBI getAllBitIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.hashSet.isEmpty();
    }

    @Override
    public void setAll(int max) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxPossibleId() {
        return this.maxPossibleId;
    }

    @Override
    public int getMinPossibleId() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void setMaxPossibleId(int nid) {
        this.maxPossibleId = nid;
    }

    private class Iterator implements NativeIdSetItrBI {

        IntArrayList items;
        int index = -1;
        int size;

        public Iterator() {
            items = hashSet.keys();
            size = items.size();
        }

        @Override
        public int nid() {
            return items.get(index);
        }

        @Override
        public boolean next() throws IOException {
            if (index < size - 1) {
                index++;
                return true;
            }
            return false;
        }
    }
}
