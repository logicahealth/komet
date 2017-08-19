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
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;

import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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

   private static final int TRANSITION_OFF_TIME = 250;
   private static final int TRANSITION_ON_TIME = 750;

   
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
   private final ScrollPane scrollPane; 

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

      historySwitch.selectedProperty().addListener(this::setShowHistory);
      this.scrollPane = new ScrollPane(conceptDetailPane);
      this.scrollPane.setFitToWidth(true);
      this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      nodeConsumer.accept(this.scrollPane);
   }

   //~--- methods -------------------------------------------------------------
   public void addChronology(ObservableChronology observableChronology, ParallelTransition parallelTransition) {
      StampCoordinate stampCoordinate
              = this.conceptDetailManifold.getStampCoordinate().makeCoordinateAnalog(State.ANY_STATE_SET);
      if (!historySwitch.isSelected()) {
         stampCoordinate = this.conceptDetailManifold.getStampCoordinate().makeCoordinateAnalog(State.ACTIVE);
      }
      CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
              = observableChronology.getCategorizedVersions(stampCoordinate);
      if (oscCategorizedVersions.getLatestVersion().isPresent()) {
         parallelTransition.getChildren().add(addComponent(oscCategorizedVersions));
      }

   }

   private Animation addComponent(CategorizedVersions<ObservableCategorizedVersion> categorizedVersions) {
      ComponentPanel panel = new ComponentPanel(conceptDetailManifold, categorizedVersions);
      panel.setOpacity(0);
      VBox.setMargin(panel, new Insets(1, 5, 1, 5));
      componentPanelBox.getChildren()
              .add(panel);
      FadeTransition ft =  new FadeTransition(Duration.millis(TRANSITION_ON_TIME), panel);
      ft.setFromValue(0);
      ft.setToValue(1);
      return ft;
   }

   private void clearComponents() {
      
      final ParallelTransition parallelTransition = new  ParallelTransition();
      
      componentPanelBox.getChildren().forEach((child) -> {
         if (toolGrid != child) {
         FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_OFF_TIME), child);
         ft.setFromValue(1.0);
         ft.setToValue(0.0);   
         parallelTransition.getChildren().add(ft);
         }
      });
      
      parallelTransition.setOnFinished(this::clearAnimationComplete);
      parallelTransition.play();
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
   }
   private void clearAnimationComplete(ActionEvent completeEvent) {
      
      componentPanelBox.getChildren()
              .clear();
      componentPanelBox.getChildren().add(toolGrid);
      ConceptChronology newValue = this.conceptDetailManifold.getFocusedConceptChronology();
      if (newValue != null) {
         titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
         toolTipProperty.set(
                 "concept details for: " + this.conceptDetailManifold.getFullySpecifiedDescriptionText(newValue));

         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         newValue.getConceptSequence());

         final ParallelTransition parallelTransition = new  ParallelTransition();

         addChronology(observableConceptChronology, parallelTransition);

         for (ObservableSememeChronology osc : observableConceptChronology.getObservableSememeList()) {
            addChronology(osc, parallelTransition);
         }
         parallelTransition.play();
      }
   }

   private void setupFadeOnTransition(Node nodeToTransition, final ParallelTransition parallelTransition) {
      FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), nodeToTransition);
      ft.setFromValue(0);
      ft.setToValue(1);
      parallelTransition.getChildren().add(ft);
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
