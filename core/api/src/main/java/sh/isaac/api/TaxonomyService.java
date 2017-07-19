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

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface TaxonomyService.
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
   void updateTaxonomy(SememeChronology<LogicGraphVersion> logicGraphChronology);

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
    * Gets the all circular relationship origin sequences.
    *
    * @param tc the taxonomy coordinate used to compute the taxonomic
    * relationships
    * @return concept sequences for concepts that define circular
    * relationships, or {@code IntStream.empty()} if there are no such
    * relationships.
    */
   IntStream getAllCircularRelationshipOriginSequences(ManifoldCoordinate tc);

   /**
    * Gets the all circular relationship type sequences.
    *
    * @param originId a concept nid or concept sequence to examine for circular
    * relationships.
    * @param tc the taxonomy coordinate used to compute the taxonomic
    * relationships
    * @return concept sequences for the types of relationships that are
    * circular, or {@code IntStream.empty()} if there are no such
    * relationships.
    */
   IntStream getAllCircularRelationshipTypeSequences(int originId, ManifoldCoordinate tc);

   /**
    * Gets the all relationship destination sequences.
    *
    * @param originId a concept nid or concept sequence to retrieve
    * relationship destination information from
    * @return conceptSequences of all relationship destination concepts
    * (including is-a relationships)
    */
   IntStream getAllRelationshipDestinationSequences(int originId);

   /**
    * Gets the all relationship destination sequences.
    *
    * @param originId the origin id
    * @param tc the tc
    * @return the all relationship destination sequences
    */
   IntStream getAllRelationshipDestinationSequences(int originId, ManifoldCoordinate tc);

   /**
    * Gets the all relationship destination sequences not of type.
    *
    * @param originId a concept nid or concept sequence to retrieve
    * relationship destination information from
    * @param typeSequenceSet set of relationship types to exclude from the
    * results.
    * @param tc the taxonomy coordinate used to compute the taxonomic
    * relationships
    * @return the all relationship destination sequences not of type
    */
   IntStream getAllRelationshipDestinationSequencesNotOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc);

   /**
    * Gets the all relationship destination sequences of type.
    *
    * @param originId the origin id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship destination sequences of type
    */
   IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet);

   /**
    * Gets the all relationship destination sequences of type.
    *
    * @param originId a concept nid or concept sequence to retrieve
    * relationship destination information from
    * @param typeSequenceSet set of relationship types to include in the
    * results.
    * @param tc the taxonomy coordinate used to compute the taxonomic
    * relationships
    * @return conceptSequences of all relationship destination concepts
    * (including is-a relationships)
    */
   IntStream getAllRelationshipDestinationSequencesOfType(int originId,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc);

   /**
    * Gets the all relationship origin sequences.
    *
    * @param destinationId the destination id
    * @return the all relationship origin sequences
    */
   IntStream getAllRelationshipOriginSequences(int destinationId);

   /**
    * Gets the all relationship origin sequences.
    *
    * @param destinationId the destination id
    * @param tc the tc
    * @return the all relationship origin sequences
    */
   IntStream getAllRelationshipOriginSequences(int destinationId, ManifoldCoordinate tc);

   /**
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @return the all relationship origin sequences of type
    */
   IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet);

   /**
    * Gets the all relationship origin sequences of type.
    *
    * @param destinationId the destination id
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the all relationship origin sequences of type
    */
   IntStream getAllRelationshipOriginSequencesOfType(int destinationId,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc);

   /**
    * Gets the all types for relationship.
    *
    * @param originId nid or sequence of the origin concept
    * @param destinationId nid or sequence of the destination concept
    * @param tc the taxonomy coordinate used to compute the taxonomic
    * relationships
    * @return concept sequences of all relationship types between the origin
    * and destination that meet the taxonomy coordinate criterion.
    */
   IntStream getAllTypesForRelationship(int originId, int destinationId, ManifoldCoordinate tc);

   /**
    * Gets the ancestor of sequence set.
    *
    * @param childId a concept sequence or nid for the child concept
    * @param tc coordinate used to compute the taxonomy
    * @return the ancestor concept sequences for the childId concept.
    */
   ConceptSequenceSet getAncestorOfSequenceSet(int childId, ManifoldCoordinate tc);

   /**
    * Checks if child of.
    *
    * @param childId a concept sequence or nid for the child concept
    * @param parentId a concept sequence or nid for the parent concept
    * @param tc coordinate used to compute the taxonomy
    * @return true if the childId concept is a direct descendant of the
    * parentId concept according to the constraints of the
    * {@code ManifoldCoordinate}
    */
   boolean isChildOf(int childId, int parentId, ManifoldCoordinate tc);

   /**
    * Gets the child of sequence set.
    *
    * @param parentId the parent id
    * @param tc the tc
    * @return the child of sequence set
    */
   ConceptSequenceSet getChildOfSequenceSet(int parentId, ManifoldCoordinate tc);

   /**
    * Checks if kind of.
    *
    * @param childId a concept sequence or nid for the child concept
    * @param ancestorId a concept sequence or nid for the ancestor concept
    * @param tc coordinate used to compute the taxonomy
    * @return true if the childId concept is a kind of the ancestorId concept
    * according to the constraints of the {@code ManifoldCoordinate}
    */
   boolean isKindOf(int childId, int ancestorId, ManifoldCoordinate tc);

   /**
    * Gets the kind of sequence set.
    *
    * @param rootId the root id
    * @param tc the tc
    * @return the kind of sequence set
    */
   ConceptSequenceSet getKindOfSequenceSet(int rootId, ManifoldCoordinate tc);

   /**
    * Gets the roots.
    *
    * @param sc the sc
    * @return the roots
    */
   IntStream getRoots(ManifoldCoordinate sc);

   /**
    * Gets the snapshot.
    *
    * @param tc the tc
    * @return the snapshot
    */
   TaxonomySnapshotService getSnapshot(ManifoldCoordinate tc);

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentId the parent id
    * @return the taxonomy child sequences
    */
   IntStream getTaxonomyChildSequences(int parentId);

   /**
    * Gets the taxonomy child sequences.
    *
    * @param parentId the parent id
    * @param tc the tc
    * @return the taxonomy child sequences
    */
   IntStream getTaxonomyChildSequences(int parentId, ManifoldCoordinate tc);

   /**
    * Gets the taxonomy parent sequences.
    *
    * @param childId the child id
    * @return the taxonomy parent sequences
    */
   IntStream getTaxonomyParentSequences(int childId);

   /**
    * Gets the taxonomy parent sequences.
    *
    * @param childId the child id
    * @param tc the tc
    * @return the taxonomy parent sequences
    */
   IntStream getTaxonomyParentSequences(int childId, ManifoldCoordinate tc);

   /**
    * Gets the taxonomy tree.
    *
    * @param tc the tc
    * @return the taxonomy tree
    */
   Tree getTaxonomyTree(ManifoldCoordinate tc);
}

