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

/**
 *
 * @author kec
 */
public class IsomorphicSearchTopDownNode implements Comparable<IsomorphicSearchTopDownNode> {
    
    int depth;
    int parentNodeId;
    NodeSemantic nodeSemantic;
    int nodeId;

    public IsomorphicSearchTopDownNode(int depth, int parentNodeId, 
            NodeSemantic nodeSemantic, int nodeId) {
        this.depth = depth;
        this.parentNodeId = parentNodeId;
        this.nodeSemantic = nodeSemantic;
        this.nodeId = nodeId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.parentNodeId;
        hash = 79 * hash + this.nodeId;
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
        final IsomorphicSearchTopDownNode other = (IsomorphicSearchTopDownNode) obj;
        if (this.depth != other.depth) {
            return false;
        }
        if (this.parentNodeId != other.parentNodeId) {
            return false;
        }
        if (this.nodeSemantic != other.nodeSemantic) {
            return false;
        }
        return this.nodeId == other.nodeId;
    }
    

    @Override
    public int compareTo(IsomorphicSearchTopDownNode o) {
        int comparison = Integer.compare(depth, o.depth);
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(parentNodeId, o.parentNodeId);
        if (comparison != 0) {
            return comparison;
        }
        comparison = nodeSemantic.compareTo(o.nodeSemantic);
        if (comparison != 0) {
            return comparison;
        }
        return Integer.compare(nodeId, o.nodeId);
    }

    @Override
    public String toString() {
        return "IsomorphicSearchNode{" + 
                "depth=" + depth + ", parent=" + parentNodeId + ", semantic=" + 
                nodeSemantic + ", id=" + nodeId + '}';
    }
    
}
