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



package sh.isaac.komet.gui.treeview;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.NonInvertibleTransformException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.komet.gui.interfaces.DraggableWithImage;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link TreeCell} for rendering {@link ConceptChronology<ConceptVersion>} objects.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
final public class MultiParentTreeCell
        extends TreeCell<ConceptChronology>
         implements DraggableWithImage {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private double   dragOffset = 0;
   private TilePane graphicTilePane;

   //~--- constructors --------------------------------------------------------

   MultiParentTreeCell(TreeView<ConceptChronology> treeView) {
      super();
      updateTreeView(treeView);
      setSkin(new MultiParentTreeCellSkin(this));

      // Handle left-clicks.
      ClickListener eventHandler = new ClickListener();

      setOnMouseClicked(eventHandler);

      // Handle right-clicks.7c21b6c5-cf11-5af9-893b-743f004c97f5
      ContextMenu cm = buildContextMenu();

      setContextMenu(cm);

      // Allow drags
      
      this.setOnDragDetected(new DragDetectedCellEventHandler());
      this.setOnDragDone(new DragDoneEventHandler());

   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void updateItem(ConceptChronology taxRef, boolean empty) {
      boolean addProgressIndicator = false;

      try {
         super.updateItem(taxRef, empty);

         if (empty) {
            setText("");
            setGraphic(null);
         } else {
            final MultiParentTreeItem treeItem = (MultiParentTreeItem) getTreeItem();
 

               if (!treeItem.isLeaf()) {
               Node iv = treeItem.isExpanded() ? Iconography.TAXONOMY_CLICK_TO_CLOSE.getIconographic()
                                               : Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic();
                  if (addProgressIndicator) {
                     StackPane progressStack = new StackPane();

                     progressStack.getChildren()
                                        .add(iv);
                     addProgressIndicator(treeItem, progressStack);
                     setDisclosureNode(progressStack);
                  } else {
                     setDisclosureNode(iv);
                  }
               }

               ConceptSnapshotService conceptSnapshotService = treeItem.getTreeView().manifoldProperty
                                                                    .get()
                                                                    .getConceptSnapshotService();

            if (taxRef != null) {
               ConceptSnapshot conceptSnapshot = conceptSnapshotService.getConceptSnapshot(taxRef.getConceptSequence());

               if (conceptSnapshotService.isConceptActive(taxRef.getConceptSequence())) {
                  setFont(Font.font(getFont().getFamily(), FontPosture.REGULAR, getFont().getSize()));
               } else {
                  setFont(Font.font(getFont().getFamily(), FontPosture.ITALIC, getFont().getSize()));
               }

               setText(conceptSnapshot.getDescription()
                                      .getText());

               if (getGraphic() == null) {
                  graphicTilePane = new TilePane();

                  // Set to the number of icons for display. Will need to make dynamic if more than one is possible.
                  graphicTilePane.setPrefColumns(1);
                  graphicTilePane.getChildren()
                                 .addAll(treeItem.computeGraphic());
                  setGraphic(graphicTilePane);
               }
            }
         }
      } catch (Exception e) {
         LOG.error("Unexpected error updating cell", e);
         setText("Internal error!");
         setGraphic(null);
      }
   }

   private void addProgressIndicator(final MultiParentTreeItem treeItem, StackPane progressStack) {
      ProgressIndicator pi = new ProgressIndicator();

      pi.setPrefSize(16, 16);
      pi.setMaxSize(16, 16);
      pi.progressProperty()
        .bind(treeItem.getChildLoadPercentComplete());
      pi.visibleProperty()
        .bind(
            treeItem.getChildLoadPercentComplete()
                    .lessThan(1.0)
                    .and(treeItem.getChildLoadPercentComplete()
                                 .greaterThanOrEqualTo(-1.0)));
      pi.setMouseTransparent(true);
      progressStack.getChildren()
                         .add(pi);
      StackPane.setAlignment(pi, Pos.CENTER);

      // StackPane.setMargin(pi, new Insets(0, 10, 0, 0));
   }

   private ContextMenu buildContextMenu() {
      ContextMenu cm    = new ContextMenu();
      MenuItem    item1 = new MenuItem("About");

      item1.setOnAction(
          (ActionEvent e) -> {
             System.out.println("About");
          });

      MenuItem item2 = new MenuItem("Preferences");

      item2.setOnAction(
          (ActionEvent e) -> {
             System.out.println("Preferences");
          });
      cm.getItems()
        .addAll(item1, item2);
      return cm;
   }

   private void openOrCloseParent(MultiParentTreeItem treeItem)
            throws IOException {
      ConceptChronology value = treeItem.getValue();

      if (value != null) {
         treeItem.setValue(null);

         MultiParentTreeItem parentItem = (MultiParentTreeItem) treeItem.getParent();
         ObservableList<TreeItem<ConceptChronology>> siblings = parentItem.getChildren();

         if (treeItem.isSecondaryParentOpened()) {
            removeExtraParents(treeItem, siblings);
         } else {
            int[] allParents = treeItem.getTreeView()
                                       .getTaxonomyTree()
                                       .getParentSequences(value.getConceptSequence());
            ArrayList<MultiParentTreeItem> secondaryParentItems = new ArrayList<>();

            for (int parentSequence: allParents) {
               if ((allParents.length == 1) || (parentSequence != parentItem.getValue().getConceptSequence())) {
                  MultiParentTreeItem extraParentItem = new MultiParentTreeItem(parentSequence, treeItem.getTreeView());

                  extraParentItem.setMultiParentDepth(treeItem.getMultiParentDepth() + 1);
                  secondaryParentItems.add(extraParentItem);
               }
            }

            Collections.sort(secondaryParentItems);
            Collections.reverse(secondaryParentItems);

            int startIndex = siblings.indexOf(treeItem);

            for (MultiParentTreeItem extraParentItem: secondaryParentItems) {
               parentItem.getChildren()
                         .add(startIndex++, extraParentItem);
               treeItem.getExtraParents()
                       .add(extraParentItem);
               Get.executor()
                  .execute(new GetMultiParentTreeItemConceptCallable(extraParentItem, false));
            }
         }

         treeItem.setValue(value);
         treeItem.setSecondaryParentOpened(!treeItem.isSecondaryParentOpened());
         treeItem.computeGraphic();
      }
   }

   private void removeExtraParents(MultiParentTreeItem treeItem,
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

   //~--- inner classes -------------------------------------------------------

   /**
    * Listens for mouse clicks to expand/collapse node.
    */
   private final class ClickListener
            implements EventHandler<MouseEvent> {
      @Override
      public void handle(MouseEvent t) {
         if (getItem() != null) {
            if (getGraphic().getBoundsInParent()
                            .contains(t.getX(), t.getY())) {
               MultiParentTreeItem item = (MultiParentTreeItem) getTreeItem();

               if (item.isMultiParent() || (item.getMultiParentDepth() > 0)) {
                  try {
                     openOrCloseParent(item);
                  } catch (IOException ex) {
                     LOG.error(ex.getLocalizedMessage(), ex);
                  }
               }
            }
         }
      }
   }
}

