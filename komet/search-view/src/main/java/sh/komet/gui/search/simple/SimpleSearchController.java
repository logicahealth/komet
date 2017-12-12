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



package sh.komet.gui.search.simple;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.table.DescriptionTableCell;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class SimpleSearchController
         implements ExplorationNode {
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private final SimpleStringProperty titleProperty     = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   private final SimpleStringProperty titleNodeProperty = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   SimpleStringProperty               toolTipText       = new SimpleStringProperty("Simple Search Panel");
   private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
                                                               Iconography.SIMPLE_SEARCH.getIconographic());
   private NidSet                                            results                = new NidSet();
   private final DescriptionLuceneMatch                      descriptionLuceneMatch = new DescriptionLuceneMatch();
   private final ObservableList<CustomCheckListItem>         kindOfObservableList = FXCollections.observableArrayList();
   private Manifold                                          manifold;
   @FXML
   AnchorPane                                                mainAnchorPane;
   @FXML
   TextField                                                 searchParameter;
   @FXML
   private TableView<ObservableDescriptionVersion>           resultTable;
   @FXML
   private TableColumn<ObservableDescriptionVersion, String> resultColumn;
   @FXML
   private CheckListView<CustomCheckListItem>                kindOfCheckListView;
   @FXML
   private ChoiceBox<SearchComponentStatus>                  statusChoiceBox;
   @FXML
   private ProgressBar searchProgressBar;
   @FXML
   private Button cancelSearchButton;

   //~--- methods -------------------------------------------------------------

   @FXML
   public void executeSearch(ActionEvent actionEvent) {
      if (!this.searchParameter.getText()
                               .isEmpty()) {
         this.descriptionLuceneMatch.setManifoldCoordinate(this.manifold);
         this.descriptionLuceneMatch.setParameterString(this.searchParameter.getText());
         this.results = descriptionLuceneMatch.computePossibleComponents(null);
         filterSearchResults();
      }
   }

   @FXML
   public void cancelSearch(ActionEvent actionEvent){
      
   }

   @FXML
   void initialize() {
      assert mainAnchorPane != null:
             "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert searchParameter != null:
             "fx:id=\"searchParameter\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert resultTable != null: "fx:id=\"resultTable\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert resultColumn != null: "fx:id=\"resultColumn\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert kindOfCheckListView != null:
             "fx:id=\"kindOfCheckListView\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert statusChoiceBox != null:
             "fx:id=\"statusComboBox\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert searchProgressBar != null: "fx:id=\"searchProgressBar\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert cancelSearchButton != null: "fx:id=\"cancelSearchButton\" was not injected: check your FXML file 'SimpleSearch.fxml'.";

      this.resultTable.setOnDragDetected(new DragDetectedCellEventHandler());
      this.resultTable.setOnDragDone(new DragDoneEventHandler());
      this.resultColumn.setCellValueFactory(new PropertyValueFactory("Result"));
      this.resultColumn.setCellValueFactory(
          (TableColumn.CellDataFeatures<ObservableDescriptionVersion, String> param) -> param.getValue()
                .textProperty());
      this.resultColumn.setCellFactory(
          (TableColumn<ObservableDescriptionVersion, String> stringText) -> new DescriptionTableCell());
      this.resultTable.getSelectionModel()
                      .selectedItemProperty()
                      .addListener(
                          (obs, oldSelection, newSelection) -> {
                             if (newSelection != null) {
                                this.manifold.setFocusedConceptChronology(
                                    Get.conceptService()
                                       .getConceptChronology(newSelection.getReferencedComponentNid()));
                             }
                          });
   }

   private void displaySearchResults() {
      ObservableList<ObservableDescriptionVersion> tableItems = this.resultTable.getItems();
      ObservableSnapshotService                    snapshot   = Get.observableSnapshotService(this.manifold);

      tableItems.clear();
      this.results.stream()
                  .forEach(
                      value -> {
                         LatestVersion<ObservableDescriptionVersion> latestDescription =
                            (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(
                                value);

                         if (latestDescription.isPresent()) {
                            tableItems.add(latestDescription.get());
                         } else {
                            LOG.error("No latest description for: " + value);
                         }
                      });
   }

   private void filterSearchResults() {
      NidSet                filteredValues        = new NidSet();
      SearchComponentStatus searchComponentStatus = this.statusChoiceBox.getValue();

      // Get a combined set of allowed concepts...
      IndexedCheckModel<CustomCheckListItem> indexedCheckModel = this.kindOfCheckListView.getCheckModel();
      TaxonomySnapshotService                taxonomySnapshot  = Get.taxonomyService()
                                                                    .getSnapshot(this.manifold);
      NidSet                                 allowedParents    = new NidSet();

      for (int checkedIndex: indexedCheckModel.getCheckedIndices()) {
         allowedParents.add(indexedCheckModel.getItem(checkedIndex)
               .getNID());
      }

      // if the result set is small, it will be faster to use the isKindOf method call, rather than pre-computing
      // all allowed concepts as the kindOfSequenceSet would. You can play with changing this number to compare
      // performance choices.
      NidSet allowedConceptNids = null;

      if (this.results.size() > 500) {
         allowedConceptNids = new NidSet();

         for (int allowedParentNid: allowedParents.asArray()) {
            NidSet kindOfSet = taxonomySnapshot.getKindOfConceptNidSet(allowedParentNid);

            allowedConceptNids.addAll(kindOfSet);
         }
      }

      // I assume this is a description nid...
      for (int descriptionNid: this.results.asArray()) {
         SemanticChronology descriptionChronology      = Get.assemblageService()
                                                            .getSemanticChronology(descriptionNid);
         LatestVersion<DescriptionVersion> description = descriptionChronology.getLatestVersion(manifold);

         // TODO, this step probably filters out inactive descriptions, which is not always what we do.
         // The stamp coordinate would have to have both active and inactive status values, but I don't want to mess with the
         // manifold here, save that for the FLOWR query to get right.
         if (!description.isPresent()) {
            // move on to the next one...
            continue;
         }

         DescriptionVersion descriptionVersion = description.get();
         int                conceptNid         = descriptionVersion.getReferencedComponentNid();

         if (searchComponentStatus != SearchComponentStatus.DONT_CARE) {
            boolean active = Get.conceptActiveService()
                                .isConceptActive(conceptNid, manifold);

            if (!searchComponentStatus.filter(active)) {
               // move on to the next one...
               continue;
            }
         }

         if (!allowedParents.isEmpty()) {
            if (allowedConceptNids != null) {
               if (!allowedConceptNids.contains(conceptNid)) {
                  // move on to the next one...
                  continue;
               }
            } else {
               boolean allowedParentFound = false;

               for (int allowedParentNid: allowedParents.asArray()) {
                  if (taxonomySnapshot.isKindOf(conceptNid, allowedParentNid)) {
                     allowedParentFound = true;

                     // break the allowedParents loop
                     break;
                  }
               }

               if (!allowedParentFound) {
                  // move on to the next one.
                  continue;
               }
            }
         }

         // if you get this far, it is an allowed nid.
         filteredValues.add(descriptionNid);
      }

      this.results.clear();
      this.results.addAll(filteredValues);
      displaySearchResults();
   }

   private void initControls() {
      // init Status Choice Box
      ObservableList<SearchComponentStatus> statusChoiceBoxItems = FXCollections.observableArrayList();

      for (SearchComponentStatus status: SearchComponentStatus.values()) {
         statusChoiceBoxItems.add(status);
      }

      this.statusChoiceBox.setItems(statusChoiceBoxItems);
      this.statusChoiceBox.getSelectionModel()
                          .select(SearchComponentStatus.ACTIVE);

      // init CheckListView
      CustomCheckListItem       defaultCheckedItem;
      TaxonomySnapshotService   taxonomySnapshot = Get.taxonomyService()
                                                      .getSnapshot(this.manifold);
      List<CustomCheckListItem> list             = new ArrayList<>();

      list.add(new CustomCheckListItem(Get.conceptSpecification(MetaData.METADATA____SOLOR.getNid())));
      Arrays.stream(taxonomySnapshot.getTaxonomyChildConceptNids(MetaData.HEALTH_CONCEPT____SOLOR.getNid()))
            .forEach(value -> list.add(new CustomCheckListItem(Get.conceptSpecification(value))));
      Collections.sort(list);
      list.stream()
          .forEach(customCheckListItem -> this.kindOfObservableList.add(customCheckListItem));
      this.kindOfCheckListView.setItems(this.kindOfObservableList);
      list.stream()
          .forEach(
              item -> {
                 if (item.getNID() == MetaData.PHENOMENON____SOLOR.getNid()) {
                    this.kindOfCheckListView.getCheckModel()
                                            .check(item);
                 }
              });
   }

   private void streamFilterSearchResults() {
      NidSet                filteredValues        = new NidSet();
      SearchComponentStatus searchComponentStatus = this.statusChoiceBox.getValue();

      // Get a combined set of allowed concepts...
      IndexedCheckModel<CustomCheckListItem> indexedCheckModel = this.kindOfCheckListView.getCheckModel();
      TaxonomySnapshotService                taxonomySnapshot  = Get.taxonomyService()
                                                                    .getSnapshot(this.manifold);
      NidSet                                 allowedParents    = new NidSet();

      for (int checkedIndex: indexedCheckModel.getCheckedIndices()) {
         allowedParents.add(indexedCheckModel.getItem(checkedIndex)
               .getNID());
      }

      // if the result set is small, it will be faster to use the isKindOf method call, rather than pre-computing
      // all allowed concepts as the kindOfSequenceSet would. You can play with changing this number to compare
      // performance choices.
      NidSet allowedConceptNids = new NidSet();

      if (this.results.size() > 500) {
         for (int allowedParentNid: allowedParents.asArray()) {
            NidSet kindOfSet = taxonomySnapshot.getKindOfConceptNidSet(allowedParentNid);

            allowedConceptNids.addAll(kindOfSet);
         }
      }

      this.results.stream()
                  .mapToObj(
                      (int descriptionNid) -> {
                         SemanticChronology descriptionChronology = Get.assemblageService()
                                                                       .getSemanticChronology(descriptionNid);

                         return descriptionChronology.getLatestVersion(manifold);
                      })
                  .filter((LatestVersion<? super DescriptionVersion> description) -> description.isPresent())
                  .map((LatestVersion<? super DescriptionVersion> optional) -> (DescriptionVersion) optional.get())
                  .filter(
                      (descriptionVersion) -> {
                         int conceptNid = descriptionVersion.getReferencedComponentNid();

                         if (searchComponentStatus != SearchComponentStatus.DONT_CARE) {
                            boolean active = Get.conceptActiveService()
                                                .isConceptActive(conceptNid, manifold);

                            if (!searchComponentStatus.filter(active)) {
                               return false;
                            }
                         }

                         return true;
                      })
                  .filter(
                      (descriptionVersion) -> {
                         if (!allowedParents.isEmpty()) {
                            int conceptNid = descriptionVersion.getReferencedComponentNid();

                            if (!allowedConceptNids.isEmpty()) {
                               if (!allowedConceptNids.contains(conceptNid)) {
                                  return false;
                               }
                            } else {
                               boolean allowedParentFound = false;

                               for (int allowedParentNid: allowedParents.asArray()) {
                                  if (taxonomySnapshot.isKindOf(conceptNid, allowedParentNid)) {
                                     allowedParentFound = true;

                                     // break the allowedParents loop
                                     break;
                                  }
                               }

                               if (!allowedParentFound) {
                                  // move on to the next one.
                                  return false;
                               }
                            }
                         }

                         return true;
                      })
                  .forEach((descriptionVersion) -> filteredValues.add(descriptionVersion.getNid()));
      this.results.clear();
      this.results.addAll(filteredValues);
      displaySearchResults();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Manifold getManifold() {
      return manifold;
   }

   //~--- set methods ---------------------------------------------------------

   public void setManifold(Manifold manifold) {
      this.manifold = manifold;
      initControls();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Node getNode() {
      return mainAnchorPane;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public Optional<Node> getTitleNode() {
      Label titleLabel = new Label();

      titleLabel.graphicProperty()
                .bind(iconProperty);
      titleLabel.textProperty()
                .bind(titleNodeProperty);
      titleProperty.set("");
      return Optional.of(titleLabel);
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipText;
   }

   //~--- inner classes -------------------------------------------------------

   private class CustomCheckListItem
            implements Comparable<CustomCheckListItem> {
      private ConceptSpecification conceptSpecification;

      //~--- constructors -----------------------------------------------------

      public CustomCheckListItem(ConceptSpecification conceptSpecification) {
         this.conceptSpecification = conceptSpecification;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(CustomCheckListItem o) {
         return this.conceptSpecification.getFullySpecifiedConceptDescriptionText()
                                         .compareTo(
                                               o.getConceptSpecification()
                                                     .getFullySpecifiedConceptDescriptionText());
      }

      @Override
      public String toString() {
         return this.conceptSpecification.getFullySpecifiedConceptDescriptionText();
      }

      //~--- get methods ------------------------------------------------------

      public ConceptSpecification getConceptSpecification() {
         return conceptSpecification;
      }

      public int getNID() {
         return this.conceptSpecification.getNid();
      }
   }
}

