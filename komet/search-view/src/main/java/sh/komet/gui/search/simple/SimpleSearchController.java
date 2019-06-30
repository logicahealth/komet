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

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.komet.gui.treeview.MultiParentTreeCell;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.table.DescriptionTableCell;

import java.util.*;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.GuiSearcher;

/**
 * @author kec
 */
public class SimpleSearchController implements ExplorationNode, GuiSearcher {
    private static final Logger              LOG               = LogManager.getLogger();
    private final SimpleStringProperty       titleProperty     =
        new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
    private final SimpleStringProperty       titleNodeProperty =
        new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
    private final SimpleStringProperty                     toolTipText       = new SimpleStringProperty("Simple Search Panel");
    private final SimpleObjectProperty<Node> iconProperty      =
        new SimpleObjectProperty<>(Iconography.SIMPLE_SEARCH.getIconographic());
    private final SimpleSearchService                         searchService        = new SimpleSearchService();
    private final SimpleListProperty<Integer> draggedTaxonomyConceptsForFilteringListProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private Manifold                                          manifold;

    @FXML
    AnchorPane                                                mainAnchorPane;
    @FXML
    TextField                                                 searchTextField;
    @FXML
    private TableView<ObservableDescriptionVersion>           resultTable;
    @FXML
    private TableColumn<ObservableDescriptionVersion, String> resultColumn;
    @FXML
    private ProgressBar                                       searchProgressBar;
    @FXML
    private FlowPane searchTagFlowPane;
    @FXML
    private Label searchTextFieldLabel;


   @Override
   public Node getMenuIcon() {
      return Iconography.SIMPLE_SEARCH.getIconographic();
   }

    @Override
    public void executeSearch(String searchString) {
        setSearchText(searchString);
        executeSearch();
    }


    @FXML
    public void executeSearch() {
        this.resultTable.getItems().clear();
        switch (this.searchService.getState()){
            case READY:
                this.searchService.start();
                break;
            case SCHEDULED:
                this.searchService.restart();
                break;
            case RUNNING:
                this.searchService.cancel();
                this.searchService.restart();
                break;
            case SUCCEEDED:
                this.searchService.restart();
                break;
         case CANCELLED:
            break;
         case FAILED:
            break;
         default:
            LOG.error("These cases were forgotten.... {}", this.searchService.getState());
            break;
        }
    }

