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



package sh.isaac.model.concept;

import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.model.VersionImpl;

/**
 * The Class ConceptVersionImpl.
 *
 * @author kec
 */
public class ConceptVersionImpl
        extends VersionImpl
         implements ConceptVersion {
   /**
    * Instantiates a new concept version impl.
    *
    * @param chronicle the chronicle
    * @param stampSequence the stamp sequence
    */
   public ConceptVersionImpl(ConceptChronologyImpl chronicle, int stampSequence) {
      super(chronicle, stampSequence);
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public ConceptChronology getChronology() {
      return (ConceptChronology) this.chronicle;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      ConceptChronologyImpl chronologyImpl = (ConceptChronologyImpl) this.chronicle;
      final ConceptVersionImpl newVersion = new ConceptVersionImpl(chronologyImpl, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;
   }

   @Override
   public final boolean dataEquals(VersionImpl other) {
      // no additional fields to check. 
      return true;
   }

   @Override
   protected int editDistance2(VersionImpl other, int editDistance) {
      // no additional fields to add.
      return editDistance;
   }
}

