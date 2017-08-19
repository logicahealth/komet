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
package sh.komet.gui.provider.concept.detail.panel;

//~--- JDK imports ------------------------------------------------------------
import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.HPos;

import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.control.ComponentPanel;
import sh.komet.gui.control.ConceptLabelToolbar;
import sh.komet.gui.control.ExpandControl;
import sh.komet.gui.control.OnOffToggleSwitch;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptDetailPanelNode
        implements DetailNode {

   private final BorderPane conceptDetailPane = new BorderPane();
   private final SimpleStringProperty titleProperty = new SimpleStringProperty("detail graph");
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("detail graph");
   private final VBox componentPanelBox = new VBox(8);
   private final GridPane versionBrancheGrid = new GridPane();
   private final GridPane toolGrid = new GridPane();
   private final Manifold conceptDetailManifold;
   private final ExpandControl expandControl = new ExpandControl();
   private final OnOffToggleSwitch historySwitch = new OnOffToggleSwitch();
   private final Label expandControlLabel = new Label("Expand All", expandControl);

   {
      expandControlLabel.setGraphicTextGap(0);
   }

   //~--- constructors --------------------------------------------------------
   public ConceptDetailPanelNode(Manifold conceptDetailManifold, Consumer<Node> nodeConsumer) {
      this.conceptDetailManifold = conceptDetailManifold;
      this.conceptDetailManifold.getStampCoordinate()
              .allowedStatesProperty()
              .add(State.INACTIVE);
      conceptDetailManifold.focusedConceptChronologyProperty()
              .addListener(this::setConcept);
      conceptDetailPane.setTop(ConceptLabelToolbar.make(conceptDetailManifold));
      conceptDetailPane.getStyleClass()
              .add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
      conceptDetailPane.setCenter(componentPanelBox);
      versionBrancheGrid.add(Iconography.CIRCLE_A.getIconographic(), 0, 0);
      conceptDetailPane.setRight(versionBrancheGrid);
      componentPanelBox.getStyleClass()
              .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
      componentPanelBox.setFillWidth(true);
      setupToolGrid();
      nodeConsumer.accept(conceptDetailPane);

      historySwitch.selectedProperty().addListener(this::setShowHistory);

      // TODO why does scroll pane squash contents to minimum width?
      // nodeConsumer.accept(new ScrollPane(conceptDetailPane));
   }

   //~--- methods -------------------------------------------------------------
   public void addChronology(ObservableChronology observableConceptChronology) {
      StampCoordinate stampCoordinate
              = this.conceptDetailManifold.getStampCoordinate().makeCoordinateAnalog(State.ANY_STATE_SET);
      if (!historySwitch.isSelected()) {
         stampCoordinate = this.conceptDetailManifold.getStampCoordinate().makeCoordinateAnalog(State.ACTIVE);
      }
      CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
              = observableConceptChronology.getCategorizedVersions(stampCoordinate);
      if (oscCategorizedVersions.getLatestVersion().isPresent()) {
         addComponent(oscCategorizedVersions);
      }

   }

   private void addComponent(CategorizedVersions<ObservableCategorizedVersion> categorizedVersions) {
      ComponentPanel panel = new ComponentPanel(conceptDetailManifold, categorizedVersions);

      VBox.setMargin(panel, new Insets(1, 5, 1, 5));
      componentPanelBox.getChildren()
              .add(panel);
   }

   private void clearComponents() {
      componentPanelBox.getChildren()
              .clear();

   }

   //~--- set methods ---------------------------------------------------------
   private void setShowHistory(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      setConcept(conceptDetailManifold.focusedConceptChronologyProperty(),
              null,
              conceptDetailManifold.focusedConceptChronologyProperty().get());
   }

   private void setConcept(ObservableValue<? extends ConceptChronology> observable,
           ConceptChronology oldValue,
           ConceptChronology newValue) {
      clearComponents();
      if (newValue != null) {
         titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
         toolTipProperty.set(
                 "concept details for: " + this.conceptDetailManifold.getFullySpecifiedDescriptionText(newValue));

         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         newValue.getConceptSequence());

         // add back toolbar...
         componentPanelBox.getChildren().add(toolGrid);
         addChronology(observableConceptChronology);

         for (ObservableSememeChronology osc : observableConceptChronology.getObservableSememeList()) {
            addChronology(osc);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public ReadOnlyProperty<String> getTitle() {
      return this.titleProperty;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return this.toolTipProperty;
   }

   private void setupToolGrid() {
      GridPane.setConstraints(expandControlLabel, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
      this.toolGrid.getChildren().add(expandControlLabel);
      Pane spacer = new Pane();
      GridPane.setConstraints(spacer, 1, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER, new Insets(2));
      this.toolGrid.getChildren().add(spacer);
      Label historySwitchWithLabel = new Label("History", historySwitch);
      historySwitchWithLabel.setContentDisplay(ContentDisplay.RIGHT);

      GridPane.setConstraints(historySwitchWithLabel, 2, 0, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.NEVER, Priority.NEVER, new Insets(2));
      this.toolGrid.getChildren().add(historySwitchWithLabel);
      componentPanelBox.getChildren().add(toolGrid);
   }
}
