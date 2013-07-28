/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.api.concurrency;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author kec
 */
public class ConcurrentReentrantLocks extends ConcurrencyLocks {
   ReentrantLock[] locks;

   //~--- constructors --------------------------------------------------------

   public ConcurrentReentrantLocks() {
      super();
      setupLocks();
   }

   public ConcurrentReentrantLocks(int concurrencyLevel) {
      super(concurrencyLevel);
      setupLocks();
   }

   //~--- methods -------------------------------------------------------------

   public void lock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].lock();
   }

   public void lockAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].lock();
      }
   }

   private void setupLocks() {
      locks = new ReentrantLock[concurrencyLevel];

      for (int i = 0; i < concurrencyLevel; i++) {
         locks[i] = new ReentrantLock();
      }
   }

   public void unlock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].unlock();
   }

   public void unlockAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].unlock();
      }
   }
}
