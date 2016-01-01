/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface TaxonomyService {

    TaxonomySnapshotService getSnapshot(TaxonomyCoordinate tc);

    Tree getTaxonomyTree(TaxonomyCoordinate tc);

    /**
     *
     * @param childId a concept sequence or nid for the child concept
     * @param parentId a concept sequence or nid for the parent concept
     * @param tc coordinate used to compute the taxonomy
     * @return true if the childId concept is a direct descendant of the
     * parentId concept according to the constraints of the
     * {@code TaxonomyCoordinate}
     */
    boolean isChildOf(int childId, int parentId, TaxonomyCoordinate tc);

    /**
     *
     * @param childId a concept sequence or nid for the child concept
     * @param ancestorId a concept sequence or nid for the ancestor concept
     * @param tc coordinate used to compute the taxonomy
     * @return true if the childId concept is a kind of the ancestorId concept
     * according to the constraints of the {@code TaxonomyCoordinate}
     */
    boolean isKindOf(int childId, int ancestorId, TaxonomyCoordinate tc);

    /**
     * Method to determine if a concept was ever a kind of another, without
     * knowing a TaxonomyCoordinate.
     *
     * @param childId a concept sequence or nid for the child concept
     * @param parentId a concept sequence or nid for the parent concept
     * @return true if child was ever a kind of the parent.
     */
    boolean wasEverKindOf(int childId, int parentId);

    ConceptSequenceSet getKindOfSequenceSet(int rootId, TaxonomyCoordinate tc);

    ConceptSequenceSet getChildOfSequenceSet(int parentId, TaxonomyCoordinate tc);

    /**
     *
     * @param childId a concept sequence or nid for the child concept
     * @param tc coordinate used to compute the taxonomy
     * @return the ancestor concept sequences for the childId concept.
     */
    ConceptSequenceSet getAncestorOfSequenceSet(int childId, TaxonomyCoordinate tc);

    /**
     *
     * @param tc the taxonomy coordinate used to compute the taxonomic
     * relationships
     * @return concept sequences for concepts that define circular
     * relationships, or {@code IntStream.empty()} if there are no such
     * relationships.
     */
    IntStream getAllCircularRelationshipOriginSequences(TaxonomyCoordinate tc);

    /**
     *
     * @param originId a concept nid or concept sequence to examine for circular
     * relationships.
     * @param tc the taxonomy coordinate used to compute the taxonomic
     * relationships
     * @return concept sequences for the types of relationships that are
     * circular, or {@code IntStream.empty()} if there are no such
     * relationships.
     */
    IntStream getAllCircularRelationshipTypeSequences(int originId, TaxonomyCoordinate tc);

    IntStream getAllRelationshipOriginSequences(int destinationId, TaxonomyCoordinate tc);

    IntStream getAllRelationshipOriginSequences(int destinationId);

    IntStream getAllRelationshipDestinationSequences(int originId, TaxonomyCoordinate tc);

    /**
     *
     * @param originId a concept nid or concept sequence to retrieve
     * relationship destination information from
     * @return conceptSequences of all relationship destination concepts
     * (including is-a relationships)
     */
    IntStream getAllRelationshipDestinationSequences(int originId);

    /**
     *
     * @param originId a concept nid or concept sequence to retrieve
     * relationship destination information from
     * @param tc the taxonomy coordinate used to compute the taxonomic
     * relationships
     * @param typeSequenceSet set of relationship types to include in the
     * results.
     * @return conceptSequences of all relationship destination concepts
     * (including is-a relationships)
     */
    IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);

    /**
     *
     * @param originId a concept nid or concept sequence to retrieve
     * relationship destination information from
     * @param typeSequenceSet set of relationship types to exclude from the
     * results.
     * @param tc the taxonomy coordinate used to compute the taxonomic
     * relationships
     * @return
     */
    IntStream getAllRelationshipDestinationSequencesNotOfType(int originId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);

    IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet);

    IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);

    IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet);

    /**
     *
     * @param originId nid or sequence of the origin concept
     * @param destinationId nid or sequence of the destination concept
     * @param tc the taxonomy coordinate used to compute the taxonomic
     * relationships
     * @return concept sequences of all relationship types between the origin
     * and destination that meet the taxonomy coordinate criterion.
     */
    IntStream getAllTypesForRelationship(int originId, int destinationId, TaxonomyCoordinate tc);

    IntStream getTaxonomyChildSequences(int parentId, TaxonomyCoordinate tc);

    IntStream getTaxonomyChildSequences(int parentId);

    IntStream getTaxonomyParentSequences(int childId, TaxonomyCoordinate tc);

    IntStream getTaxonomyParentSequences(int childId);

    IntStream getRoots(TaxonomyCoordinate sc);

    /**
     * Update the taxonomy by extracting relationships from the logical
     * definitions in the {@code logicGraphChronology}. This method will be
     * called by a commit listener, so developers do not have to update the
     * taxonomy themselves, unless developing an alternative taxonomy service
     * implementation.
     *
     * @param logicGraphChronology Chronology of the logical definitions
     */
    void updateTaxonomy(SememeChronology<LogicGraphSememe<?>> logicGraphChronology);
}
