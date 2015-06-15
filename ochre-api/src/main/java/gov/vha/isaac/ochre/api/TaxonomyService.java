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

import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
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
    
    boolean isChildOf(int childId, int parentId, TaxonomyCoordinate tc);
    
    boolean isKindOf(int childId, int parentId, TaxonomyCoordinate tc);
    
    /**
     * Method to determine if a concept was ever a kind of another, without
     * knowing a TaxonomyCoordinate. 
     * @param childId
     * @param parentId
     * @return true if child was ever a kind of the parent. 
     */
    boolean wasEverKindOf(int childId, int parentId);
    
    ConceptSequenceSet getKindOfSequenceSet(int rootId, TaxonomyCoordinate tc);
     
    ConceptSequenceSet getChildOfSequenceSet(int parentId, TaxonomyCoordinate tc);
     
    IntStream getAllRelationshipOriginSequencesActive(int destinationId, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipOriginSequencesVisible(int destinationId, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipOriginSequences(int destinationId);

    IntStream getAllRelationshipDestinationSequencesActive(int originId, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipDestinationSequencesVisible(int originId, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipDestinationSequences(int originId);

    IntStream getAllRelationshipDestinationSequencesOfTypeActive(int originId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipDestinationSequencesOfTypeVisible(int originId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipDestinationSequencesOfType(int originId, ConceptSequenceSet typeSequenceSet);

    IntStream getAllRelationshipOriginSequencesOfTypeActive(int destinationId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipOriginSequencesOfTypeVisible(int destinationId, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc);
    
    IntStream getAllRelationshipOriginSequencesOfType(int destinationId, ConceptSequenceSet typeSequenceSet);

    IntStream getTaxonomyChildSequencesActive(int parentId, TaxonomyCoordinate tc);

    IntStream getTaxonomyChildSequencesVisible(int parentId, TaxonomyCoordinate tc);
    
    IntStream getTaxonomyChildSequences(int parentId);
    
    IntStream getTaxonomyParentSequencesActive(int childId, TaxonomyCoordinate tc);
    
    IntStream getTaxonomyParentSequencesVisible(int childId, TaxonomyCoordinate tc);
    
    IntStream getTaxonomyParentSequences(int childId);
    
    IntStream getRoots(TaxonomyCoordinate sc);
}
