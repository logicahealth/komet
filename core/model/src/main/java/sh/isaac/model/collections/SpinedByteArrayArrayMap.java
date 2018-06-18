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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import sh.isaac.model.ModelGet;
import static sh.isaac.model.collections.SpineFileUtil.SPINE_PREFIX;

/**
 *
 * @author kec
 */
public class SpinedByteArrayArrayMap extends SpinedIntObjectMap<byte[][]> {

    private static final Logger LOG = LogManager.getLogger();
    private final Semaphore diskSemaphore = new Semaphore(1);

    File directory;

    public SpinedByteArrayArrayMap() {
    }

    public int sizeOnDisk() {
        if (directory == null) {
            return 0;
        }
        File[] files = directory.listFiles((pathname) -> {
            return pathname.getName().startsWith(SPINE_PREFIX);
        });
        int size = 0;
        for (File spineFile : files) {
            size = (int) (size + spineFile.length());
        }
        return size;
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

    public int lazyRead(File directory) {
        this.directory = directory;
        spineCount.set(SpineFileUtil.readSpineCount(directory));
        return spineCount.get();
    }

    /**
     *
     * @param directory
     * @return the number of spine files read.
     */
    public int read(File directory) {
        this.spineCount.set(SpineFileUtil.readSpineCount(directory));
        this.directory = directory;
        File[] files = directory.listFiles((pathname) -> {
            return pathname.getName().startsWith(SPINE_PREFIX);
        });
        int spineFilesRead = 0;
        for (File spineFile : files) {
            readSpine(spineFile);
            spineFilesRead++;
        }
        return spineFilesRead;
    }

    private void readSpine(int spineIndex) {
        String spineKey = SPINE_PREFIX + spineIndex;
        File spineFile = new File(directory, spineKey);
        readSpine(spineFile);
    }

    private void readSpine(File spineFile) throws NumberFormatException {
        int spineIndex = Integer.parseInt(spineFile.getName().substring(SPINE_PREFIX.length()));
        if (spineFile.exists()) {
            diskSemaphore.acquireUninterruptibly();
            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
                int arraySize = dis.readInt();
                byte[][][] spineArray = new byte[arraySize][][];
                for (int i = 0; i < arraySize; i++) {
                    int valueSize = dis.readInt();
                    if (valueSize != 0) {
                        byte[][] value = new byte[valueSize][];
                        for (int j = 0; j < valueSize; j++) {
                            int valuePartSize = dis.readInt();
                            byte[] valuePart = new byte[valuePartSize];
                            dis.readFully(valuePart);
                            value[j] = valuePart;
                        }
                        spineArray[i] = value;
                    }
                }
                AtomicReferenceArray<byte[][]> spine = new AtomicReferenceArray<>(spineArray);
                this.spines.putIfAbsent(spineIndex, spine);
            } catch (IOException ex) {
                LOG.error(ex);
            } finally {
                diskSemaphore.release();
            }
        } else {
            this.spines.putIfAbsent(spineIndex, newSpine(spineIndex));
        }
    }

    public boolean write(File directory) {
        try {
            AtomicBoolean wroteAny = new AtomicBoolean(false);
            SpineFileUtil.writeSpineCount(directory, spineCount.get());
            spines.forEach((Integer key, AtomicReferenceArray<byte[][]> spine) -> {
                String spineKey = SPINE_PREFIX + key;
                boolean spineChanged = this.changedSpineIndexes.contains(key);
                
                if (spineChanged) {
                    wroteAny.set(true);
                    this.changedSpineIndexes.remove(key);
                    File spineFile = new File(directory, spineKey);
                    diskSemaphore.acquireUninterruptibly();
                    try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
                        dos.writeInt(spine.length());
                        for (int i = 0; i < spine.length(); i++) {
                            byte[][] value = spine.get(i);
                            if (value == null) {
                                dos.writeInt(0);
                            } else {
                                dos.writeInt(value.length);
                                for (byte[] valuePart : value) {
                                    dos.writeInt(valuePart.length);
                                    dos.write(valuePart);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOG.error(ex);
                    } finally {
                        diskSemaphore.release();
                    }
                }
                
            });
            return wroteAny.get();
        } catch (IOException ex) {
            LOG.error(ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte[][] get(int index) {
        if (index < 0) {
            if (ModelGet.sequenceStore() != null) {
               index = ModelGet.sequenceStore().getElementSequenceForNid(index);
            }
            else {
               index = Integer.MAX_VALUE + index;
            }
        }
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
    public void put(int index, byte[][] element) {
        if (index < 0) {
           if (ModelGet.sequenceStore() != null) {
                index = ModelGet.sequenceStore().getElementSequenceForNid(index);
             }
             else {
                index = Integer.MAX_VALUE + index;
             }
        }
        int spineIndex = index / spineSize;
        int indexInSpine = index % spineSize;
        if (spineIndex < this.spineCount.get()) {
            if (!this.spines.containsKey(spineIndex)) {
                readSpine(spineIndex);
            }
        }
        this.changedSpineIndexes.add(spineIndex);

        this.spines.computeIfAbsent(spineIndex, this::newSpine).accumulateAndGet(indexInSpine, element, this::merge);
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
