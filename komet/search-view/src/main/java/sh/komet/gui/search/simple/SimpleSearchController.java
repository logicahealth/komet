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

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.komet.gui.graphview.MultiParentGraphCell;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.clipboard.ClipboardHelper;
import sh.komet.gui.control.manifold.CoordinateMenuFactory;
import sh.komet.gui.control.manifold.ManifoldMenuModel;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.gui.table.DescriptionTableCell;

import java.util.*;
import java.util.function.Predicate;

import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.GuiSearcher;

/**
 * @author kec
 */
public class SimpleSearchController extends ExplorationNodeAbstract implements GuiSearcher, ConceptExplorationNode, Predicate<DescriptionVersion> {

    {
        titleProperty.setValue(SimpleSearchViewFactory.MENU_TEXT);
        toolTipProperty.setValue("Simple Search Panel. ");
        menuIconProperty.setValue(Iconography.SIMPLE_SEARCH.getIconographic());
    }

    private static final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
    private static final Logger              LOG               = LogManager.getLogger();

    private final SimpleSearchService                         searchService        = new SimpleSearchService(this::test);
    private final SimpleListProperty<Integer> draggedTaxonomyConceptsForFilteringListProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleObjectProperty<ConceptSpecification> selectedConceptSpecificationProperty = new SimpleObjectProperty<>();

    private final ContextMenu copyMenu = new ContextMenu();

    @FXML
    MenuButton searchPanelMenuButton;

    @FXML
    Menu coordinatesMenu;

    private ActivityFeed activityFeed;

    private ManifoldMenuModel manifoldMenuModel;


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

    @FXML
    private CheckBox defCheckBox;

    @FXML
    private CheckBox namCheckBox;

    @FXML
    private CheckBox fqnCheckBox;

    @FXML
    private CheckBox anyCheckBox;


    @Override
    public void revertPreferences() {

    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getMenuIconGraphic() {
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


        searchPanelMenuButton.setGraphic(Iconography.COORDINATES.getStyledIconographic());

        anyCheckBox.selectedProperty().addListener(((observable, wasSelected, isSelected) -> {
            if (isSelected == false && fqnCheckBox.isSelected() && namCheckBox.isSelected() && defCheckBox.isSelected()) {
                defCheckBox.setSelected(false);
            }
            updateTypeChecks(isSelected);
        }));
        fqnCheckBox.selectedProperty().addListener(((observable, wasSelected, isSelected) -> {
            updateTypeChecks(isSelected);
        }));
        namCheckBox.selectedProperty().addListener(((observable, wasSelected, isSelected) -> {
            updateTypeChecks(isSelected);
        }));
        defCheckBox.selectedProperty().addListener(((observable, wasSelected, isSelected) -> {
            updateTypeChecks(isSelected);
        }));

        this.resultTable.setOnDragDetected(new DragDetectedCellEventHandler());
        this.resultTable.setOnDragDone(new DragDoneEventHandler());
        //this.resultColumn.setCellValueFactory(new PropertyValueFactory("Result"));
        this.resultColumn.setCellValueFactory((TableColumn.CellDataFeatures<ObservableDescriptionVersion,
                String> param) -> param.getValue()
                .textProperty());

        resultTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            this.resultColumn.setMaxWidth(newValue.doubleValue() - 20);
            this.resultColumn.setMinWidth(newValue.doubleValue() - 20);
            this.resultColumn.setPrefWidth(newValue.doubleValue() - 20);
        });