    @FXML
    void initialize() {
        assert mainAnchorPane != null :
                "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert searchTextField != null :
                "fx:id=\"searchTextField\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert resultTable != null :
                "fx:id=\"resultTable\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert resultColumn != null :
                "fx:id=\"resultColumn\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert searchTagFlowPane != null :
                "fx:id=\"searchTagFlowPane\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert searchTextFieldLabel != null :
                "fx:id=\"searchTextFieldLabel\" was not injected: check your FXML file 'SimpleSearch.fxml'.";

        this.resultTable.setOnDragDetected(new DragDetectedCellEventHandler());
        this.resultTable.setOnDragDone(new DragDoneEventHandler());
        this.resultColumn.setCellValueFactory(new PropertyValueFactory("Result"));
        this.resultColumn.setCellValueFactory((TableColumn.CellDataFeatures<ObservableDescriptionVersion,
                String> param) -> param.getValue()
                .textProperty());
        this.resultColumn.setCellFactory((TableColumn<ObservableDescriptionVersion,
                String> stringText) -> new DescriptionTableCell());
        this.resultTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                this.manifold.setFocusedConceptChronology(
                        Get.conceptService().getConceptChronology(newSelection.getReferencedComponentNid()));
            }
        });

        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            searchTextField.setText("+tetra* +fallot");
        }
        FxGet.searchers().add(this);
    }

    private void initializeControls() {
        initializeSearchTextField();
        initializeProgressBar();
        initializeSearchService();
        initializeSearchTagFlowPlane();
    }

    private void initializeSearchTextField(){
        this.searchTextFieldLabel.setOnMouseEntered(mouseEnteredEvent
                -> this.searchTextFieldLabel.setCursor(Cursor.HAND));
        this.searchTextFieldLabel.setOnMouseClicked(mouseClickedEvent -> executeSearch());
        //Tool Tip for text field
        Tooltip searchTextFieldToolTip = new Tooltip();
        searchTextFieldToolTip.setText("Enter Keyword(s) to use for Simple Search.");
        this.searchTextField.setTooltip(searchTextFieldToolTip);
    }

    private void initializeSearchTagFlowPlane(){

        Label allLabel = new Label();
        allLabel.setGraphic(Iconography.SEARCH_FILTER.getIconographic());
        allLabel.setText("All");
        allLabel.setStyle("-fx-background-color: transparent;" +"-fx-background-insets: 0;" + "-fx-padding:5;"
        + "-fx-font-weight:bold;");

        //Tool Tip for All Filter
        Tooltip allFilterToolTip = new Tooltip();
        allFilterToolTip.setText("Allow all Simple Search results.");
        allLabel.setTooltip(allFilterToolTip);
        //Tool Tip for Drag and Drop Filters
        Tooltip dragAndDropToolTip = new Tooltip();
        dragAndDropToolTip.setText("Additional restriction on Simple Search results.");

        this.searchTagFlowPane.getChildren().add(allLabel);

        this.searchTagFlowPane.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        this.searchTagFlowPane.setOnDragDropped(event -> {
            Label labelFromDrop = new Label();

            ConceptChronology droppedChronology = ((MultiParentTreeCell)event.getGestureSource()).getTreeItem().getValue();
            labelFromDrop.setGraphic(Iconography.SEARCH_MINUS.getIconographic());
            labelFromDrop.setText(droppedChronology.getFullyQualifiedName());
            labelFromDrop.setStyle("-fx-background-color: transparent;" +"-fx-background-insets: 0;" + "-fx-padding:5;"
                    + "-fx-font-weight:bold;");
            labelFromDrop.setUserData(droppedChronology);

            labelFromDrop.setOnMouseClicked(labelClickedEvent -> {
                this.searchTagFlowPane.getChildren().removeAll(labelFromDrop);

                this.draggedTaxonomyConceptsForFilteringListProperty.get()
                        .remove((Integer)((ConceptChronology)labelFromDrop.getUserData()).getNid());
            });

            labelFromDrop.setTooltip(dragAndDropToolTip);
            labelFromDrop.setOnMouseEntered(mouseEnteredEvent
                    -> labelFromDrop.setCursor(Cursor.HAND));

            this.searchTagFlowPane.getChildren().add(labelFromDrop);
            this.draggedTaxonomyConceptsForFilteringListProperty.get().add(droppedChronology.getNid());

        });
    }

    private void initializeProgressBar(){
        this.searchService.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == -1) {
                this.searchProgressBar.setProgress(0);
            } else {
                this.searchProgressBar.setProgress(newValue.doubleValue());
            }
        });
    }

    private void initializeSearchService(){
        this.searchService.setManifold(this.manifold);
        this.searchService.luceneQueryProperty().bind(this.searchTextField.textProperty());
        this.searchService.parentNidsProperty().bind(this.draggedTaxonomyConceptsForFilteringListProperty);

        this.searchService.stateProperty().addListener((observable, oldValue, newValue) -> {

            switch (newValue) {
                case SUCCEEDED:
                    ObservableList<ObservableDescriptionVersion> tableItems = this.resultTable.getItems();
                    ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);

                    if(this.searchService.getValue().size() == 0) {
                        this.resultTable.setPlaceholder(new Label("No Results Found..."));
                        break;
                    }

                    this.searchService.getValue().stream().forEach(nid -> {
                        LatestVersion<ObservableDescriptionVersion> latestDescription =
                                (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(nid);

                        if (latestDescription.isPresent()) {
                           tableItems.add(latestDescription.get());
                        } else {
                           LOG.error("No latest description for: " + nid);
                        }
                    });
                    break;
                case FAILED:
                    this.resultTable.setPlaceholder(new Label("Simple Search Failed..."));
                    break;
            }

        });
    }


    public void setSearchText(String searchText) {
        searchTextField.setText(searchText);
    }
    
    
    @Override
    public Manifold getManifold() {
        return manifold;
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        initializeControls();
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
    public Optional<Node> getTitleNode() {
        Label titleLabel = new Label();

        titleLabel.graphicProperty().bind(iconProperty);
        titleLabel.textProperty().bind(titleNodeProperty);
        titleProperty.set("");

        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipText;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}