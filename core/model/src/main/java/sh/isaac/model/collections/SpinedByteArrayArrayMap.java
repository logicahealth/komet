/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.store.ByteArrayArrayStore;

/**
 *
 * @author kec
 */
public class SpinedByteArrayArrayMap extends SpinedIntObjectMap<byte[][]> {

    private static final Logger LOG = LogManager.getLogger();
    private final ByteArrayArrayStore byteArrayArrayStore;

    public SpinedByteArrayArrayMap(ByteArrayArrayStore byteArrayArrayStore) {
        this.byteArrayArrayStore = byteArrayArrayStore;
        spineCount.set(byteArrayArrayStore.getSpineCount());
    }

    public int sizeOnDisk() {
        return byteArrayArrayStore.sizeOnDisk();
    }

    public int memoryInUse() {
        int sizeInBytes = 0;
        sizeInBytes = sizeInBytes + ((spineSize * 8) * spines.size()); // 8 bytes = pointer to an object
        for (AtomicReferenceArray<byte[][]> spine : spines.values()) {
            for (int i = 0; i < spine.length(); i++) {
                byte[][] value = spine.get(i);
                if (value != null) {
                    for (byte[] byteArray : value) {
                        sizeInBytes = sizeInBytes + byteArray.length + 4; // 4 bytes = integer length of the array of array length. 
                    }
                }
            }
        }
        return sizeInBytes;
    }

    @Override
    protected AtomicReferenceArray<byte[][]> newSpine(Integer spineKey) {
        if (spineKey < this.spineCount.get()) {
            return readSpine(spineKey);
        }
        return makeNewSpine(spineKey);
    }


    private AtomicReferenceArray<byte[][]> readSpine(int spineIndex) {
        Optional<AtomicReferenceArray<byte[][]>> optionalSpine = this.byteArrayArrayStore.get(spineIndex);
        if (optionalSpine.isPresent()) {
            return this.spines.putIfAbsent(spineIndex, optionalSpine.get());
        }
        return this.spines.putIfAbsent(spineIndex, makeNewSpine(spineIndex));
        
    }

    public boolean write() {
        lock();
        try {
            AtomicBoolean wroteAny = new AtomicBoolean(false);
            this.byteArrayArrayStore.writeSpineCount(spineCount.get());
            spines.forEach((Integer spineIndex, AtomicReferenceArray<byte[][]> spine) -> {
                boolean spineChanged = this.changedSpineIndexes.contains(spineIndex);
                
                if (spineChanged) {
                    wroteAny.set(true);
                    this.changedSpineIndexes.remove(spineIndex);
                    this.byteArrayArrayStore.put(spineIndex, spine);
                }
                
            });
            return wroteAny.get();
        } finally {
            release();
        }
    }

    @Override
    public byte[][] get(int index) {
        index = indexToSpineIndex(index);
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        if (spineIndex < this.spineCount.get()) {
            if (!this.spines.containsKey(spineIndex)) {
                readSpine(spineIndex);
            }
        }
        return this.spines.computeIfAbsent(spineIndex, this::newSpine).get(indexInSpine);
    }

    @Override
    public boolean put(int index, byte[][] element) {
        index = indexToSpineIndex(index);
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        if (spineIndex < this.spineCount.get()) {
            if (!this.spines.containsKey(spineIndex)) {
                readSpine(spineIndex);
            }
        }
        this.changedSpineIndexes.add(spineIndex);
        
        
        AtomicReferenceArray<byte[][]> spine = this.spines.computeIfAbsent(spineIndex, this::newSpine);
        boolean returnValue = spine.get(indexInSpine) != null;
        spine.accumulateAndGet(indexInSpine, element, this::merge);
        return returnValue;
    }

    private int indexToSpineIndex(int index) {
        if (index < 0) {
           if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
             }
             else {
                index = Integer.MAX_VALUE + index;
             }
        }
        return index;
    }

    private static int compare(byte[] one, byte[] another) {
        boolean oneStartsWithZero = false;
        boolean anotherStartsWithZero = false;

        if (one[0] == 0 && one[1] == 0 && one[2] == 0 && one[3] == 0) {
            oneStartsWithZero = true;
        }
        if (another[0] == 0 && another[1] == 0 && another[2] == 0 && another[3] == 0) {
            anotherStartsWithZero = true;
        }
        if (oneStartsWithZero && anotherStartsWithZero) {
            return 0;
        }
        if (oneStartsWithZero) {
            return -1;
        }
        if (anotherStartsWithZero) {
            return 1;
        }
        for (int i = 0; i < one.length && i < another.length; i++) {
            int compare = Byte.compare(one[i], another[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return one.length - another.length;
    }

    private byte[][] merge(byte[][] currentValue, byte[][] updateValue) {
        if (currentValue == null || currentValue.length == 0) {
            return updateValue;
        }
        if (updateValue == null) {
            throw new IllegalStateException("Update value is null");
        }
        Arrays.sort(updateValue, SpinedByteArrayArrayMap::compare);
        ArrayList<byte[]> mergedValues = new ArrayList<>(currentValue.length + updateValue.length);
        int updateIndex = 0;
        int currentIndex = 0;

        while (updateIndex < updateValue.length && currentIndex < currentValue.length) {
            int compare = compare(currentValue[currentIndex], updateValue[updateIndex]);
            if (compare == 0) {
                mergedValues.add(currentValue[currentIndex]);
                currentIndex++;
                updateIndex++;
                if (currentIndex == currentValue.length) {
                    while (updateIndex < updateValue.length) {
                        mergedValues.add(updateValue[updateIndex++]);
                    }
                }
                if (updateIndex == updateValue.length) {
                    while (currentIndex < currentValue.length) {
                        mergedValues.add(currentValue[currentIndex++]);
                    }
                }
            } else if (compare < 0) {
                mergedValues.add(currentValue[currentIndex]);
                currentIndex++;
                if (currentIndex == currentValue.length) {
                    while (updateIndex < updateValue.length) {
                        mergedValues.add(updateValue[updateIndex++]);
                    }
                }
            } else {
                mergedValues.add(updateValue[updateIndex]);
                updateIndex++;
                if (updateIndex == updateValue.length) {
                    while (currentIndex < currentValue.length) {
                        mergedValues.add(currentValue[currentIndex++]);
                    }
                }
            }
        }
        return mergedValues.toArray(new byte[mergedValues.size()][]);
    }

    public void put(int elementSequence, List<byte[]> dataList) {
        put(elementSequence, dataList.toArray(new byte[dataList.size()][]));
    }

}
