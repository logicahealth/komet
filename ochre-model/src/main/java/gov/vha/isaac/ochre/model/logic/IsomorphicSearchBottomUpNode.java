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
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class IsomorphicSearchBottomUpNode implements Comparable<IsomorphicSearchBottomUpNode> {
    
    NodeSemantic nodeSemantic;
    ConceptSequenceSet conceptsReferencedAtNodeOrAbove;
    int childNodeId;
    int nodeId;

    public IsomorphicSearchBottomUpNode(NodeSemantic nodeSemantic, 
            ConceptSequenceSet conceptsReferencedAtNodeOrAbove, int childNodeId, int nodeId) {
        this.nodeSemantic = nodeSemantic;
        this.conceptsReferencedAtNodeOrAbove = conceptsReferencedAtNodeOrAbove;
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
        comparison = Integer.compare(this.conceptsReferencedAtNodeOrAbove.size(),
                o.conceptsReferencedAtNodeOrAbove.size());
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
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.nodeSemantic);
        hash = 61 * hash + this.nodeId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IsomorphicSearchBottomUpNode other = (IsomorphicSearchBottomUpNode) obj;
        if (this.nodeId != other.nodeId) {
            return false;
        }
        if (this.nodeSemantic != other.nodeSemantic) {
            return false;
        }
        if (this.childNodeId != other.childNodeId) {
            return false;
        }
        return Objects.equals(this.conceptsReferencedAtNodeOrAbove, other.conceptsReferencedAtNodeOrAbove);
    }

    @Override
    public String toString() {
        return "BottomUpNode{"+ nodeSemantic + ", conceptsAtOrAbove=" + Arrays.toString(conceptsReferencedAtNodeOrAbove.asArray()) + ", childId=" + childNodeId + ", nodeId=" + nodeId + '}';
    }
}
