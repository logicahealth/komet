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
    
    ConceptSequenceSet getKindOfSequenceSet(int rootId, TaxonomyCoordinate tc);
     
    int[] getAllRelationshipOriginSequencesActive(int destination, TaxonomyCoordinate tc);
    
    int[] getAllRelationshipOriginSequencesVisible(int destination, TaxonomyCoordinate tc);
    
    int[] getAllRelationshipOriginSequences(int destination);

    int[] getTaxonomyChildSequencesActive(int parentId, TaxonomyCoordinate tc);

    int[] getTaxonomyChildSequencesVisible(int parentId, TaxonomyCoordinate tc);
    
    int[] getTaxonomyChildSequences(int parentId);
    
    int[] getTaxonomyParentSequencesActive(int childId, TaxonomyCoordinate tc);
    
    int[] getTaxonomyParentSequencesVisible(int childId, TaxonomyCoordinate tc);
    
    int[] getTaxonomyParentSequences(int childId);
    
    int[] getRoots(TaxonomyCoordinate sc);
}
