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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Level;
import java.util.logging.Logger;
import sh.isaac.model.ModelGet;

/**
 *
 * @author kec
 */
public class SpinedByteArrayArrayMap extends SpinedIntObjectMap<byte[][]> {

   ConcurrentHashMap<String, AtomicLong> lastWrite = new ConcurrentHashMap<>();
   ConcurrentHashMap<String, AtomicLong> lastUpdate = new ConcurrentHashMap<>();

   public SpinedByteArrayArrayMap() {
   }

   public void read(File directory) {
      File[] files = directory.listFiles((pathname) -> {
         return pathname.getName().startsWith("spine-");
      });
      for (File spineFile : files) {
         int spine = Integer.parseInt(spineFile.getName().substring("spine-".length()));
         try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(spineFile)))) {
            int arraySize = dis.readInt();
            int offset = arraySize * spine;
            for (int i = 0; i < arraySize; i++) {
               int valueSize = dis.readInt();
               if (valueSize != 0) {
                  byte[][] value = new byte[valueSize][];
                  put(offset + i, value);
                  for (int j = 0; j < valueSize; j++) {
                     int valuePartSize = dis.readInt();
                     byte[] valuePart = new byte[valuePartSize];
                     value[j] = valuePart;
                     for (int k = 0; k < valuePartSize; k++) {
                        valuePart[k] = dis.readByte();
                     }
                  }
               }
            }
         } catch (IOException ex) {
            Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   public void write(File directory) {
      spines.forEach((Integer key, AtomicReferenceArray<byte[][]> spine) -> {
         String spineKey = "spine-" + key;
         AtomicLong lastWriteSequence = lastWrite.computeIfAbsent(spineKey, (t) -> new AtomicLong());
         AtomicLong lastUpdateSequence = lastUpdate.computeIfAbsent(spineKey, (t) -> new AtomicLong());
         if (lastWriteSequence.get() < lastUpdateSequence.get()) {
            lastWriteSequence.set(lastUpdateSequence.get());
            File spineFile = new File(directory, spineKey);
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(spineFile)))) {
               dos.writeInt(spine.length());
               for (int i = 0; i < spine.length(); i++) {
                  byte[][] value = spine.get(i);
                  if (value == null) {
                     dos.writeInt(0);
                  } else {
                     dos.writeInt(value.length);
                     for (int j = 0; j < value.length; j++) {
                        byte[] valuePart = value[j];
                        dos.writeInt(valuePart.length);
                        for (int k = 0; k < valuePart.length; k++) {
                           dos.writeByte(valuePart[k]);
                        }
                     }
                  }
               }
            } catch (FileNotFoundException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
               Logger.getLogger(SpinedByteArrayArrayMap.class.getName()).log(Level.SEVERE, null, ex);
            }
         }

      });
   }

   @Override
   public void put(int index, byte[][] element) {
      if (index < 0) {
         index = ModelGet.identifierService().getElementSequenceForNid(index);
      }
      int spineIndex = index / spineSize;
      int indexInSpine = index % spineSize;
      lastUpdate.computeIfAbsent("spine-" + spineIndex, (t) -> new AtomicLong()).incrementAndGet();
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
