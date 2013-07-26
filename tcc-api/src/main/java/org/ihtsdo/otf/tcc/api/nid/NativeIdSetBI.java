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


    NativeIdSetItrBI getIterator();
    
    int size();

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
    
    int getMax();
    
    int getMin();
    
    boolean contiguous();
    
    @Override
    String toString();
    
    void union(NativeIdSetBI other);
    
    void setNotMember(int nid);
    
    void andNot(NativeIdSetBI other);
    
    boolean isEmpty();
    
}
