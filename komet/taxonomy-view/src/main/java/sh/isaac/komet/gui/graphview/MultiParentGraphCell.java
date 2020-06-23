/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.komet.gui.graphview;

//~--- JDK imports ------------------------------------------------------------


import java.util.ArrayList;
import java.util.Collections;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.geometry.Rectangle2D;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.NonInvertibleTransformException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.collection.ImmutableCollection;
import sh.isaac.api.Get;
import sh.isaac.api.Edge;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.interfaces.DraggableWithImage;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.menu.MenuItemWithText;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link TreeCell} for rendering {@link ConceptChronology<ConceptVersion>} objects.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
final public class MultiParentGraphCell
        extends TreeCell<ConceptChronology>
         implements DraggableWithImage {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private double   dragOffset = 0;
   private TilePane graphicTilePane;
   private String conceptDescriptionText; // Cached to speed up updates 
   
   //~--- constructors --------------------------------------------------------

   MultiParentGraphCell(TreeView<ConceptChronology> treeView) {
      super();
      updateTreeView(treeView);
      setSkin(new MultiParentGraphCellSkin(this));

      // Allow drags
      
      this.setOnDragDetected(new DragDetectedCellEventHandler());
      this.setOnDragDone(new DragDoneEventHandler());

   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void updateItem(ConceptChronology concept, boolean empty) {
      // Handle right-clicks.7c21b6c5-cf11-5af9-893b-743f004c97f5

      //profiling showed set context menu very slow. Maybe only set on right click...
      //setContextMenu(buildContextMenu(concept));

      try {
         super.updateItem(concept, empty);

         if (empty) {
            setText("");
            conceptDescriptionText = null;
            setGraphic(null);
         } else {
            final MultiParentGraphItemImpl treeItem = (MultiParentGraphItemImpl) getTreeItem();
            conceptDescriptionText = treeItem.toString();

               if (!treeItem.isLeaf()) {
                    Node iv = treeItem.isExpanded() ? Iconography.TAXONOMY_CLICK_TO_CLOSE.getIconographic(24)
                                               : Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic(24);

                    setDisclosureNode(iv);
               }

               ConceptSnapshotService conceptSnapshotService = Get.conceptService()
                       .getSnapshot(treeItem.getGraphView().getManifoldCoordinate());

            if (concept != null) {
               if (conceptSnapshotService.isConceptActive(concept.getNid())) {
                  setFont(Font.font(getFont().getFamily(), FontPosture.REGULAR, getFont().getSize()));
               } else {
                  setFont(Font.font(getFont().getFamily(), FontPosture.ITALIC, getFont().getSize()));
               }

               setText(conceptDescriptionText);
               setGraphic(treeItem.computeGraphic());
            }
         }
      } catch (Exception e) {
         LOG.error("Unexpected error updating cell", e);
         setText("Internal error!");
         setGraphic(null);
      }
   }

   private ContextMenu buildContextMenu(ConceptChronology concept) {
      if (concept != null) {
         MultiParentGraphItemImpl graphItem = (MultiParentGraphItemImpl) getTreeItem();
         MultiParentGraphViewController graphView = graphItem.getGraphView();
         ManifoldCoordinate menuManifold = graphView.getManifoldCoordinate();
         
      ContextMenu cm    = new ContextMenu();
      MenuItem    item1 = new MenuItemWithText("About " + menuManifold.getPreferredDescriptionText(concept));

      item1.setOnAction(
          (ActionEvent e) -> {
             int conceptNid = ((MultiParentGraphItemImpl) getTreeItem()).getConceptNid();
             ManifoldCoordinate manifold = ((MultiParentGraphItemImpl) getTreeItem()).getGraphView().getManifoldCoordinate();
             graphItem.getValue();
          });

      MenuItem item2 = new MenuItemWithText("Preferences");

      item2.setOnAction(
          (ActionEvent e) -> {
             System.out.println("Preferences");
          });
      cm.getItems()
        .addAll(item1, item2);
      return cm;
      }
      return null;
   }

   protected void openOrCloseParent(MultiParentGraphItemImpl treeItem) {
      ConceptChronology value = treeItem.getValue();

      if (value != null) {
         treeItem.setValue(null);

         MultiParentGraphItemImpl parentItem = (MultiParentGraphItemImpl) treeItem.getParent();
         ObservableList<TreeItem<ConceptChronology>> siblings = parentItem.getChildren();

         if (treeItem.isSecondaryParentOpened()) {
            removeExtraParents(treeItem, siblings);
         } else {
            ImmutableCollection<Edge> allParents = treeItem.getGraphView()
                                       .getNavigator()
                                       .getParentLinks(value.getNid());
            ArrayList<MultiParentGraphItemImpl> secondaryParentItems = new ArrayList<>();

            for (Edge parentLink: allParents) {
               if ((allParents.size() == 1) || (parentLink.getDestinationNid() != parentItem.getValue().getNid())) {
                  ConceptChronology parentChronology = Get.concept(parentLink.getDestinationNid());
                  MultiParentGraphItemImpl extraParentItem = new MultiParentGraphItemImpl(parentChronology, treeItem.getGraphView(), parentLink.getTypeNid(), null);
                  ManifoldCoordinate manifold = treeItem.getGraphView().getManifoldCoordinate();
                  extraParentItem.setDefined(parentChronology.isSufficientlyDefined(manifold.getVertexStampFilter(), manifold.getLogicCoordinate()));
                  extraParentItem.setMultiParentDepth(treeItem.getMultiParentDepth() + 1);
                  secondaryParentItems.add(extraParentItem);
               }
            }

            Collections.sort(secondaryParentItems);
            Collections.reverse(secondaryParentItems);

            int startIndex = siblings.indexOf(treeItem);

            for (MultiParentGraphItemImpl extraParentItem: secondaryParentItems) {
               parentItem.getChildren()
                         .add(startIndex++, extraParentItem);
               treeItem.getExtraParents()
                       .add(extraParentItem);
            }
         }

         treeItem.setValue(value);
         treeItem.setSecondaryParentOpened(!treeItem.isSecondaryParentOpened());
         treeItem.computeGraphic();
      }
   }

   private void removeExtraParents(MultiParentGraphItemImpl treeItem,
                                   ObservableList<TreeItem<ConceptChronology>> siblings) {
      treeItem.getExtraParents().stream().map((extraParent) -> {
         removeExtraParents(extraParent, siblings);
         return extraParent;
      }).forEachOrdered((extraParent) -> {
         siblings.remove(extraParent);
      });
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Image getDragImage() {
      //TODO see if we can replace this method with DragImageMaker...
      SnapshotParameters snapshotParameters = new SnapshotParameters();

      dragOffset = 0;

      double width  = this.getWidth();
      double height = this.getHeight();

      if (graphicTilePane != null) {
         // The height difference and width difference are to account for possible 
         // changes in size of an object secondary to a hover (which might cause a 
         // -fx-effect:  dropshadow... or similar, whicn will create a difference in the 
         // tile pane height, but not cause a change in getLayoutBounds()...
         // I don't know if this is a workaround for a bug, or if this is expected
         // behaviour for some reason...

         double layoutWidth     = graphicTilePane.getLayoutBounds()
                                       .getWidth();
         double widthDifference = graphicTilePane.getBoundsInParent()
                                                  .getWidth() - layoutWidth;
         double widthAdjustment       = 0;
         if (widthDifference > 0) {
            widthDifference = Math.rint(widthDifference);
            widthAdjustment       = widthDifference / 2;
         }

         dragOffset = graphicTilePane.getBoundsInParent()
                                     .getMinX() + widthAdjustment;
         width      = this.getWidth() - dragOffset;
         height     = this.getLayoutBounds().getHeight();
      }
      
      try {
         snapshotParameters.setTransform(this.getLocalToParentTransform().createInverse());
      } catch (NonInvertibleTransformException ex) {
         throw new RuntimeException(ex);
      }
      snapshotParameters.setViewport(new Rectangle2D(dragOffset -2, 0, width, height));
      return snapshot(snapshotParameters, null);
   }

   @Override
   public double getDragViewOffsetX() {
      return dragOffset;
   }


   @Override
   public String toString() {
      if (conceptDescriptionText == null) {
         MultiParentGraphItemImpl treeItem = (MultiParentGraphItemImpl) getTreeItem();
         conceptDescriptionText = treeItem.toString();
      }
      return conceptDescriptionText;
   }
   
   
}

