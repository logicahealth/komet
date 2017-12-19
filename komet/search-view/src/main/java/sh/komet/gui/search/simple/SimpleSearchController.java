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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.CheckListView;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.table.DescriptionTableCell;

import java.util.*;

/**
 * @author kec
 */
public class SimpleSearchController implements ExplorationNode {
    private static final Logger              LOG               = LogManager.getLogger();
    private final SimpleStringProperty       titleProperty     =
        new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
    private final SimpleStringProperty       titleNodeProperty =
        new SimpleStringProperty(SimpleSearchViewFactory.MENU_TEXT);
    SimpleStringProperty                     toolTipText       = new SimpleStringProperty("Simple Search Panel");
    private final SimpleObjectProperty<Node> iconProperty      =
        new SimpleObjectProperty<>(Iconography.SIMPLE_SEARCH.getIconographic());
    private final ObservableList<CustomCheckListItem>         kindOfObservableList =
        FXCollections.observableArrayList();
    private final SimpleSearchService                         searchService        = new SimpleSearchService();
    private Manifold                                          manifold;
    private static final PseudoClass CSS_FAIL = PseudoClass.getPseudoClass("fail");
    private static final PseudoClass CSS_SUCESS = PseudoClass.getPseudoClass("success");
    private static final PseudoClass CSS_NORESULT = PseudoClass.getPseudoClass("noResults");

    @FXML
    AnchorPane                                                mainAnchorPane;
    @FXML
    TextField                                                 searchTextField;
    @FXML
    private TableView<ObservableDescriptionVersion>           resultTable;
    @FXML
    private TableColumn<ObservableDescriptionVersion, String> resultColumn;
    @FXML
    private CheckListView<CustomCheckListItem>                kindOfCheckListView;
    @FXML
    private ChoiceBox<SearchComponentStatus>                  statusChoiceBox;
    @FXML
    private ProgressBar                                       searchProgressBar;
    @FXML
    private Button searchRefreshButton;

    @FXML
    public void searchRefresh() {
        if(this.searchService.isRunning())
            this.searchService.cancel();
        else
            this.searchService.reset();

        this.searchTextField.clear();
        this.resultTable.getItems().clear();
        this.resultTable.setPlaceholder(new Label("No content in table"));
        this.searchTextField.pseudoClassStateChanged(CSS_FAIL, false);
        this.searchTextField.pseudoClassStateChanged(CSS_SUCESS, false);
        this.searchTextField.pseudoClassStateChanged(CSS_NORESULT, false);
        this.searchTextField.setDisable(false);
    }

    @FXML
    public void executeSearch() {
        this.resultTable.getItems().clear();
        if (this.searchService.getState() == Worker.State.READY) {
           this.searchService.start();
        } else {
           this.searchService.restart();
           this.searchService.start();
        }
        this.searchTextField.setDisable(true);
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
        assert kindOfCheckListView != null :
                "fx:id=\"kindOfCheckListView\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert statusChoiceBox != null :
                "fx:id=\"statusComboBox\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert searchProgressBar != null :
                "fx:id=\"searchProgressBar\" was not injected: check your FXML file 'SimpleSearch.fxml'.";
        assert searchRefreshButton != null :
                "fx:id=\"searchRefreshButton\" was not injected: check your FXML file 'SimpleSearch.fxml'.";

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
    }

    private void initializeControls() {
        initializeSearchComponentStatus();
        initializeKindOfCheckListView();
        initializeSearchService();
    }

    private void initializeSearchComponentStatus(){
        ObservableList<SearchComponentStatus> statusChoiceBoxItems = FXCollections.observableArrayList();

        statusChoiceBoxItems.addAll(Arrays.asList(SearchComponentStatus.values()));

        this.statusChoiceBox.setItems(statusChoiceBoxItems);
        this.statusChoiceBox.getSelectionModel().select(SearchComponentStatus.ACTIVE);
    }

    private void initializeKindOfCheckListView(){
        TaxonomySnapshotService   taxonomySnapshot = Get.taxonomyService().getSnapshot(this.manifold);
        List<CustomCheckListItem> list             = new ArrayList<>();

        list.add(new CustomCheckListItem(Get.conceptSpecification(MetaData.METADATA____SOLOR.getNid())));
        Arrays.stream(taxonomySnapshot.getTaxonomyChildConceptNids(MetaData.HEALTH_CONCEPT____SOLOR.getNid()))
                .forEach(value -> list.add(new CustomCheckListItem(Get.conceptSpecification(value))));
        Collections.sort(list);
        this.kindOfObservableList.addAll(list);
        this.kindOfCheckListView.setItems(this.kindOfObservableList);
        list.forEach(item -> {
            if (item.getNID() == MetaData.PHENOMENON____SOLOR.getNid()) {
                this.kindOfCheckListView.getCheckModel().check(item);
            }
        });
    }

    private void initializeProgressBar(){

    }

    private void initializeSearchService(){
        this.searchService.setManifold(this.manifold);
        this.searchService.luceneQueryProperty().bind(this.searchTextField.textProperty());
        this.searchService.searchComponentStatusProperty().bind(this.statusChoiceBox.valueProperty());
        this.searchService.searchableParentsProperty().bind(this.kindOfCheckListView.checkModelProperty());

        this.searchService.stateProperty().addListener((observable, oldValue, newValue) -> {

            switch (newValue) {
                case SUCCEEDED:
                    ObservableList<ObservableDescriptionVersion> tableItems = this.resultTable.getItems();
                    ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);

                    if(this.searchService.getValue().size() == 0) {
                        this.searchTextField.pseudoClassStateChanged(CSS_NORESULT, true);
                        this.resultTable.setPlaceholder(new Label("No Results Found..."));
                        break;
                    }

                    this.searchService.getValue().stream().forEach(value -> {
                        LatestVersion<ObservableDescriptionVersion> latestDescription =
                                (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(value);

                        if (latestDescription.isPresent()) {
                           tableItems.add(latestDescription.get());
                        } else {
                           LOG.error("No latest description for: " + value);
                        }
                    });

                    this.searchTextField.pseudoClassStateChanged(CSS_SUCESS, true);
                    break;
                case FAILED:
                    this.searchTextField.pseudoClassStateChanged(CSS_FAIL, true);
                    this.resultTable.setPlaceholder(new Label("Simple Search Failed..."));
                    break;
            }

        });
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

    protected class CustomCheckListItem implements Comparable<CustomCheckListItem> {
        private ConceptSpecification conceptSpecification;

        CustomCheckListItem(ConceptSpecification conceptSpecification) {
            this.conceptSpecification = conceptSpecification;
        }

        @Override
        public int compareTo(CustomCheckListItem o) {
            return this.conceptSpecification.getFullySpecifiedConceptDescriptionText()
                                            .compareTo(o.getConceptSpecification()
                                                        .getFullySpecifiedConceptDescriptionText());
        }

        @Override
        public String toString() {
            return this.conceptSpecification.getFullySpecifiedConceptDescriptionText();
        }

        ConceptSpecification getConceptSpecification() {
            return this.conceptSpecification;
        }

        public int getNID() {
            return this.conceptSpecification.getNid();
        }
    }
}