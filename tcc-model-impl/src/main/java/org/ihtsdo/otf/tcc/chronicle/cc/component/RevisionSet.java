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



package org.ihtsdo.otf.tcc.chronicle.cc.component;

//~--- JDK imports ------------------------------------------------------------

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class RevisionSet<R extends Revision<R, C>, C extends ConceptComponent<R, C>>
        extends ConcurrentSkipListSet<R> {
   int primordialStamp;

   //~--- constructors --------------------------------------------------------

   public RevisionSet(int primordialStamp) {
      super(new Comparator<R>() {
         @Override
         public int compare(R r1, R r2) {
            return r1.stamp - r2.stamp;
         }
      });
      this.primordialStamp = primordialStamp;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean add(R e) {
      if (e.stamp == primordialStamp) {
         return false;
      }

      return super.add(e);
   }
}
