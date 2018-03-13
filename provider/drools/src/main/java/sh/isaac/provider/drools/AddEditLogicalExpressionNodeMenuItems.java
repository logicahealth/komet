/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithNids;
import sh.isaac.model.logic.node.internal.TypedNodeWithNids;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class AddEditLogicalExpressionNodeMenuItems {
    final List<Action> actionItems = new ArrayList<>();
    final Manifold manifold;
    final LogicNode nodeToEdit;
    final LogicalExpressionImpl expressionContiningNode;
    final Consumer<LogicalExpression> expressionUpdater;

    public AddEditLogicalExpressionNodeMenuItems(Manifold manifold, 
            LogicNode nodeToEdit, 
            LogicalExpression expressionContiningNode,
            Consumer<LogicalExpression> expressionUpdater) {
        this.manifold = manifold;
        this.nodeToEdit = nodeToEdit;
        this.expressionContiningNode = (LogicalExpressionImpl) expressionContiningNode;
        this.expressionUpdater = expressionUpdater;
    }

    public List<Action> getActionItems() {
        return actionItems;
    }

    public LogicNode getNodeToEdit() {
        return nodeToEdit;
    }

    public int getNodeToEditTypeConceptNid() {
        if (nodeToEdit instanceof TypedNodeWithNids) {
            TypedNodeWithNids typedNode = (TypedNodeWithNids) nodeToEdit;
            return typedNode.getTypeConceptNid();
        }
        return Integer.MAX_VALUE;
    }

    public NodeSemantic getNodeSemantic() {
        return nodeToEdit.getNodeSemantic();
    }

    public LogicalExpression getExpressionContiningNode() {
        return expressionContiningNode;
    }
    
    public void addDeleteNodeAction() {
       Action deleteAction = new Action("Delete", (ActionEvent event) -> {
           this.expressionUpdater.accept(expressionContiningNode.removeNode(nodeToEdit));
       });
       actionItems.add(deleteAction);
    }

    public void addIsaNodeAction() {
       addIsaNodeAction(MetaData.METADATA____SOLOR);
    }

    public void addIsaNodeAction(ConceptSpecification spec) {
       addIsaNodeAction(spec.getNid());
    }
    
    public void addIsaNodeAction(int conceptNid) {
       Action addIsaAction = new Action("Add is-a " + manifold.getPreferredDescriptionText(conceptNid), (ActionEvent event) -> {
           ConceptNodeWithNids newIsa = expressionContiningNode.Concept(conceptNid);
           for (LogicNode node: nodeToEdit.getChildren()) {
               if (node.getNodeSemantic() == NodeSemantic.AND) {
                   node.addChildren(newIsa);
                   break;
               }
           }
           this.expressionUpdater.accept(expressionContiningNode);
       });
       actionItems.add(addIsaAction);
    }
    
    public void addRoleAction(ConceptSpecification typeSpec, ConceptSpecification restrictionSpec) {
        addRoleAction(typeSpec.getNid(), restrictionSpec.getNid());
    }
    
    public void addRoleAction(int typeNid, int restrictionNid) {
        StringBuilder builder = new StringBuilder();
        builder.append("Add  ");
        builder.append(manifold.getPreferredDescriptionText(typeNid));
        builder.append(" ");
        builder.append(manifold.getPreferredDescriptionText(restrictionNid));
        Action addIsaAction = new Action(builder.toString(), (ActionEvent event) -> {
           RoleNodeSomeWithNids newRole = expressionContiningNode.SomeRole(typeNid, expressionContiningNode.Concept(restrictionNid));
           for (LogicNode node: nodeToEdit.getChildren()) {
               if (node.getNodeSemantic() == NodeSemantic.AND) {
                   node.addChildren(newRole);
                   break;
               }
           }
           this.expressionUpdater.accept(expressionContiningNode);
       });
       actionItems.add(addIsaAction);
    }
    
    @Override
    public String toString() {
        return "AddEditLogicalExpressionNodeMenuItems{nodeToEdit: " + nodeToEdit + ", expression: " + expressionContiningNode + '}';
    }
    
    
}
