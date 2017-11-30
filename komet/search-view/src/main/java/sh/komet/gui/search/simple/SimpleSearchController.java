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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.table.DescriptionTableCell;

import java.util.Optional;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class SimpleSearchController
         implements ExplorationNode {

   private static final Logger LOG = LogManager.getLogger();
   private final SimpleStringProperty titleProperty = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   private final SimpleStringProperty titleNodeProperty = new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
   SimpleStringProperty               toolTipText   = new SimpleStringProperty("Simple Search Panel");
   private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
                                                               Iconography.SIMPLE_SEARCH.getIconographic());
   private Manifold manifold;
   private NidSet results = new NidSet();
   private final DescriptionLuceneMatch descriptionLuceneMatch = new DescriptionLuceneMatch();

   @FXML
   AnchorPane mainAnchorPane;
   @FXML
   TextField searchParameter;
   @FXML
   private TableView<ObservableDescriptionVersion> resultTable;
   @FXML
   private TableColumn<ObservableDescriptionVersion, String> resultColumn;


   @FXML
   void initialize() {
      assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert searchParameter != null : "fx:id=\"searchParameter\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
      assert resultColumn != null : "fx:id=\"resultColumn\" was not injected: check your FXML file 'SimpleSearch.fxml'.";

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

   @Override
   public Optional<Node> getTitleNode() {
      Label titleLabel = new Label();
      titleLabel.graphicProperty().bind(iconProperty);
      titleLabel.textProperty().bind(titleNodeProperty);
      titleProperty.set("");
      return Optional.of(titleLabel);
   }
 
   @Override
   public Manifold getManifold() {
      return manifold;
   }

   @Override
   public Node getNode() {
      return mainAnchorPane;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipText;
   }

   public void setManifold(Manifold manifold) {
      this.manifold = manifold;
   }

   @FXML
   public void executeSearch(ActionEvent actionEvent){

      this.descriptionLuceneMatch.setManifoldCoordinate(this.manifold);
      this.descriptionLuceneMatch.setParameterString(this.searchParameter.getText());
      this.results = descriptionLuceneMatch.computePossibleComponents(null);

      filterSearchResults();
   }

   private void filterSearchResults(){

      displaySearchResults();
   }

   private void displaySearchResults(){
      ObservableList<ObservableDescriptionVersion> tableItems = this.resultTable.getItems();
      ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);

      tableItems.clear();
      this.results.stream().forEach(value -> {
         LatestVersion<ObservableDescriptionVersion> latestDescription
                 = (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(
                 value);

         if (latestDescription.isPresent()) {
            tableItems.add(latestDescription.get());
         } else {
            LOG.error("No latest description for: " + value);
         }

      });
   }
}

