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

//~--- non-JDK imports --------------------------------------------------------

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import sh.isaac.api.State;

import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.state.ExpandAction;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public abstract class BadgedVersionPanel
        extends Pane {
   private final ObservableCategorizedVersion categorizedVersion;
   private final Manifold                     manifold;
   protected final int badgeWidth = 25;
   ArrayList<Node> badges = new ArrayList<>();
   int columns = 10;
   Text componentText = new Text();
   Text componentType = new Text();
   Node editControl = Iconography.EDIT_PENCIL.getIconographic();
   ExpandControl expandControl = new ExpandControl();
   GridPane gridpane = new GridPane();
   protected final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty isContradiction = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty isInactive = new SimpleBooleanProperty(false);
   protected final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);
   protected final int rowHeight = 25;
   StampControl stampControl = new StampControl("", Iconography.CIRCLE_A.getIconographic());
   int wrappingWidth = 300;

   //~--- initializers --------------------------------------------------------
   {
      isDescription.addListener(this::pseudoStateChanged);
      isInactive.addListener(this::pseudoStateChanged);
      isConcept.addListener(this::pseudoStateChanged);
      isLogicalDefinition.addListener(this::pseudoStateChanged);
      isContradiction.addListener(this::pseudoStateChanged);
   }

   //~--- constructors --------------------------------------------------------

   public BadgedVersionPanel(Manifold manifold, ObservableCategorizedVersion categorizedVersion) {
      this.manifold           = manifold;
      this.categorizedVersion = categorizedVersion;
      isInactive.set(getCategorizedVersion().getState() == State.INACTIVE);
      expandControl.expandActionProperty().addListener(this::expand);
      this.getChildren()
              .add(gridpane);

      componentType.getStyleClass()
              .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
      componentText.getStyleClass()
              .add(StyleClasses.COMPONENT_TEXT.toString());
      componentText.setWrappingWidth(wrappingWidth);
      componentText.boundsInLocalProperty()
              .addListener(this::textLayoutChanged);
      isInactive.set(this.categorizedVersion.getState() != State.ACTIVE);

      this.stampControl.setStampedVersion(getCategorizedVersion(), manifold);
      this.widthProperty()
              .addListener(this::widthChanged);
   }

   @Override
   public final ObservableList<Node> getChildren() {
      return super.getChildren(); 
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the categorizedVersion
    */
   public final ObservableCategorizedVersion getCategorizedVersion() {
      return categorizedVersion;
   }

   /**
    * @return the manifold
    */
   public Manifold getManifold() {
      return manifold;
   }

   //~--- methods -------------------------------------------------------------
   protected abstract void expand(ObservableValue<? extends ExpandAction> observable, ExpandAction oldValue, ExpandAction newValue);

   protected void pseudoStateChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      if (observable == isDescription) {
         this.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, newValue);
      } else if (observable == isInactive) {
         this.pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, newValue);
      } else if (observable == isConcept) {
         this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
      } else if (observable == isLogicalDefinition) {
         this.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
      } else if (observable == isContradiction) {
         this.pseudoClassStateChanged(PseudoClasses.CONTRADICTED_PSEUDO_CLASS, newValue);
      }
   }

   protected void resetComponentTextHeight(int newRows) {
      gridpane.getChildren().remove(expandControl);
      GridPane.setConstraints(expandControl, 0, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren().add(expandControl); // next is 1
      gridpane.getChildren().remove(componentType);
      GridPane.setConstraints(componentType, 1, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren().add(componentType); // next is 3
      gridpane.getChildren().remove(stampControl);
      GridPane.setConstraints(stampControl, columns, 1, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren().add(stampControl);
      gridpane.getChildren().remove(editControl);
      GridPane.setConstraints(editControl, columns, 0, 1, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.NEVER);
      gridpane.getChildren().add(editControl);
      componentText.getLayoutBounds().getHeight();
      gridpane.getChildren().remove(componentText);
      GridPane.setConstraints(componentText, 3, 0, columns - 4, (int) newRows, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);
      gridpane.getChildren().add(componentText);
      gridpane.getRowConstraints().clear();
      gridpane.getRowConstraints().add(new RowConstraints(rowHeight)); // add row zero...
      for (int i = 0; i < badges.size();) {
         for (int row = 1; i < badges.size(); row++) {
            gridpane.getRowConstraints().add(new RowConstraints(rowHeight));
            if (row + 1 <= newRows) {
               for (int column = 1; (column < 3) && (i < badges.size()); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            } else {
               for (int column = 1; (column < columns) && (i < badges.size()); column++) {
                  setupBadge(badges.get(i++), column, row);
               }
            }
         }
      }
   }

   protected void setupBadge(Node badge, int column, int row) {
      gridpane.getChildren().remove(badge);
      GridPane.setConstraints(badge, column, row, 1, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
      gridpane.getChildren().add(badge);
      if (!badge.getStyleClass().contains(StyleClasses.COMPONENT_BADGE.toString())) {
         badge.getStyleClass().add(StyleClasses.COMPONENT_BADGE.toString());
      }
   }

   public void setupColumns() {
      if (this.getParent() != null) {
         int width = (int) getParent().getBoundsInLocal().getWidth();
         this.columns = (width / badgeWidth) - 1;
         gridpane.getColumnConstraints().clear();
         for (int i = 0; i < this.columns; i++) {
            gridpane.getColumnConstraints().add(new ColumnConstraints(badgeWidth));
         }
      }
   }

   protected void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
      double doubleRows = newBounds.getHeight() / rowHeight;
      int rows = (int) doubleRows + 1;
      resetComponentTextHeight(rows);
      setupColumns();
   }

   protected void widthChanged(ObservableValue<? extends Number> observableWidth, Number oldWidth, Number newWidth) {
      int newTextWidth = (int) (newWidth.intValue() - (4 * badgeWidth));
      setupColumns();
      if (newTextWidth > 0) {
         int oldTextWidth = (int) componentText.getWrappingWidth();
         if (newTextWidth != oldTextWidth) {
            this.wrappingWidth = newTextWidth;
            Platform.runLater(() -> {
               componentText.setWrappingWidth(wrappingWidth);
            });
         }
      }
   }
}

