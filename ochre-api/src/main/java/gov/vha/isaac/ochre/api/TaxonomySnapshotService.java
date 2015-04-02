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

import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;

/**
 *
 * @author kec
 */
public interface TaxonomySnapshotService {

    Tree getTaxonomyTree();
    
    boolean isChildOf(int childId, int parentId);
    
    boolean isKindOf(int childId, int parentId);
    
    ConceptSequenceSet getKindOfSequenceSet(int rootId);
     
    int[] getAllRelationshipOriginSequencesActive(int destination);

    int[] getTaxonomyChildSequencesActive(int parentId);

    int[] getTaxonomyParentSequencesActive(int childId);

    int[] getRoots();    
}
