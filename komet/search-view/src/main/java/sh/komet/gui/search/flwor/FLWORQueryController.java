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
package sh.komet.gui.search.flwor;

//~--- JDK imports ------------------------------------------------------------

/*
* Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
        US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
import javafx.beans.property.*;
import sh.isaac.api.query.JoinProperty;
import sh.isaac.api.query.AttributeFunction;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.AttributeSpecification;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

//~--- non-JDK imports --------------------------------------------------------
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//~--- JDK imports ------------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;

//~--- non-JDK imports --------------------------------------------------------
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.api.ConceptProxy;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableConceptProxy;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.ForSet;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.ParentClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.SortSpecification;
import sh.isaac.api.query.clauses.*;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.api.util.time.DurationUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.xml.Jaxb;

import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.control.concept.ConceptSpecificationForControlWrapper;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
public class FLWORQueryController
        implements ExplorationNode {

    private static final Logger LOG = LogManager.getLogger();
    private static final String CLAUSE = "clause";
    public static final boolean OUTPUT_CSS_STYLE_INFO = false;

    //~--- fields --------------------------------------------------------------
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("FLWOR query view");
    private final SimpleStringProperty titleProperty = new SimpleStringProperty(FLWORQueryViewFactory.MENU_TEXT);
    private final SimpleStringProperty titleNodeProperty = new SimpleStringProperty(FLWORQueryViewFactory.MENU_TEXT);
    private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
            Iconography.FLWOR_SEARCH.getIconographic());
    @FXML  // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML                                                                         // fx:id="anchorPane"
    private AnchorPane anchorPane;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="flowrAccordian"
    private Accordion flowrAccordian;    // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="letPane"
    private TitledPane forPane;           // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="letPane"
    private TitledPane letPane;           // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="orderPane"
    private TitledPane orderPane;         // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="wherePane"
    private TitledPane wherePane;         // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="whereTreeTable"
    private TreeTableView<QueryClause> whereTreeTable;    // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="clauseNameColumn"
    private TreeTableColumn<QueryClause, String> clauseNameColumn;  // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="clausePropertiesColumn"
    private TreeTableColumn<QueryClause, Object> clausePropertiesColumn;   // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="returnPane"
    private TitledPane returnPane;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="executeButton"
    private Button executeButton;     // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="progressBar"
    private ProgressBar progressBar;       // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="cancelButton"
    private Button cancelButton;      // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="resultTable"
    private TableView<List<String>> resultTable;       // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="textColumn"
    private TableColumn<List<String>, String> textColumn;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="typeColumn"
    private TableColumn<List<String>, Integer> typeColumn;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="languageColumn"
    private TableColumn<List<String>, Integer> languageColumn;    // Value injected by FXMLLoader
    @FXML
    private AnchorPane forAnchorPane;
    @FXML
    private AnchorPane letAnchorPane;

    @FXML
    private MenuButton returnAddRowButton;

    @FXML
    private Button returnUpButton;

    @FXML
    private Button returnDownButton;

    @FXML
    private Button returnTrashButton;

    @FXML
    private MenuButton orderAddRowButton;

    @FXML
    private Button orderUpButton;

    @FXML
    private Button orderDownButton;

    @FXML
    private Button orderTrashButton;

    @FXML // fx:id="returnTable"
    private TableView<AttributeSpecification> returnTable; // Value injected by FXMLLoader

    @FXML // fx:id="returnFunctionColumn"
    private TableColumn<AttributeSpecification, AttributeFunction> returnFunctionColumn; // Value injected by FXMLLoader

    @FXML // fx:id="returnColumnNameColumn"
    private TableColumn<AttributeSpecification, String> returnColumnNameColumn; // Value injected by FXMLLoader

    @FXML // fx:id="returnStampCoordinateColumn"
    private TableColumn<AttributeSpecification, LetItemKey> returnStampCoordinateColumn; // Value injected by FXMLLoader

    @FXML // fx:id="spacerLabel"
    private Label spacerLabel; // Value injected by FXMLLoader

    @FXML
    private TableView<SortSpecification> orderTable;
    @FXML
    private TableColumn<SortSpecification, AttributeFunction> orderFunctionColumn; // Value injected by FXMLLoader

    @FXML
    private TableColumn<SortSpecification, String> orderColumnNameColumn; // Value injected by FXMLLoader

    @FXML
    private TableColumn<SortSpecification, LetItemKey> orderStampCoordinateColumn; // Value injected by FXMLLoader

    @FXML
    private TableColumn<SortSpecification, TableColumn.SortType> orderSortColumn; // Value injected by FXMLLoader

    @FXML
    private MenuItem importFlwor;

    @FXML
    private MenuItem exportFlwor;

    @FXML
    private MenuItem exportResults;

    @FXML
    private ContextMenu resultTableContextMenu;

    private ClauseTreeItem root;
    private Manifold manifold;
    private LetPropertySheet letPropertySheet;
    private ForPanel forPropertySheet;
    private ControllerForReturnSpecification returnSpecificationController;

    private ControllerForSortSpecification sortSpecificationController;

    private LetItemsController letItemsController;
    private Query query;
    private final List<AttributeSpecification> resultColumns = new ArrayList<>();

    ObservableList<JoinProperty> joinProperties = FXCollections.observableArrayList();

    ObservableList<AttributeFunction> cellFunctions = FXCollections.observableArrayList();

    double resultTableMouseX = 0;
    double resultTableMouseY = 0;

    //~--- methods -------------------------------------------------------------
    @Override
    public Node getMenuIcon() {
        return Iconography.FLWOR_SEARCH.getIconographic();
    }

    public Query getQuery() {
        return query;
    }

    /*
    Functions needed: 
        primordial uuid for nid
        all uuids for nid
        time, date time for epochTime
    
        // language coordinate
        fully qualified name for nid
        preferred name for nid
        definition for nid

    
     */
    void displayResults(int[][] resultArray, Map<ConceptSpecification, Integer> assemblageToIndexMap) {
        ObservableList<List<String>> tableItems = resultTable.getItems();
        int columnCount = resultTable.getColumns().size();
        tableItems.clear();
        OpenIntIntHashMap fastAssemblageNidToIndexMap = new OpenIntIntHashMap();
        for (Map.Entry<ConceptSpecification, Integer> entry : assemblageToIndexMap.entrySet()) {
            fastAssemblageNidToIndexMap.put(entry.getKey().getNid(), entry.getValue());
        }
        //ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);
        ObservableSnapshotService[] snapshotArray = new ObservableSnapshotService[columnCount];
        for (int column = 0; column < resultColumns.size(); column++) {
            AttributeSpecification columnSpecification = resultColumns.get(column);
            if (columnSpecification.getStampCoordinateKey() != null) {
                StampCoordinate stamp = (StampCoordinate) letPropertySheet.getLetItemObjectMap().get(columnSpecification.getStampCoordinateKey());
                if (stamp == null) {
                    throw new IllegalStateException("No coordinate for key: " + columnSpecification.getStampCoordinateKey());
                }
                snapshotArray[column] = Get.observableSnapshotService(stamp);
            }
        }

        for (int row = 0; row < resultArray.length; row++) {
            String[] resultRow = new String[columnCount];
            LatestVersion[] latestVersionArray = new LatestVersion[resultArray[row].length];
            List[] propertyListArray = new List[resultArray[row].length];
            for (int column = 0; column < latestVersionArray.length; column++) {
                latestVersionArray[column] = snapshotArray[column].getObservableVersion(resultArray[row][column]);
                if (latestVersionArray[column].isPresent()) {
                    propertyListArray[column] = ((ObservableVersion) latestVersionArray[column].get()).getProperties();
                } else {
                    propertyListArray[column] = null;
                }
            }
            for (int column = 0; column < resultColumns.size(); column++) {
                AttributeSpecification columnSpecification = resultColumns.get(column);
                int resultArrayNidIndex = fastAssemblageNidToIndexMap.get(columnSpecification.getAssemblageNid());
                if (latestVersionArray[resultArrayNidIndex].isPresent()) {
                    List<ReadOnlyProperty<?>> propertyList = propertyListArray[resultArrayNidIndex];
                    ReadOnlyProperty<?> property = propertyList.get(columnSpecification.getPropertyIndex());
                    if (columnSpecification.getAttributeFunction() != null) {
                        StampCoordinate sc = (StampCoordinate) letPropertySheet.getLetItemObjectMap().get(columnSpecification.getStampCoordinateKey());
                        resultRow[column] = columnSpecification.getAttributeFunction().apply(property.getValue().toString(), sc, query);
                    } else {
                        resultRow[column] = property.getValue().toString();
                    }
                }
            }
            tableItems.add(Arrays.asList(resultRow));
        }
    }

    @FXML
    void importFlwor(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import FLWOR specification from file");
        fileChooser.setInitialFileName("query.flwor");
        File selectedFile = fileChooser.showOpenDialog(spacerLabel.getScene().getWindow());
        if (selectedFile != null) {
            try (FileReader reader = new FileReader(selectedFile)) {
                setManifold(this.manifold);
                setQuery(null);
                Unmarshaller unmarshaller = Jaxb.createUnmarshaller();
                Query queryFromDisk = (Query) unmarshaller.unmarshal(reader);
                queryFromDisk.getRoot().setEnclosingQuery(queryFromDisk);
                setQuery(queryFromDisk);
            } catch (Throwable ex) {
                FxGet.dialogs().showErrorDialog("Error importing " + selectedFile.getName(), ex);
            }
        }
    }

    @FXML
    void exportFlwor(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save FLWOR specification to file");
            fileChooser.setInitialFileName("query.flwor");
            File selectedFile = fileChooser.showSaveDialog(spacerLabel.getScene().getWindow());
            if (selectedFile != null) {

                Marshaller marshaller = Jaxb.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                this.query.reset();

                Map<LetItemKey, Object> currentMap = this.query.getLetDeclarations();
                HashMap<LetItemKey, Object> mapForExport = new HashMap();
                
                for (LetItemKey key : this.letPropertySheet.getLetItemObjectMap().keySet()) {
                    Object value = this.letPropertySheet.getLetItemObjectMap().get(key);
                    if (value instanceof ObservableStampCoordinate) {
                        value = ((ObservableStampCoordinate) value).getStampCoordinate();
                    } else if (value instanceof ObservableLanguageCoordinate) {
                        value = ((ObservableLanguageCoordinate) value).getLanguageCoordinate();
                    } else if (value instanceof ObservableLogicCoordinate) {
                        value = ((ObservableLogicCoordinate) value).getLogicCoordinate();
                    } else if (value instanceof ObservableConceptProxy) {
                        value = ((ObservableConceptProxy) value).get();
                    }
                    
                    if (value instanceof ConceptSpecificationForControlWrapper) {
                        value = new ConceptProxy((ConceptSpecification) value);
                    }
                    mapForExport.put(key, value);
                    
                }

                this.query.setLetDeclarations(mapForExport);
                ClauseTreeItem itemToProcess = this.root;
                Clause rootClause = itemToProcess.getValue()
                        .getClause();

                this.query.setRoot(rootClause);

                query.setReturnAttributeList(resultColumns);

                rootClause.setEnclosingQuery(query);
                marshaller.marshal(query, new FileWriter(selectedFile));
                this.query.setLetDeclarations(currentMap);
            }

        } catch (JAXBException | IOException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }

    }

    @FXML
    void exportData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save results to file");
        fileChooser.setInitialFileName("export.txt");
        File selectedFile = fileChooser.showSaveDialog(spacerLabel.getScene().getWindow());
        if (selectedFile != null) {
            try (FileWriter writer = new FileWriter(selectedFile)) {
                int columnCount = resultTable.getColumns().size();
                for (int i = 0; i < columnCount; i++) {
                    TableColumn<List<String>, ?> column = resultTable.getColumns().get(i);
                    writer.append(column.getText());
                    if (i < columnCount - 1) {
                        writer.append("\t");
                    } else {
                        writer.append("\n");
                    }
                }
                ObservableList<List<String>> tableRows = resultTable.getItems();
                for (List<String> row : tableRows) {
                    for (int i = 0; i < columnCount; i++) {
                        String cellString = row.get(i);
                        writer.append(cellString);
                        if (i < columnCount - 1) {
                            writer.append("\t");
                        } else {
                            writer.append("\n");
                        }
                    }
                }
            } catch (IOException ex) {
                FxGet.dialogs().showErrorDialog("Error writing results to file", ex);
            }
        }
    }

    @FXML
    void newFlwor(ActionEvent event) {
        this.forPropertySheet.reset();
        this.letItemsController.reset();
        this.returnSpecificationController.reset();
        this.resultTable.getItems().clear();
        this.resultColumns.clear();

        Query q = new Query(forPropertySheet.getForSetSpecification());
        q.setRoot(new Or(q));
        this.setQuery(q);
        this.letPropertySheet.addLanguageCoordinate(null);
        this.letPropertySheet.addStampCoordinate(null);
        this.letPropertySheet.addLogicCoordinate(null);
        this.letPropertySheet.addManifoldCoordinate(null);
    }

    @FXML
    void cancelQuery(ActionEvent event) {
        resultTable.getItems().clear();
        FxGet.statusMessageService()
                .reportSceneStatus(anchorPane.getScene(), "FLWOR query canceled. (Cancel not completely implemented)");
    }

    @FXML
    void executeQuery(ActionEvent event) {
        FxGet.statusMessageService()
                .reportSceneStatus(anchorPane.getScene(), "Starting FLWOR query...");
        LOG.info("Starting FLWOR query...");
        Instant startTime = Instant.now();
        try {
            long msStart = System.currentTimeMillis();
            this.query.reset();
            this.query.getLetDeclarations().putAll(this.letPropertySheet.getLetItemObjectMap());
            ClauseTreeItem itemToProcess = this.root;
            Clause rootClause = itemToProcess.getValue()
                    .getClause();

            this.query.setRoot(rootClause);
            rootClause.setEnclosingQuery(query);

            int[][] resultArray = query.reify();
            LOG.info("Finished FLWOR query reify: " + DurationUtil.format(Duration.between(startTime, Instant.now())));
            ForSet forSet = query.getForSetSpecification();

            NumberFormat formatter = new DecimalFormat("#0.000");
            FxGet.statusMessageService()
                    .reportSceneStatus(anchorPane.getScene(), "Query result count: "
                            + resultArray.length + " in "
                            + formatter.format((System.currentTimeMillis() - msStart) / 1000.0) + " seconds");
            displayResults(resultArray, forSet.getAssembalgeToIndexMap());
            LOG.info("Finished FLWOR query display (combined total): " + DurationUtil.format(Duration.between(startTime, Instant.now())));
        } catch (Exception e) {
            FxGet.dialogs().showErrorDialog("Error during query...", e);
        }

    }

    @FXML  // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert flowrAccordian != null : "fx:id=\"flowrAccordian\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert forPane != null : "fx:id=\"forPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert letPane != null : "fx:id=\"letPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert orderPane != null : "fx:id=\"orderPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert wherePane != null : "fx:id=\"wherePane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert whereTreeTable != null : "fx:id=\"whereTreeTable\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert clauseNameColumn != null :
                "fx:id=\"clauseNameColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert clausePropertiesColumn != null : "fx:id=\"clausePropertiesColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert returnPane != null : "fx:id=\"returnPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert returnTable != null : "fx:id=\"returnTable\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert returnFunctionColumn != null : "fx:id=\"functionColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert returnColumnNameColumn != null : "fx:id=\"columnNameColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert executeButton != null : "fx:id=\"executeButton\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert textColumn != null : "fx:id=\"textColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert typeColumn != null : "fx:id=\"typeColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert languageColumn != null : "fx:id=\"languageColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert forAnchorPane != null : "fx:id=\"forAnchorPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert letAnchorPane != null : "fx:id=\"letAnchorPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        resultTable.setOnDragDetected(new DragDetectedCellEventHandler());
        resultTable.setOnDragDone(new DragDoneEventHandler());
        resultTable.setContextMenu(null);
        resultTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == -1) {
                resultTable.setContextMenu(null);
            } else {
                resultTable.setContextMenu(resultTableContextMenu);
            }
        });
        resultTable.setOnMouseMoved((MouseEvent event) -> {
            FLWORQueryController.this.resultTableMouseX = event.getSceneX();
            FLWORQueryController.this.resultTableMouseY = event.getSceneY();
        });

        returnTable.setEditable(true);
        returnFunctionColumn.setCellValueFactory(new PropertyValueFactory("functionName"));
        returnColumnNameColumn.setCellValueFactory(new PropertyValueFactory("columnName"));
        returnColumnNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        this.returnUpButton.setGraphic(Iconography.ARROW_UP.getIconographic());
        this.returnUpButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            if (rowIndex > 0) {
                AttributeSpecification row = returnTable.getItems().remove(rowIndex);
                returnTable.getItems().add(rowIndex - 1, row);
                returnTable.getSelectionModel().select(rowIndex - 1);
            }
        });

        this.returnDownButton.setGraphic(Iconography.ARROW_DOWN.getIconographic());
        this.returnDownButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            if (rowIndex < returnTable.getItems().size() - 1) {
                AttributeSpecification row = returnTable.getItems().remove(rowIndex);
                returnTable.getItems().add(rowIndex + 1, row);
                returnTable.getSelectionModel().select(rowIndex + 1);
            }
        });

        this.returnTrashButton.setGraphic(Iconography.DELETE_TRASHCAN.getIconographic());
        this.returnTrashButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            returnTable.getItems().remove(rowIndex);
            returnTable.getSelectionModel().select(rowIndex);
        });

        this.orderTable.setEditable(true);
        this.orderFunctionColumn.setCellValueFactory(new PropertyValueFactory("functionName"));
        this.orderColumnNameColumn.setCellValueFactory(new PropertyValueFactory("columnName"));
        this.orderSortColumn.setCellValueFactory(new PropertyValueFactory("sortType"));

        this.orderUpButton.setGraphic(Iconography.ARROW_UP.getIconographic());
        this.orderUpButton.setOnAction((event) -> {
            int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
            if (rowIndex > 0) {
                SortSpecification row = orderTable.getItems().remove(rowIndex);
                orderTable.getItems().add(rowIndex - 1, row);
                orderTable.getSelectionModel().select(rowIndex - 1);
            }
        });

        this.orderDownButton.setGraphic(Iconography.ARROW_DOWN.getIconographic());
        this.orderDownButton.setOnAction((event) -> {
            int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
            if (rowIndex < orderTable.getItems().size() - 1) {
                SortSpecification row = orderTable.getItems().remove(rowIndex);
                orderTable.getItems().add(rowIndex + 1, row);
                orderTable.getSelectionModel().select(rowIndex + 1);
            }
        });

        this.orderTrashButton.setGraphic(Iconography.DELETE_TRASHCAN.getIconographic());
        this.orderTrashButton.setOnAction((event) -> {
            int rowIndex = orderTable.getSelectionModel().getSelectedIndex();
            orderTable.getItems().remove(rowIndex);
            orderTable.getSelectionModel().select(rowIndex);
        });
        
    }

    private void addChildClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);
        clause.setEnclosingQuery(query);

        treeItem.getChildren()
                .add(new ClauseTreeItem(new QueryClause(clause, manifold, this.forPropertySheet,
                        joinProperties, letPropertySheet)));

    }

    private void addSiblingClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);
        clause.setEnclosingQuery(query);

        treeItem.getParent()
                .getChildren()
                .add(new ClauseTreeItem(new QueryClause(clause, manifold, this.forPropertySheet,
                        joinProperties, letPropertySheet)));

    }

    private void changeClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();
        ClauseTreeItem parent = (ClauseTreeItem) treeItem.getParent();
        
        Clause originalClause = treeItem.getValue().getClause();
        if (parent != null) {
            treeItem.getValue().getClause().removeParent(parent.getValue().getClause());
        }

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);
        clause.setEnclosingQuery(query);


        treeItem.setValue(new QueryClause(clause, manifold, this.forPropertySheet,
                joinProperties, letPropertySheet));
        
        if (originalClause instanceof ParentClause && clause instanceof ParentClause) {
            for (Clause child: originalClause.getChildren()) {
                child.setParent(clause);
            }
        } else {
            treeItem.getChildren().clear();
        }
    }

    // changeClause->, addSibling->, addChild->,
    private void deleteClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ClauseTreeItem parent = (ClauseTreeItem) treeItem.getParent();

        parent.getChildren().remove(treeItem);

        Clause parentClause = parent.getValue().getClause();
        treeItem.getValue().getClause().removeParent(parentClause);
    }

    private void outputStyleInfo(String prefix, TreeTableCell nodeToStyle) {
        // System.out.println(prefix + " css metadata: " + nodeToStyle.getCssMetaData());
        // System.out.println(prefix + " style: " + nodeToStyle.getStyle());
        System.out.println(prefix + " style classes: " + nodeToStyle.getStyleClass());
    }

    private Collection<? extends Action> setupContextMenu(final TreeTableRow<QueryClause> rowValue) {
        // Firstly, create a list of Actions
        ArrayList<Action> actionList = new ArrayList<>();
        final ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        if (treeItem != null) {
            QueryClause clause = treeItem.getValue();

            if (clause != null) {
                Clause[] siblings = new Clause[]{};
                if (treeItem.getParent() != null) {
                    siblings = clause.getClause()
                            .getAllowedSiblingClauses();
                }
                Clause[] children = clause.getClause()
                        .getAllowedChildClauses();
                Clause[] substitution = clause.getClause()
                        .getAllowedSubstutitionClauses();

                if (siblings.length > 0) {
                    ConceptAction[] actions = new ConceptAction[siblings.length];

                    for (int i = 0; i < siblings.length; i++) {
                        actions[i] = new ConceptAction(
                                siblings[i],
                                (ActionEvent event) -> {
                                    addSiblingClause(event, rowValue);
                                });
                        actions[i].getProperties()
                                .put(CLAUSE, siblings[i]);
                    }

                    actionList.add(new ActionGroup("add sibling", actions));
                }

                if (children.length > 0) {
                    ConceptAction[] actions = new ConceptAction[children.length];

                    for (int i = 0; i < children.length; i++) {
                        actions[i] = new ConceptAction(
                                children[i],
                                (ActionEvent event) -> {
                                    addChildClause(event, rowValue);
                                });
                        actions[i].getProperties()
                                .put(CLAUSE, children[i]);
                    }

                    actionList.add(new ActionGroup("add child", actions));
                }

                if (substitution.length > 0) {
                    ConceptAction[] actions = new ConceptAction[substitution.length];

                    for (int i = 0; i < substitution.length; i++) {
                        actions[i] = new ConceptAction(
                                substitution[i],
                                (ActionEvent event) -> {
                                    changeClause(event, rowValue);
                                });
                        actions[i].getProperties()
                                .put(CLAUSE, substitution[i]);
                    }

                    actionList.add(new ActionGroup("change this clause", actions));
                }

                if (treeItem.getParent() != null) {
                    Action deleteAction = new Action(
                            "delete this clause",
                            (ActionEvent event) -> {
                                deleteClause(event, rowValue);
                            });

                    // deleteAction.setGraphic(GlyphFonts.fontAwesome().create('\uf013').color(Color.CORAL).size(28));
                    actionList.add(deleteAction);
                }
            }
        }

        return actionList;
    }

    private void updateStyle(String item,
            boolean empty,
            TreeTableRow<QueryClause> ttr,
            TreeTableCell nodeToStyle) {
        if (empty) {
            Arrays.stream(StyleClasses.values())
                    .forEach(styleClass -> ttr.getStyleClass()
                    .remove(styleClass.toString()));
        } else {
            if (ttr.getItem() != null) {
                ConceptSpecification clauseConcept = ttr.getItem()
                        .getClause()
                        .getClauseConcept();

                if (clauseConcept.equals(TermAux.AND_QUERY_CLAUSE)) {
                    ttr.getStyleClass()
                            .remove(StyleClasses.OR_CLAUSE.toString());
                    ttr.getStyleClass()
                            .add(StyleClasses.AND_CLAUSE.toString());
                } else if (clauseConcept.equals(TermAux.OR_QUERY_CLAUSE)) {
                    ttr.getStyleClass()
                            .add(StyleClasses.OR_CLAUSE.toString());
                    ttr.getStyleClass()
                            .remove(StyleClasses.AND_CLAUSE.toString());
                }
            }

            ClauseTreeItem rowItem = (ClauseTreeItem) nodeToStyle.getTreeTableRow()
                    .getTreeItem();

            if (rowItem != null) {
                TreeItem<QueryClause> parentItem = rowItem.getParent();
                if (parentItem != null) {
                    ConceptSpecification parentConcept = parentItem.getValue()
                            .getClause()
                            .getClauseConcept();

                    if (parentConcept.equals(TermAux.AND_QUERY_CLAUSE)) {
                        ttr.getStyleClass()
                                .remove(StyleClasses.OR_CLAUSE_CHILD.toString());
                        ttr.getStyleClass()
                                .add(StyleClasses.AND_CLAUSE_CHILD.toString());
                    } else if (parentConcept.equals(TermAux.OR_QUERY_CLAUSE)) {
                        ttr.getStyleClass()
                                .add(StyleClasses.OR_CLAUSE_CHILD.toString());
                        ttr.getStyleClass()
                                .remove(StyleClasses.AND_CLAUSE_CHILD.toString());
                    }
                }
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
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
        return this.manifold;
    }

    void setQuery(Query query) {
        this.resultTable.getItems().clear();
        this.resultTable.getColumns().clear();
        this.returnAddRowButton.getItems().clear();
        this.query = query;
        this.joinProperties.clear();
        this.forPropertySheet.getForAssemblagesProperty().clear();
        this.letPropertySheet.reset();
        this.returnSpecificationController.getReturnSpecificationRows().clear();

        if (this.query != null) {
            for (Map.Entry<LetItemKey, Object> entry : this.query.getLetDeclarations().entrySet()) {
                this.letPropertySheet.addItem(entry.getKey(), entry.getValue());
            }
            this.query.setLetDeclarations(this.letPropertySheet.getLetItemObjectMap());

            for (ConceptSpecification assemblageSpec : this.query.getForSetSpecification().getForSet()) {
                forPropertySheet.getForAssemblagesProperty().add(assemblageSpec);
            }
            this.query.setForSetSpecification(forPropertySheet.getForSetSpecification());
            QueryClause rootQueryClause = new QueryClause(this.query.getRoot(), this.manifold,
                    this.forPropertySheet,
                    this.joinProperties,
                    this.letPropertySheet);
            this.root = new ClauseTreeItem(rootQueryClause);
            addChildren(this.query.getRoot(), this.root);
            this.root.setExpanded(true);
            this.whereTreeTable.setRoot(root);

            // add return specifications
            for (AttributeSpecification attributeSpecification : this.query.getReturnAttributeList()) {
                this.returnSpecificationController.getReturnSpecificationRows().add(attributeSpecification);
            }
            this.query.setReturnAttributeList(this.returnSpecificationController.getReturnSpecificationRows());
        }
    }

    private void addChildren(Clause parent, ClauseTreeItem parentTreeItem) {
        for (Clause child : parent.getChildren()) {
            QueryClause childQueryClause = new QueryClause(child, this.manifold,
                    this.forPropertySheet,
                    this.joinProperties,
                    this.letPropertySheet);
            ClauseTreeItem childTreeItem = new ClauseTreeItem(childQueryClause);
            parentTreeItem.getChildren().add(childTreeItem);
            addChildren(child, childTreeItem);
        }
    }

    //~--- set methods ---------------------------------------------------------
    public void setManifold(Manifold manifold) throws IOException {
        this.manifold = manifold;
        this.letPropertySheet = new LetPropertySheet(this.manifold.deepClone(), this);
        returnStampCoordinateColumn.setCellValueFactory((param) -> {
            return param.getValue().stampCoordinateKeyProperty();
        });
        returnStampCoordinateColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(this.letPropertySheet.getStampCoordinateKeys()));

        returnFunctionColumn.setCellValueFactory((param) -> {
            return param.getValue().attributeFunctionProperty();
        });
        returnFunctionColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(cellFunctions));

        orderStampCoordinateColumn.setCellValueFactory((param) -> {
            return param.getValue().stampCoordinateKeyProperty();
        });
        orderStampCoordinateColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(this.letPropertySheet.getStampCoordinateKeys()));

        orderFunctionColumn.setCellValueFactory((param) -> {
            return param.getValue().attributeFunctionProperty();
        });
        orderFunctionColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(cellFunctions));

        orderSortColumn.setCellValueFactory((param) -> {
            return param.getValue().sortTypeProperty();
        });
        orderSortColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(TableColumn.SortType.values()));

        this.forPropertySheet = new ForPanel(manifold);
        this.query = new Query(forPropertySheet.getForSetSpecification());
        this.root = new ClauseTreeItem(new QueryClause(Clause.getRootClause(), manifold, this.forPropertySheet,
                joinProperties, letPropertySheet));

        this.root.getValue().getClause().setEnclosingQuery(this.query);

        this.root.getChildren()
                .add(new ClauseTreeItem(new QueryClause(new DescriptionLuceneMatch(this.query), manifold, this.forPropertySheet,
                        joinProperties, letPropertySheet)));
        this.root.getValue().getClause().setEnclosingQuery(this.query);
        this.root.setExpanded(true);
        this.clauseNameColumn.setCellFactory(
                (TreeTableColumn<QueryClause, String> p) -> {
                    TreeTableCell<QueryClause, String> cell = new TreeTableCell<QueryClause, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);

                    TreeTableRow<QueryClause> rowValue = this.tableRowProperty()
                            .getValue();

                    updateStyle(item, empty, getTreeTableRow(), this);

                    if ((item != null) && OUTPUT_CSS_STYLE_INFO) {
                        outputStyleInfo("updateItem: " + item, this);
                    }

                    setContextMenu(ActionUtils.createContextMenu(setupContextMenu(rowValue)));
                }
            };

                    return cell;
                });

        // Given the data in the row, return the observable value for the resultArrayNidIndex.
        this.clauseNameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<QueryClause, String> p) -> p.getValue()
                        .getValue().clauseName);

        this.clausePropertiesColumn.setCellValueFactory(new TreeItemPropertyValueFactory("clause"));
        this.clausePropertiesColumn.setCellFactory(param -> new WhereParameterCell());
        this.whereTreeTable.setRoot(root);
        this.whereTreeTable.setFixedCellSize(-1);

        this.forAnchorPane.getChildren().add(this.forPropertySheet.getNode());
        
         FXMLLoader letItemsLoader = new FXMLLoader(getClass().getResource("/sh/komet/gui/search/fxml/LetItems.fxml"));
         letItemsLoader.load();
         this.setLetItemsController(letItemsLoader.getController());

        this.letAnchorPane.getChildren()
                .add(letPropertySheet.getNode());
        this.sortSpecificationController = new ControllerForSortSpecification(
                this.forPropertySheet.getForAssemblagesProperty(),
                this.letItemsController.getLetListViewletListView().getItems(),
                this.letPropertySheet.getLetItemObjectMap(),
                this.cellFunctions,
                this.joinProperties,
                this.orderAddRowButton.getItems(),
                this.resultTable,
                this.manifold);
        this.orderTable.setItems(this.sortSpecificationController.getSpecificationRows());

        this.returnSpecificationController = new ControllerForReturnSpecification(
                this.forPropertySheet.getForAssemblagesProperty(),
                this.letItemsController.getLetListViewletListView().getItems(),
                this.letPropertySheet.getLetItemObjectMap(),
                this.cellFunctions,
                this.joinProperties,
                this.returnAddRowButton.getItems(),
                this.resultTable,
                this.manifold);
        this.returnSpecificationController.addReturnSpecificationListener(this::returnSpecificationListener);
        this.returnTable.setItems(this.returnSpecificationController.getReturnSpecificationRows());

    }

    public void returnSpecificationListener(ListChangeListener.Change<? extends AttributeSpecification> c) {
        try {
            resultTable.getItems().clear();
            resultTable.getColumns().clear();
            resultColumns.clear();
            int columnIndex = 0;
            for (AttributeSpecification rowSpecification : c.getList()) {
                final int currentIndex = columnIndex++;
                TableColumn<List<String>, String> column
                        = new TableColumn<>(rowSpecification.getColumnName());
                column.setCellValueFactory(param // TableColumn$CellDataFeatures
                        -> new ReadOnlyObjectWrapper<>(param.getValue().get(currentIndex)));
                column.setComparator(new NaturalOrder());
                resultTable.getColumns().add(column);
                resultColumns.add(rowSpecification);
            }

        } catch (Exception e) {
            FxGet.dialogs().showErrorDialog("Error modifying return specifications.", e);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public Node getNode() {
        flowrAccordian.setExpandedPane(wherePane);
        return anchorPane;
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public SimpleBooleanProperty closeExplorationNodeProperty() {
        return null;
    }

    void setLetItemsController(LetItemsController letItemsController) {
        this.letItemsController = letItemsController;
        this.letPropertySheet.setLetItemsController(letItemsController);
        this.letPropertySheet.addLanguageCoordinate(null);
        this.letPropertySheet.addStampCoordinate(null);
        this.letPropertySheet.addLogicCoordinate(null);
        this.letPropertySheet.addManifoldCoordinate(null);
    }

    @FXML
    protected void copyCellToClipboard(ActionEvent actionEvent) {
        StringBuilder clipboardString = new StringBuilder();
        Node pickedNode = pickTableCell(this.resultTable, this.resultTableMouseX, this.resultTableMouseY);
        if (pickedNode != null && pickedNode instanceof TableCell) {
            TableCell clickedCell = (TableCell) pickedNode;
            clipboardString.append(clickedCell.getItem().toString());
            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(clipboardString.toString());

            // set clipboard content
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }

    }

    @FXML
    protected void copyRowToClipboard(ActionEvent actionEvent) {
        StringBuilder clipboardString = new StringBuilder();
        int rowIndex = this.resultTable.getSelectionModel().getSelectedIndex();
        if (rowIndex > -1) {
            List<String> rowList = this.resultTable.getItems().get(rowIndex);
            for (int i = 0; i < rowList.size(); i++) {
                clipboardString.append(rowList.get(i));
                if (i < rowList.size() - 1) {
                    clipboardString.append("\t");
                } else {
                    clipboardString.append("\n");
                }
            }
            final ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(clipboardString.toString());

            // set clipboard content
            Clipboard.getSystemClipboard().setContent(clipboardContent);

        }
    }

    public static Node pickTableCell(Node node, double sceneX, double sceneY) {
        if (node == null) {
            return null;
        }
        if (node instanceof TableCell) {
            return node;
        }
        Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!node.contains(p)) {
            return null;
        }

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                p = child.sceneToLocal(sceneX, sceneY, true /* rootScene */);
                if (child.isVisible() && !child.isMouseTransparent() && child.contains(p)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                return pickTableCell(bestMatchingChild, sceneX, sceneY);
            }
        }

        return node;
    }
}
