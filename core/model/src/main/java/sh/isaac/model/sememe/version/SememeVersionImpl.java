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



package sh.isaac.model.sememe.version;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.MutableSememeVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.ObjectVersionImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 *  @param <V>
 */
public class SememeVersionImpl<V extends SememeVersionImpl<V>>
        extends ObjectVersionImpl<SememeChronologyImpl<V>, V>
         implements MutableSememeVersion<V> {
   public SememeVersionImpl(SememeChronologyImpl<V> container, int stampSequence, short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      return getSememeType().toString() + super.toString();
   }

   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAssemblageSequence() {
      return chronicle.getAssemblageSequence();
   }

   @Override
   public SememeChronology<V> getChronology() {
      return chronicle;
   }

   @Override
   public int getReferencedComponentNid() {
      return chronicle.getReferencedComponentNid();
   }

   @Override
   public int getSememeSequence() {
      return chronicle.getSememeSequence();
   }

   public SememeType getSememeType() {
      return SememeType.MEMBER;
   }

   ;
}

