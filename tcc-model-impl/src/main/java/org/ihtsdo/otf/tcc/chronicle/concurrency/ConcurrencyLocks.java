/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.chronicle.concurrency;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author kec
 */
public abstract class ConcurrencyLocks {
   protected final int   concurrencyLevel;
   private int   sshift           = 0;
   private int   ssize            = 1;
   protected int segmentMask;
   protected int segmentShift;

   //~--- constructors --------------------------------------------------------

   public ConcurrencyLocks() {
       concurrencyLevel = 128;
      setup();
   }

   public ConcurrencyLocks(int concurrencyLevel) {
      this.concurrencyLevel = concurrencyLevel;
      setup();
   }

   //~--- methods -------------------------------------------------------------

   private void setup() {
      while (ssize < concurrencyLevel) {
         ++sshift;
         ssize <<= 1;
      }

      segmentShift = 32 - sshift;
      segmentMask  = ssize - 1;
      
   }

   //~--- get methods ---------------------------------------------------------

   public int getConcurrencyLevel() {
      return concurrencyLevel;
   }
}
