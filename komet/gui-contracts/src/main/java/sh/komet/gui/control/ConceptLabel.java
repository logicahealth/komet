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

import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Label;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

import sh.komet.gui.contract.Manifold;
import sh.komet.gui.drag.drop.DragImageMaker;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptLabel
        extends Label {
   SimpleObjectProperty<ConceptChronology> conceptProperty;
   SimpleObjectProperty<Manifold>          manifoldProperty;
   Consumer<ConceptLabel>                  descriptionTextUpdater;
   Background originalBackground;

   //~--- constructors --------------------------------------------------------

   public ConceptLabel(SimpleObjectProperty<ConceptChronology> conceptProperty,
                       SimpleObjectProperty<Manifold> manifoldProperty,
                       Consumer<ConceptLabel> descriptionTextUpdater) {
      this.conceptProperty        = conceptProperty;
      this.manifoldProperty       = manifoldProperty;
      this.descriptionTextUpdater = descriptionTextUpdater;
      this.conceptProperty.addListener(
          (ObservableValue<? extends ConceptChronology> observable,
           ConceptChronology oldValue,
           ConceptChronology newValue) -> {
             this.descriptionTextUpdater.accept(this);
          });
      this.setOnDragOver(this::handleDragOver);
      this.setOnDragEntered(this::handleDragEntered);
      this.setOnDragDetected(this::handleDragDetected);
      this.setOnDragExited(this::handleDragExited);
      this.setOnDragDropped(this::handleDragDropped);
      this.setOnDragDone(this::handleDragDone);
      //this.setDisabled(false);
      //this.setDisable(false);
   }

   //~--- methods -------------------------------------------------------------

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

   private void handleDragEntered(DragEvent event) {
      System.out.println("Dragging entered: " + event );
      BackgroundFill fill = new BackgroundFill(Color.AQUA, CornerRadii.EMPTY, Insets.EMPTY);
      this.originalBackground = this.getBackground();
      this.setBackground(new Background(fill));
   }

   private void handleDragDropped(DragEvent event) {
      System.out.println("Dragging dropped: " + event );
      this.setBackground(originalBackground);
   }

   private void handleDragExited(DragEvent event) {
      System.out.println("Dragging exited: " + event );
      this.setBackground(originalBackground);
   }

   private void handleDragDone(DragEvent event) {
      System.out.println("Dragging done: " + event );
      this.setBackground(originalBackground);
   }

   private void handleDragOver(DragEvent event) {
      System.out.println("Dragging over: " + event );
      BackgroundFill fill = new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY);
      this.setBackground(new Background(fill));
   }

   //~--- set methods ---------------------------------------------------------

   private void setDescriptionText(DescriptionVersion latestDescriptionVersion) {
      if (latestDescriptionVersion != null) {
         this.setText(latestDescriptionVersion.getText());
      }
   }

   private void setEmptyText() {
      this.setText("empty");
   }

   public static void setFullySpecifiedText(ConceptLabel label) {
      label.conceptProperty.get()
                           .getFullySpecifiedDescription(label.manifoldProperty.get())
                           .ifPresent(label::setDescriptionText)
                           .ifAbsent(label::setEmptyText);
   }

   public static void setPreferredText(ConceptLabel label) {
      label.conceptProperty.get()
                           .getPreferredDescription(label.manifoldProperty.get())
                           .ifPresent(label::setDescriptionText)
                           .ifAbsent(label::setEmptyText);
   }
}

