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



package org.ihtsdo.otf.tcc.chronicle.cc.change;

/**
 *
 * @author kec
 */
public class BdbCommitSequence {
   private static short commitSequence = Short.MIN_VALUE;

   //~--- methods -------------------------------------------------------------

   public static synchronized short nextSequence() {
      if (commitSequence >= Short.MAX_VALUE) {
         commitSequence = Short.MIN_VALUE;
      }

      return commitSequence++;
   }

   //~--- get methods ---------------------------------------------------------

   public static short getCommitSequence() {
      return commitSequence;
   }
}
