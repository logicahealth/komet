/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.komet.gui.treeview;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import sh.isaac.api.component.concept.ConceptChronology;

/**
 * @author kec
 */
public class MultiParentTreeCellBehavior extends TreeCellBehavior<ConceptChronology> {

    public MultiParentTreeCellBehavior(TreeCell<ConceptChronology> control) {
        super(control);
    }

    @Override
    protected boolean handleDisclosureNode(double x, double y) {
        MultiParentTreeCell treeCell = (MultiParentTreeCell) getNode();
        Node disclosureNode = treeCell.getDisclosureNode();
        if (disclosureNode != null) {
            if (disclosureNode.getBoundsInParent().contains(x, y)) {
                if (treeCell.getTreeItem() != null) {
                    treeCell.getTreeItem().setExpanded(!treeCell.getTreeItem().isExpanded());
                }
                return true;
            }
        }
        MultiParentTreeItemImpl treeItem = (MultiParentTreeItemImpl) treeCell.getTreeItem();
        if (treeItem.isMultiParent() || treeItem.getMultiParentDepth() > 0) {
            Node multiParentDisclosureNode = treeCell.getGraphic();
            if (multiParentDisclosureNode != null) {
                if (multiParentDisclosureNode.getBoundsInParent().contains(x, y)) {
                    if (treeCell.getTreeItem() != null) {
                        treeCell.openOrCloseParent(treeItem);
                        return true;
                    }

                }
            }
        }
        return false;
    }

}
