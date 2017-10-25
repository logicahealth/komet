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
package sh.isaac.provider.bdb.chronology;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.util.Spliterator;
import java.util.function.Consumer;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

/**
 *
 * @author kec
 */
public class CursorChronologySpliterator implements Spliterator<ByteArrayDataBuffer>, AutoCloseable {

   final Cursor cursor;
   final Database database;
   final DatabaseEntry key = new DatabaseEntry();
   final DatabaseEntry value = new DatabaseEntry();
   private boolean closed = false;
   int currentId;
   int maxId;

   public CursorChronologySpliterator(Database database, int maxId) {
      this.cursor = database.openCursor(null, CursorConfig.DEFAULT);
      this.database = database;
      this.currentId = 0;
      this.maxId = maxId;
   }

   @Override
   public boolean tryAdvance(Consumer<? super ByteArrayDataBuffer> action) {
      if (!closed) {
         OperationStatus status = cursor.getNext(key, value, LockMode.DEFAULT);
         if (status == OperationStatus.SUCCESS) {
            int currentKey = IntegerBinding.entryToInt(key);
            if (currentKey < currentId) {
               IntegerBinding.intToEntry(currentId, key);
               status = cursor.getSearchKeyRange(key, value, LockMode.DEFAULT);
               if (status != OperationStatus.SUCCESS) {
                  close();
                  return false;
               }
            }
            currentId = currentKey;
            if (currentId <= maxId) {
               action.accept(BdbProvider.collectByteRecords(key, value, cursor));
               return true;
            }
         }
         close();
         return false;
      } else {
         throw new RuntimeException("Trying advance after close... ");
      }
   }

   @Override
   public Spliterator<ByteArrayDataBuffer> trySplit() {
      CursorChronologySpliterator splitStream = new CursorChronologySpliterator(database, maxId);
      int split = maxId - currentId;
      int half = split / 2;
      this.maxId = currentId + half;
      splitStream.currentId = currentId + half + 1;
      return splitStream;
   }

   @Override
   public long estimateSize() {
      return currentId - maxId;
   }

   @Override
   public int characteristics() {
      return Spliterator.DISTINCT + DISTINCT + NONNULL + ORDERED;
   }

   @Override
   public void close() {
      cursor.close();
      this.closed = true;
   }

}
