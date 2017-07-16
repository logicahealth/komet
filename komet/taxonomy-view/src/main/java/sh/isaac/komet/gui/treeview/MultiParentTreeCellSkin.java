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

import com.sun.javafx.scene.control.skin.TreeCellSkin;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;

/**
 *
 * @author kec
 */
public class MultiParentTreeCellSkin extends TreeCellSkin<ConceptChronology<? extends ConceptVersion<?>>> {

   private final double defaultDisclosureWidth = 18;
   private boolean disclosureNodeDirty = true;

   public MultiParentTreeCellSkin(TreeCell control) {
      super(control);
   }

   @Override
   protected void handleControlPropertyChanged(String p) {
      if (null != p) {
         switch (p) {
            case "TREE_ITEM":
               disclosureNodeDirty = true;
               break;
            default:
               break;
         }
      }
      super.handleControlPropertyChanged(p);
   }

   @Override
   protected void layoutChildren(double x, final double y,
           double w, final double h) {
      // RT-25876: can not null-check here as this prevents empty rows from
      // being cleaned out.
      // if (treeItem == null) return;

      TreeView<ConceptChronology<? extends ConceptVersion<?>>> tree = getSkinnable().getTreeView();
      if (tree == null) {
         return;
      }
      MultiParentTreeItem treeItem = (MultiParentTreeItem) getSkinnable().getTreeItem();

      if (disclosureNodeDirty) {
         updateDisclosureNode();
         disclosureNodeDirty = false;
      }

      Node disclosureNode = getSkinnable().getDisclosureNode();

      int level = tree.getTreeItemLevel(treeItem);
      if (!tree.isShowRoot()) {
         level--;
      }
      double leftMargin = getIndent() * level;
      if (treeItem != null) {
         leftMargin += treeItem.getMultiParentDepth() * getIndent();
      }
      x += leftMargin;

      // position the disclosure node so that it is at the proper indent
      boolean disclosureVisible = disclosureNode != null && treeItem != null && !treeItem.isLeaf();

      double disclosureWidth = defaultDisclosureWidth;

      if (disclosureVisible) {
         if (disclosureNode == null || disclosureNode.getScene() == null) {
            updateChildren();
         }

         if (disclosureNode != null) {
            //disclosureWidth = disclosureNode.prefWidth(h);

            double ph = disclosureNode.prefHeight(disclosureWidth);

            disclosureNode.resize(disclosureWidth, ph);
            positionInArea(disclosureNode, x, y,
                    disclosureWidth, ph, /*baseline ignored*/ 0,
                    HPos.CENTER, VPos.CENTER);
         }
      }

      // determine starting point of the graphic or cell node, and the
      // remaining width available to them
      final int padding = treeItem != null && treeItem.getGraphic() == null ? 0 : 3;
      x += disclosureWidth + padding;
      w -= (leftMargin + disclosureWidth + padding);

      // Rather ugly fix for RT-38519, where graphics are disappearing in
      // certain circumstances
      Node graphic = getSkinnable().getGraphic();
      if (graphic != null && !getChildren().contains(graphic)) {
         getChildren().add(graphic);
      }

      layoutLabelInArea(x, y, w, h);
   }

   @Override
   protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
      double labelWidth = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);

      double pw = snappedLeftInset() + snappedRightInset();

      TreeView<ConceptChronology<? extends ConceptVersion<?>>> tree = getSkinnable().getTreeView();
      if (tree == null) {
         return pw;
      }
      MultiParentTreeItem treeItem = (MultiParentTreeItem) getSkinnable().getTreeItem();

      if (treeItem == null) {
         return pw;
      }

      pw = labelWidth;

      // determine the amount of indentation
      int level = tree.getTreeItemLevel(treeItem);
      if (!tree.isShowRoot()) {
         level--;
      }
      pw += getIndent() * level;

      // include the disclosure node width
      Node disclosureNode = getSkinnable().getDisclosureNode();
      double disclosureNodePrefWidth = disclosureNode == null ? 0 : disclosureNode.prefWidth(-1);
      pw += Math.max(defaultDisclosureWidth, disclosureNodePrefWidth);

      return pw;
   }

   private void updateDisclosureNode() {
      if (getSkinnable().isEmpty()) {
         return;
      }

      Node disclosureNode = getSkinnable().getDisclosureNode();
      if (disclosureNode == null) {
         return;
      }
      MultiParentTreeItem treeItem = (MultiParentTreeItem) getSkinnable().getTreeItem();

      boolean disclosureVisible = treeItem != null && !treeItem.isLeaf();
      disclosureNode.setVisible(disclosureVisible);

      if (!disclosureVisible) {
         getChildren().remove(disclosureNode);
      } else if (disclosureNode.getParent() == null) {
         getChildren().add(disclosureNode);
         disclosureNode.toFront();
      } else {
         disclosureNode.toBack();
      }

      // RT-26625: [TreeView, TreeTableView] can lose arrows while scrolling
      // RT-28668: Ensemble tree arrow disappears
      if (disclosureNode.getScene() != null) {
         disclosureNode.applyCss();
      }
   }

}
