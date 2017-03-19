/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.integration.tests;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.externalizable.OchreExternalizable;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/25/16.
 */
public class OchreExternalizableStatsTestFilter
         implements Predicate<OchreExternalizable> {
   AtomicInteger concepts      = new AtomicInteger(0);
   AtomicInteger sememes       = new AtomicInteger(0);
   AtomicInteger stampAliases  = new AtomicInteger(0);
   AtomicInteger stampComments = new AtomicInteger(0);

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final OchreExternalizableStatsTestFilter that = (OchreExternalizableStatsTestFilter) o;

      if (this.concepts.get() != that.concepts.get()) {
         return false;
      }

      if (this.sememes.get() != that.sememes.get()) {
         return false;
      }

      if (this.stampAliases.get() != that.stampAliases.get()) {
         return false;
      }

      return this.stampComments.get() == that.stampComments.get();
   }

   @Override
   public int hashCode() {
      int result = this.concepts.hashCode();

      result = 31 * result + this.sememes.hashCode();
      result = 31 * result + this.stampAliases.hashCode();
      result = 31 * result + this.stampComments.hashCode();
      return result;
   }

   @Override
   public boolean test(OchreExternalizable ochreExternalizable) {
      switch (ochreExternalizable.getOchreObjectType()) {
      case CONCEPT:
         this.concepts.incrementAndGet();
         break;

      case SEMEME:
         this.sememes.incrementAndGet();
         break;

      case STAMP_ALIAS:
         this.stampAliases.incrementAndGet();
         break;

      case STAMP_COMMENT:
         this.stampComments.incrementAndGet();
         break;

      default:
         throw new UnsupportedOperationException("Can't handle: " + ochreExternalizable);
      }

      return true;
   }

   @Override
   public String toString() {
      return "OchreExternalizableStatsTestFilter{" + "concepts=" + this.concepts + ", sememes=" + this.sememes +
             ", stampAliases=" + this.stampAliases + ", stampComments=" + this.stampComments + '}';
   }
}

