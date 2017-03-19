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

import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatabaseServices;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.StampPosition;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
@Contract
public interface SememeService
        extends DatabaseServices {
   <V extends SememeVersion> SememeServiceTyped<V> ofType(Class<V> versionType);

   /**
    * Write a sememe to the sememe service. Will not overwrite a sememe if one already exists, rather it will
    * merge the written sememe with the provided sememe.
    *
    *
    * The persistence of the concept is dependent on the persistence
    * of the underlying service.
    * @param sememe to be written.
    */
   void writeSememe(SememeChronology<?> sememeChronicle, SememeConstraints... constraints);

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the sequence identifiers of all assemblage concepts that are actually in use by a sememe
    */
   Stream<Integer> getAssemblageTypes();

   Stream<SememeChronology<? extends DescriptionSememe<?>>> getDescriptionsForComponent(int componentNid);

   /**
    *
    * @param sememeId sequence or nid for a sememe
    * @return the identified {@code SememeChronology}
    */
   Optional<? extends SememeChronology<? extends SememeVersion<?>>> getOptionalSememe(int sememeId);

   Stream<SememeChronology<? extends SememeVersion<?>>> getParallelSememeStream();

   /**
    *
    * @param sememeId sequence or nid for a sememe
    * @return the identified {@code SememeChronology}
    */
   SememeChronology<? extends SememeVersion<?>> getSememe(int sememeId);

   /**
    * Use in circumstances when not all sememes may have been loaded to find out if a sememe is present,
    * without incurring the overhead of reading back the object.
    * @param sememeId Either a nid or sememe sequence
    * @return true if present, false otherwise
    */
   boolean hasSememe(int sememeId);

   Stream<SememeChronology<? extends SememeVersion<?>>> getSememeChronologyStream();

   int getSememeCount();

   IntStream getSememeKeyParallelStream();

   IntStream getSememeKeyStream();

   SememeSequenceSet getSememeSequencesForComponent(int componentNid);

   SememeSequenceSet getSememeSequencesForComponentFromAssemblage(int componentNid, int assemblageConceptSequence);

   /**
    * @param componentNid The component nid that the sememes must reference
    * @param allowedAssemblageSequences The (optional) set of assemblage types to limit the return to.  If empty or null, no assemblage filter is applied.
    */
   SememeSequenceSet getSememeSequencesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences);

   SememeSequenceSet getSememeSequencesForComponentsFromAssemblage(NidSet componentNidSet,
         int assemblageConceptSequence);

   SememeSequenceSet getSememeSequencesForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptSequence,
         StampPosition position);

   SememeSequenceSet getSememeSequencesFromAssemblage(int assemblageConceptSequence);

   Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponent(int componentNid);

   Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence);

   /**
    * @param componentNid The component nid that the sememes must reference
    * @param allowedAssemblageSequences The (optional) set of assemblage types to limit the return to.  If empty or null, no assemblage filter is applied.
    */
   Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblages(int componentNid,
         Set<Integer> allowedAssemblageSequences);

   Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblage(int assemblageConceptSequence);

   <V extends SememeVersion> SememeSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate);
}

