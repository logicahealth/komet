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



package sh.isaac.api.component.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampPosition;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface SememeServiceTyped.
 *
 * @author kec
 * @param <SV> the generic type
 */
public interface SememeServiceTyped<SV extends SememeVersion> {
   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    */
   void writeSememe(SememeChronology<?> sememeChronicle);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the parallel sememe stream.
    *
    * @return the parallel sememe stream
    */
   Stream<SememeChronology<SV>> getParallelSememeStream();

   /**
    * Gets the sememe.
    *
    * @param sememeSequence the sememe sequence
    * @return the sememe
    */
   SememeChronology<SV> getSememe(int sememeSequence);

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   SememeSequenceSet getSememeSequencesForComponent(int componentNid);

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences for component from assemblage
    */
   SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence);

   /**
    * Gets the sememe sequences for components from assemblage.
    *
    * @param componentNidSet the component nid set
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences for components from assemblage
    */
   SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet, int assemblageSequence);

   /**
    * Gets the sememe sequences for components from assemblage modified after position.
    *
    * @param componentNidSet the component nid set
    * @param assemblageSequence the assemblage sequence
    * @param position the position
    * @return the sememe sequences for components from assemblage modified after position
    */
   SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageSequence,
         StampPosition position);

   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe sequences from assemblage
    */
   SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageSequence);

   /**
    * Gets the sememe sequences from assemblage modified after position.
    *
    * @param assemblageSequence the assemblage sequence
    * @param position the position
    * @return the sememe sequences from assemblage modified after position
    */
   SememeSequenceSet getSememeSequencesFromAssemblageModifiedAfterPosition(int assemblageSequence,
         StampPosition position);

   /**
    * Gets the sememe stream.
    *
    * @return the sememe stream
    */
   Stream<SememeChronology<SV>> getSememeStream();

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   Stream<SememeChronology<SV>> getSememesForComponent(int componentNid);

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageSequence the assemblage sequence
    * @return the sememes for component from assemblage
    */
   Stream<SememeChronology<SV>> getSememesForComponentFromAssemblage(int componentNid, int assemblageSequence);

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememes from assemblage
    */
   Stream<SememeChronology<SV>> getSememesFromAssemblage(int assemblageSequence);
}

