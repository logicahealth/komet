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
package sh.komet.gui.control;

//~--- JDK imports ------------------------------------------------------------
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;


import javafx.geometry.Insets;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import javafx.stage.WindowEvent;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;
import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptLabel
        extends Label {

   TransferMode[] transferMode = null;
   Manifold manifold;
   Consumer<ConceptLabel> descriptionTextUpdater;
   Background originalBackground;

   //~--- constructors --------------------------------------------------------
   public ConceptLabel(Manifold manifold,
           Consumer<ConceptLabel> descriptionTextUpdater) {
      this.manifold = manifold;
      this.descriptionTextUpdater = descriptionTextUpdater;
      this.manifold.focusedConceptChronologyProperty().addListener(
              (ObservableValue<? extends ConceptChronology> observable,
                      ConceptChronology oldValue,
                      ConceptChronology newValue) -> {
                 this.descriptionTextUpdater.accept(this);
              });
      this.getStyleClass().add(CONCEPT_LABEL.toString());
      this.setOnDragOver(this::handleDragOver);
      this.setOnDragEntered(this::handleDragEntered);
      this.setOnDragDetected(this::handleDragDetected);
      this.setOnDragExited(this::handleDragExited);
      this.setOnDragDropped(this::handleDragDropped);
      this.setOnDragDone(this::handleDragDone);
      this.setMinWidth(100);

      ContextMenu contextMenu = new ContextMenu();

      for (String manifoldGroupName : Manifold.getGroupNames()) {
         MenuItem item = new MenuItem(manifoldGroupName + " history");
         contextMenu.getItems()
                 .add(item);
      }

      this.setContextMenu(contextMenu);
      contextMenu.setOnShowing(this::handle);
   }

   //~--- methods -------------------------------------------------------------
   private void handle(WindowEvent event) {
      ContextMenu contextMenu = (ContextMenu) event.getSource();
      contextMenu.getItems().clear();
      Menu manifoldHistoryMenu = new Menu("history");
      contextMenu.getItems().add(manifoldHistoryMenu);
      Collection<HistoryRecord> historyCollection = this.manifold.getHistoryRecords();
      
      setupHistoryMenuItem(historyCollection, manifoldHistoryMenu);
      
      
      for (String manifoldGroupName : Manifold.getGroupNames()) {
         Menu groupHistory = new Menu(manifoldGroupName + " history");
         contextMenu.getItems().add(groupHistory);
         setupHistoryMenuItem(Manifold.getGroupHistory(manifoldGroupName), groupHistory);
      }
   }

   private void setupHistoryMenuItem(Collection<HistoryRecord> historyCollection, Menu manifoldHistoryMenu) {
      for (HistoryRecord historyRecord: historyCollection) {
         MenuItem historyItem = new MenuItem(historyRecord.getComponentString());
         historyItem.setUserData(historyRecord);
         historyItem.setOnAction((ActionEvent actionEvent) -> {
            unlink();
            MenuItem historyMenuItem = (MenuItem) actionEvent.getSource();
            HistoryRecord itemHistoryRecord = (HistoryRecord) historyItem.getUserData();
            this.manifold.setFocusedConceptChronology(Get.concept(itemHistoryRecord.getComponentId()));
         });
         manifoldHistoryMenu.getItems().add(historyItem);
      }
   }

   private void unlink() {
      if (!this.manifold
              .getGroupName()
              .equals(Manifold.UNLINKED_GROUP_NAME)) {
         this.manifold
                 .setGroupName(Manifold.UNLINKED_GROUP_NAME);
      }
   }

   private void handleDragDetected(MouseEvent event) {
      System.out.println("Drag detected: " + event);

      DragImageMaker dragImageMaker = new DragImageMaker(this);
      Dragboard db = this.startDragAndDrop(TransferMode.COPY);

      db.setDragView(dragImageMaker.getDragImage());

      /* put a string on dragboard */
      ClipboardContent content = new ClipboardContent();

      content.putString(this.getText());
      db.setContent(content);
      event.consume();
   }

   private void handleDragDone(DragEvent event) {
      System.out.println("Dragging done: " + event);
      this.setBackground(originalBackground);
      this.transferMode = null;
   }

   private void handleDragDropped(DragEvent event) {
      System.out.println("Dragging dropped: " + event);

      unlink();

      Dragboard db = event.getDragboard();

      if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
         ConceptChronology conceptChronology = Get.serializer()
                 .toObject(db, IsaacClipboard.ISAAC_CONCEPT);

         this.manifold
                 .setFocusedConceptChronology(conceptChronology);
      } else if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
         ConceptVersion conceptVersion = Get.serializer()
                 .toObject(db, IsaacClipboard.ISAAC_CONCEPT_VERSION);

         this.manifold
                 .setFocusedConceptChronology(conceptVersion.getChronology());
      } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION)) {
         SememeChronology sememeChronology = Get.serializer()
                 .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION);

         this.manifold
                 .setFocusedConceptChronology(
                         Get.conceptService()
                                 .getConcept(sememeChronology.getReferencedComponentNid()));
      } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION_VERSION)) {
         DescriptionVersion descriptionVersion = Get.serializer()
                 .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION_VERSION);

         this.manifold
                 .setFocusedConceptChronology(
                         Get.conceptService()
                                 .getConcept(descriptionVersion.getReferencedComponentNid()));
      }

      this.setBackground(originalBackground);
   }

   private void handleDragEntered(DragEvent event) {
      System.out.println("Dragging entered: " + event);
      this.originalBackground = this.getBackground();

      Color backgroundColor;
      Set<DataFormat> contentTypes = event.getDragboard()
              .getContentTypes();

      if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.CONCEPT_TYPES)) {
         backgroundColor = Color.AQUA;
         this.transferMode = TransferMode.COPY_OR_MOVE;
      } else if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.DESCRIPTION_TYPES)) {
         backgroundColor = Color.OLIVEDRAB;
         this.transferMode = TransferMode.COPY_OR_MOVE;
      } else {
         backgroundColor = Color.RED;
         this.transferMode = null;
      }

      BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);

      this.setBackground(new Background(fill));
   }

   private void handleDragExited(DragEvent event) {
      System.out.println("Dragging exited: " + event);
      this.setBackground(originalBackground);
      this.transferMode = null;
   }

   private void handleDragOver(DragEvent event) {
      // System.out.println("Dragging over: " + event );
      if (this.transferMode != null) {
         event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
         event.consume();
      }
   }

   //~--- set methods ---------------------------------------------------------
   private void setDescriptionText(DescriptionVersion latestDescriptionVersion) {
      if (latestDescriptionVersion != null) {
         this.setText(latestDescriptionVersion.getText());
      }
   }

   private void setEmptyText() {
      setEmptyText(this);
   }

   private static void setEmptyText(Label label) {
      label.setText("empty");
   }

   public static void setFullySpecifiedText(ConceptLabel label) {
      ConceptChronology focusedConcept = label.manifold.getFocusedConceptChronology();
      if (focusedConcept != null) {
         focusedConcept
                 .getFullySpecifiedDescription(label.manifold)
                 .ifPresent(label::setDescriptionText)
                 .ifAbsent(label::setEmptyText);
      } else {
         setEmptyText(label);
      }
   }

   public static void setPreferredText(ConceptLabel label) {
      ConceptChronology focusedConcept = label.manifold.getFocusedConceptChronology();
      if (focusedConcept != null) {
         focusedConcept
                 .getPreferredDescription(label.manifold)
                 .ifPresent(label::setDescriptionText)
                 .ifAbsent(label::setEmptyText);
      } else {
         setEmptyText(label);
      }
   }
}
