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

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author kec
 */
public class ConcurrentReentrantReadWriteLocks extends ConcurrencyLocks {
   ReentrantReadWriteLock[] locks;

   //~--- constructors --------------------------------------------------------

   public ConcurrentReentrantReadWriteLocks() {
      super();
      setupLocks();
   }

   public ConcurrentReentrantReadWriteLocks(int concurrencyLevel) {
      super(concurrencyLevel);
      setupLocks();
   }

   //~--- methods -------------------------------------------------------------

   public void readLock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].readLock().lock();
   }

   private void setupLocks() {
      locks = new ReentrantReadWriteLock[concurrencyLevel];

      for (int i = 0; i < concurrencyLevel; i++) {
         locks[i] = new ReentrantReadWriteLock();
      }
   }

   public void unlockRead(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].readLock().unlock();
   }

   public void unlockWrite(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].writeLock().unlock();
   }

   public void unlockWriteAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].writeLock().unlock();
      }
   }

   public void writeLock(int dbKey) {
      int word = (dbKey >>> segmentShift) & segmentMask;

      locks[word].writeLock().lock();
   }

   public void writeLockAll() {
      for (int i = 0; i < locks.length; i++) {
         locks[i].writeLock().lock();
      }
   }
}