        this.resultTable.setContextMenu(copyMenu);
        this.resultTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super ObservableDescriptionVersion>)  c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //nothing to do...
                    }
                } else if (c.wasUpdated()) {
                    //nothing to do
                } else {
                    for (ObservableDescriptionVersion remitem : c.getRemoved()) {
                        activityFeed.feedSelectionProperty().remove(new ComponentProxy(Get.concept(remitem.getReferencedComponentNid())));
                    }
                    for (ObservableDescriptionVersion additem : c.getAddedSubList()) {
                        activityFeed.feedSelectionProperty().add(new ComponentProxy(Get.concept(additem.getReferencedComponentNid())));
                    }
                }
            }
            if (activityFeed.feedSelectionProperty().size() != c.getList().size()) {
                ArrayList<ComponentProxy> selectionList = new ArrayList<>(c.getList().size());
                for (ObservableDescriptionVersion descriptionVersion: c.getList()) {
                    selectionList.add(new ComponentProxy(Get.concept(descriptionVersion.getReferencedComponentNid())));
                }
                activityFeed.feedSelectionProperty().setAll(selectionList);

            }
        });

        FxGet.searchers().add(this);
        resultTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue != null) {
                    this.selectedConceptSpecificationProperty.set(Get.concept(newValue.getReferencedComponentNid()));
                } else {
                    this.selectedConceptSpecificationProperty.set(null);
                }
            } catch (Exception e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        });

        resultTable.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                copySelectedDescriptionsToClipboard(event);
            }
        });

    }

    private void updateTypeChecks(Boolean isSelected) {
        if (!isSelected) {
            anyCheckBox.setSelected(false);
        } else if (fqnCheckBox.isSelected() && namCheckBox.isSelected() && defCheckBox.isSelected()) {
            anyCheckBox.setSelected(true);
        }
        if (anyCheckBox.isSelected()) {
            fqnCheckBox.setSelected(true);
            namCheckBox.setSelected(true);
            defCheckBox.setSelected(true);
        }
        executeSearch();
    }

    @FXML
    public void copySelectedConceptsToClipboard(Event event) {
        final Set<Integer> rows = new TreeSet<>();
        for (final TablePosition tablePosition : resultTable.getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow());
        }
        ArrayList<IdentifiedObject> objects = new ArrayList<>();
        OpenIntHashSet addedObjectIds = new OpenIntHashSet();
        for (final Integer row : rows) {
            ObservableDescriptionVersion description = resultTable.getItems().get(row);
            if (!addedObjectIds.contains(description.getReferencedComponentNid())) {
                objects.add(Get.concept(description.getReferencedComponentNid()));
                addedObjectIds.add(description.getReferencedComponentNid());
            }
        }
        ClipboardHelper.copyToClipboard(objects);
        event.consume();
    }
    @FXML
    public void copyAllConceptsToClipboard(Event event) {

        ArrayList<IdentifiedObject> objects = new ArrayList<>();
        OpenIntHashSet addedObjectIds = new OpenIntHashSet();
        for (final ObservableDescriptionVersion description : resultTable.getItems()) {
            if (!addedObjectIds.contains(description.getReferencedComponentNid())) {
                objects.add(Get.concept(description.getReferencedComponentNid()));
                addedObjectIds.add(description.getReferencedComponentNid());
            }
        }
        ClipboardHelper.copyToClipboard(objects);
        event.consume();
    }
    @FXML
    public void copyAllDescriptionsToClipboard(Event event) {
        ClipboardHelper.copyToClipboard(resultTable.getItems());
        event.consume();
    }
    @FXML
    public void copySelectedDescriptionsToClipboard(Event event) {
        final Set<Integer> rows = new TreeSet<>();
        for (final TablePosition tablePosition : resultTable.getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow());
        }
        ArrayList<IdentifiedObject> objects = new ArrayList<>();
        for (final Integer row : rows) {
            objects.add(resultTable.getItems().get(row));
        }
        ClipboardHelper.copyToClipboard(objects);
        event.consume();
    }
    private void initializeControls() {
        initializeSearchTextField();
        initializeProgressBar();
        initializeSearchService();
        initializeSearchTagFlowPlane();

        this.coordinatesMenu.setGraphic(Iconography.COORDINATES.getStyledIconographic());

        this.manifoldMenuModel = new ManifoldMenuModel(viewProperties, searchPanelMenuButton, this.coordinatesMenu);

        CoordinateMenuFactory.makeCoordinateDisplayMenu(this.viewProperties.getManifoldCoordinate(),
                this.coordinatesMenu.getItems(), this.viewProperties.getManifoldCoordinate());

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
        allLabel.setText("All concept kinds");
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

            ConceptChronology droppedChronology = ((MultiParentGraphCell)event.getGestureSource()).getTreeItem().getValue();
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

        this.draggedTaxonomyConceptsForFilteringListProperty.addListener((ListChangeListener<Integer>) c -> {
            if (c.getList().isEmpty()) {
                allLabel.setText("All concept kinds");
            } else {
                allLabel.setText("Only: ");
            }
            executeSearch();
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
        this.searchService.setViewProperties(this.viewProperties);
        this.searchService.luceneQueryProperty().bind(this.searchTextField.textProperty());
        this.searchService.parentNidsProperty().bind(this.draggedTaxonomyConceptsForFilteringListProperty);

        this.searchService.stateProperty().addListener((observable, oldValue, newValue) -> {

            switch (newValue) {
                case SUCCEEDED:
                    ObservableList<ObservableDescriptionVersion> tableItems = this.resultTable.getItems();
                    ObservableSnapshotService snapshot = Get.observableSnapshotService(this.viewProperties.getViewStampFilter());

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

    public void setViewProperties(ViewProperties viewProperties, ActivityFeed activityFeed) {
        this.viewProperties = viewProperties;
        this.viewProperties.getManifoldCoordinate().addListener((observable, oldValue, newValue) -> {
            executeSearch();
        });
        this.activityFeed = activityFeed;
        this.resultColumn.setCellFactory((TableColumn<ObservableDescriptionVersion,
                String> stringText) -> new DescriptionTableCell(this.viewProperties));

        initializeControls();
    }

    @Override
    public Node getNode() {
        return mainAnchorPane;
    }

    @Override
    public Optional<Node> getTitleNode() {
        Label titleLabel = new Label();

        titleLabel.graphicProperty().bind(menuIconProperty);
        titleLabel.textProperty().bind(titleProperty);
        titleProperty.set("");

        return Optional.of(titleLabel);
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public ReadOnlyObjectProperty<ConceptSpecification> selectedConceptSpecification() {
        return selectedConceptSpecificationProperty;
    }

    @Override
    public void focusOnInput() {
        this.searchTextField.requestFocus();
    }

    @Override
    public void focusOnResults() {
        this.resultTable.requestFocus();
    }

    @Override
    public ActivityFeed getActivityFeed() {
        return this.activityFeed;
    }

    @Override
    public boolean test(DescriptionVersion descriptionVersion) {
        if (anyCheckBox.isSelected()) {
            return true;
        }

        if (fqnCheckBox.isSelected() && descriptionVersion.getDescriptionTypeConceptNid() == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
            return true;
        }
        if (namCheckBox.isSelected() && descriptionVersion.getDescriptionTypeConceptNid() == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
            return true;
        }

        if (defCheckBox.isSelected() && descriptionVersion.getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            return true;
        }
        return false;
    }
}