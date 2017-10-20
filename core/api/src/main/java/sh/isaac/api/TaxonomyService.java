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



package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.IntStream;
import javafx.concurrent.Task;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface TaxonomyService.
 * 
 * Many of the calls that use a manifold coordinate create a taxonomy tree in the background, 
 * and do not cache the results. So they are deprecated for now in favor of moving all these
 * methods to getSnapshot(). 
 * 
 *
 * @author kec
 */
@Contract
public interface TaxonomyService
        extends DatabaseServices {
   /**
    * Update the taxonomy by extracting relationships from the logical
    * definitions in the {@code logicGraphChronology}. This method will be
    * called by a commit listener, so developers do not have to update the
    * taxonomy themselves, unless developing an alternative taxonomy service
    * implementation.
    *
    * @param logicGraphChronology Chronology of the logical definitions
    */
   void updateTaxonomy(SemanticChronology logicGraphChronology);

   /**
    * Method to determine if a concept was ever a kind of another, without
 knowing a ManifoldCoordinate.
    *
    * @param childId a concept sequence or nid for the child concept
    * @param parentId a concept sequence or nid for the parent concept
    * @return true if child was ever a kind of the parent.
    */
   boolean wasEverKindOf(int childId, int parentId);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship origin sequences of type
    */
   IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet);

   /**
    * Gets the snapshot.
    *
    * @param tc the tc
    * @return the snapshot
    */
   Task<TaxonomySnapshotService> getSnapshot(ManifoldCoordinate tc);

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentId the parent id
    * @return the taxonomy child sequences
    */
   IntStream getTaxonomyChildSequences(int parentId);

   /**
    * Gets the taxonomy parent sequences.
    *
    * @param childId the child id
    * @return the taxonomy parent sequences
    */
   IntStream getTaxonomyParentSequences(int childId);
}

