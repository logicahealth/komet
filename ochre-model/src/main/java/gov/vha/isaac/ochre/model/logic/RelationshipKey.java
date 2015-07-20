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

import gov.vha.isaac.ochre.api.logic.Node;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;

/**
 *
 * @author kec
 */
public class RelationshipKey implements Comparable<RelationshipKey> {

    ConceptSequenceSet conceptsReferencedAtNodeOrBelow = new ConceptSequenceSet();

    public RelationshipKey(int nodeId, LogicalExpressionOchreImpl expression) {
        addNodes(nodeId, expression);
    }
    
    private void addNodes(int nodeId, LogicalExpressionOchreImpl expression) {
        Node node = expression.getNode(nodeId);
        expression.getNode(nodeId).addConceptsReferencedByNode(conceptsReferencedAtNodeOrBelow);
        node.getChildStream().forEach((childNode) -> addNodes(childNode.getNodeIndex(), expression));
    }
 
    @Override
    public int compareTo(RelationshipKey o) {
        return conceptsReferencedAtNodeOrBelow.compareTo(o.conceptsReferencedAtNodeOrBelow);
    }
    
}
