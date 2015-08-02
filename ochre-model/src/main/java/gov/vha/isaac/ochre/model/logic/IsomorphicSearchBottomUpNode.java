/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import org.apache.mahout.math.function.IntProcedure;
import org.apache.mahout.math.set.OpenIntHashSet;

import java.util.Arrays;

/**
 *
 * @author kec
 */
public class IsomorphicSearchBottomUpNode implements Comparable<IsomorphicSearchBottomUpNode> {
    
    final NodeSemantic nodeSemantic;
    final ConceptSequenceSet conceptsReferencedAtNodeOrAbove;
    int conceptsReferencedAtNodeOrAboveHash;
    final int childNodeId;
    final int nodeId;
    final int size;

    public IsomorphicSearchBottomUpNode(NodeSemantic nodeSemantic,
                                        OpenIntHashSet conceptsReferencedAtNodeOrAbove,
                                        int childNodeId, int nodeId) {
        this.nodeSemantic = nodeSemantic;
        this.conceptsReferencedAtNodeOrAbove = ConceptSequenceSet.of(conceptsReferencedAtNodeOrAbove);
        this.size = conceptsReferencedAtNodeOrAbove.size();
        this.conceptsReferencedAtNodeOrAboveHash = 1;
        for (int element: conceptsReferencedAtNodeOrAbove.keys().elements()) {
            conceptsReferencedAtNodeOrAboveHash = 31 * conceptsReferencedAtNodeOrAboveHash + element;
        }
        this.childNodeId = childNodeId;
        this.nodeId = nodeId;
    }

    @Override
    public int compareTo(IsomorphicSearchBottomUpNode o) {
        int comparison = this.nodeSemantic.compareTo(o.nodeSemantic);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(childNodeId, o.childNodeId);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(this.size,
                o.size);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(this.conceptsReferencedAtNodeOrAboveHash,
                o.conceptsReferencedAtNodeOrAboveHash);
        if (comparison != 0) {
            return comparison;
        }
        comparison = this.conceptsReferencedAtNodeOrAbove.compareTo(o.conceptsReferencedAtNodeOrAbove);
        if (comparison != 0) {
            return comparison;
        }
        return Integer.compare(nodeId, o.nodeId);
    }
    @Override
    public int hashCode() {
        return conceptsReferencedAtNodeOrAboveHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((IsomorphicSearchBottomUpNode) obj) == 0;
    }

    @Override
    public String toString() {
        return "BottomUpNode{"+ nodeSemantic + ", conceptsAtOrAbove=" + conceptsReferencedAtNodeOrAbove + ", childId=" + childNodeId + ", nodeId=" + nodeId + '}';
    }

}
