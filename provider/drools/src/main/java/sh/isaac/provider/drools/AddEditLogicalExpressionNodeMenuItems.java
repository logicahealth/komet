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
import javafx.scene.control.MenuItem;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class AddEditLogicalExpressionNodeMenuItems {
    final List<MenuItem> menuItems = new ArrayList<>();
    final Manifold manifold;
    final LogicNode nodeToEdit;
    final LogicalExpression expressionContiningNode;
    final Consumer<PropertySheetMenuItem> propertySheetConsumer;

    public AddEditLogicalExpressionNodeMenuItems(Manifold manifold, LogicNode nodeToEdit, LogicalExpression expressionContiningNode, Consumer<PropertySheetMenuItem> propertySheetConsumer) {
        this.manifold = manifold;
        this.nodeToEdit = nodeToEdit;
        this.expressionContiningNode = expressionContiningNode;
        this.propertySheetConsumer = propertySheetConsumer;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public LogicNode getNodeToEdit() {
        return nodeToEdit;
    }

    public NodeSemantic getNodeSemantic() {
        return nodeToEdit.getNodeSemantic();
    }

    public LogicalExpression getExpressionContiningNode() {
        return expressionContiningNode;
    }

    @Override
    public String toString() {
        return "AddEditLogicalExpressionNodeMenuItems{nodeToEdit: " + nodeToEdit + ", expression: " + expressionContiningNode + '}';
    }
    
    
}
