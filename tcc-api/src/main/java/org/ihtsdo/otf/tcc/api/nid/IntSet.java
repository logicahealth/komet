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
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenIntHashSet;

/**
 *
 * @author dylangrald
 */
public class IntSet implements NativeIdSetBI {

    OpenIntHashSet hashSet;

    public IntSet() {
        this.hashSet = new OpenIntHashSet();
    }

    public IntSet(int[] nids) {
        this.hashSet = new OpenIntHashSet();
        for (int i : nids) {
            this.hashSet.add(i);
        }
    }
    
    public IntSet(ConcurrentBitSet other){
        this.hashSet = new OpenIntHashSet();
        int[] otherSet = other.getSetValues();
        for(int i: otherSet){
            this.hashSet.add(i);
        }
    }

    public IntSet(NativeIdSetBI other){
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
            for (int i : this.getSetValues()) {
                if (!other.isMember(i)) {
                    this.remove(i);
                }
            }
        }
    }

    @Override
    public void or(NativeIdSetBI other) {
        this.union(other);
    }

    @Override
    public void xor(NativeIdSetBI other) {
        int[] otherValues = other.getSetValues();
        for (int i : otherValues) {
            if (!this.isMember(i)) {
                this.add(i);

            } else {
                this.remove(i);
            }
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
        int prev = setValues[0] - 1;
        for (int i : setValues) {
            if (prev != i - 1) {
                return false;
            }
            prev = i;
        }
        return true;
    }

    @Override
    public void union(NativeIdSetBI other) {
        int[] otherValues = other.getSetValues();
        for (int i : otherValues) {
            if (!this.isMember(i)) {
                this.add(i);
            }
        }
    }

    @Override
    public void setNotMember(int nid) {
        this.hashSet.remove(nid);
    }

    @Override
    public void andNot(NativeIdSetBI other) {
        for (int i : this.getSetValues()) {
            if (other.isMember(i)) {
                this.remove(i);
            }
        }
    }

    @Override
    public String toString() {
        return "IntSet{" + "hashSet=" + hashSet + '}';
    }

    @Override
    public NativeIdSetItrBI getIterator() {
        return new Iterator();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
