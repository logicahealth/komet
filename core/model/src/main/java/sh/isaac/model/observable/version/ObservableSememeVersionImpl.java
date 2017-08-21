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



package sh.isaac.model.observable.version;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableSememeVersionImpl.
 *
 * @author kec
 */
public class ObservableSememeVersionImpl
        extends ObservableVersionImpl
         implements ObservableSememeVersion {
   /**
    * Instantiates a new observable sememe version impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableSememeVersionImpl(SememeVersion stampedVersion, ObservableSememeChronology chronology) {
      super(stampedVersion, 
              chronology);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public int getAssemblageSequence() {
      return ((SememeVersion) this.stampedVersion).getAssemblageSequence();
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public ObservableSememeChronology getChronology() {
      return (ObservableSememeChronology) this.chronology;
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public int getReferencedComponentNid() {
      return ((SememeVersion) this.stampedVersion).getReferencedComponentNid();
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public int getSememeSequence() {
      return ((SememeVersion) this.stampedVersion).getSememeSequence();
   }
}

