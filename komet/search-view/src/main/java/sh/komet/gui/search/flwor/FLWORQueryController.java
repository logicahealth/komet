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
import sh.isaac.api.query.AttributeFunction;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.AttributeSpecification;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import java.util.*;
import javafx.beans.property.ReadOnlyObjectWrapper;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;

//~--- JDK imports ------------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;

//~--- non-JDK imports --------------------------------------------------------
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.ForSetsSpecification;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.clauses.*;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.xml.Jaxb;

import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.search.control.LetPropertySheet;
import sh.komet.gui.search.control.WhereParameterCell;
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
    private MenuButton addRowButton;

    @FXML
    private Button upButton;

    @FXML
    private Button downButton;

    @FXML
    private Button trashButton;

    @FXML // fx:id="returnTable"
    private TableView<AttributeSpecification> returnTable; // Value injected by FXMLLoader

    @FXML // fx:id="functionColumn"
    private TableColumn<AttributeSpecification, AttributeFunction> functionColumn; // Value injected by FXMLLoader

    @FXML // fx:id="columnNameColumn"
    private TableColumn<AttributeSpecification, String> columnNameColumn; // Value injected by FXMLLoader

    @FXML // fx:id="stampCoordinateColumn"
    private TableColumn<AttributeSpecification, LetItemKey> stampCoordinateColumn; // Value injected by FXMLLoader

    @FXML // fx:id="spacerLabel"
    private Label spacerLabel; // Value injected by FXMLLoader

    @FXML
    private MenuItem importFlwor;

    @FXML
    private MenuItem exportFlwor;

    @FXML
    private MenuItem exportResults;

    private ClauseTreeItem root;
    private Manifold manifold;
    private LetPropertySheet letPropertySheet;
    private ForPanel forPropertySheet;
    private ReturnSpecificationController returnSpecificationController;

    private LetItemsController letItemsController;
    private Query query;
    private final List<AttributeSpecification> resultColumns = new ArrayList();

    ObservableList<ConceptSpecification> joinProperties = FXCollections.observableArrayList();

    ObservableList<AttributeFunction> cellFunctions = FXCollections.observableArrayList();

    {
        cellFunctions.add(new AttributeFunction(""));
        cellFunctions.add(new AttributeFunction("Primoridal uuid"));
        cellFunctions.add(new AttributeFunction("All uuids"));
        cellFunctions.add(new AttributeFunction("Epoch to 8601 date/time"));
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public Node getMenuIcon() {
        return Iconography.FLWOR_SEARCH.getIconographic();
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
    void displayResults(int[][] resultArray, Map<ConceptSpecification, Integer> assembalgeToIndexMap) {
        ObservableList<List<String>> tableItems = resultTable.getItems();
        int columnCount = resultTable.getColumns().size();
        tableItems.clear();
        OpenIntIntHashMap fastAssemblageNidToIndexMap = new OpenIntIntHashMap();
        for (Map.Entry<ConceptSpecification, Integer> entry : assembalgeToIndexMap.entrySet()) {
            fastAssemblageNidToIndexMap.put(entry.getKey().getNid(), entry.getValue());
        }
        //ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);
        ObservableSnapshotService[] snapshotArray = new ObservableSnapshotService[columnCount];
        for (int column = 0; column < resultColumns.size(); column++) {
            AttributeSpecification columnSpecification = resultColumns.get(column);
            if (columnSpecification.getStampCoordinateKey() != null) {
                StampCoordinate stamp = (StampCoordinate) letPropertySheet.getLetItemObjectMap().get(columnSpecification.getStampCoordinateKey());
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
                Unmarshaller unmarshaller = Jaxb.createUnmarshaller();
                Query queryFromDisk = (Query) unmarshaller.unmarshal(reader);
                queryFromDisk.getRoot().setEnclosingQuery(queryFromDisk);
                setQuery(queryFromDisk);
            } catch (JAXBException | IOException ex) {
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
                marshaller.marshal(TermAux.ISAAC_UUID, System.out);

                this.query.reset();

                for (LetItemKey key : this.letPropertySheet.getLetItemObjectMap().keySet()) {
                    Object value = this.letPropertySheet.getLetItemObjectMap().get(key);
                    if (value instanceof ObservableStampCoordinate) {
                        value = ((ObservableStampCoordinate) value).getStampCoordinate();
                    } else if (value instanceof ObservableLanguageCoordinate) {
                        value = ((ObservableLanguageCoordinate) value).getLanguageCoordinate();
                    }
                    this.query.let(key, value);
                }

                ClauseTreeItem itemToProcess = this.root;
                Clause rootClause = itemToProcess.getValue()
                        .getClause();

                this.query.setRoot(rootClause);

                query.getReturnAttributeList().addAll(resultColumns);

                rootClause.setEnclosingQuery(query);

                marshaller.setEventHandler((ValidationEvent event1) -> {
                    System.out.println(event1);
                    return true;
                });

                StringWriter stringWriter1 = new StringWriter();
                marshaller.marshal(query, stringWriter1);
                String xml1 = stringWriter1.toString();
                System.out.println(xml1);

                Unmarshaller unmarshaller = Jaxb.createUnmarshaller();
                Object obj = unmarshaller.unmarshal(new StringReader(xml1));

                StringWriter stringWriter2 = new StringWriter();
                marshaller.marshal(obj, stringWriter2);
                String xml2 = stringWriter2.toString();
                System.out.println("Strings equal: " + xml1.equals(xml2));
                System.out.println(xml2);

                marshaller.marshal(query, new FileWriter(selectedFile));
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
    void executeQuery(ActionEvent event) {
        this.query.reset();
        // TODO, add let items...
        ClauseTreeItem itemToProcess = this.root;
        Clause rootClause = itemToProcess.getValue()
                .getClause();

        this.query.setRoot(rootClause);
        rootClause.setEnclosingQuery(query);

        int[][] resultArray = query.reify();
        ForSetsSpecification forSet = query.getForSetSpecification();

        FxGet.statusMessageService()
                .reportSceneStatus(anchorPane.getScene(), "Query result count: " + resultArray.length);
        displayResults(resultArray, forSet.getAssembalgeToIndexMap());
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
        assert functionColumn != null : "fx:id=\"functionColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert columnNameColumn != null : "fx:id=\"columnNameColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
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

        returnTable.setEditable(true);
        functionColumn.setCellValueFactory(new PropertyValueFactory("functionName"));
        columnNameColumn.setCellValueFactory(new PropertyValueFactory("columnName"));
        columnNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        this.upButton.setGraphic(Iconography.ARROW_UP.getIconographic());
        this.upButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            if (rowIndex > 0) {
                AttributeSpecification row = returnTable.getItems().remove(rowIndex);
                returnTable.getItems().add(rowIndex - 1, row);
                returnTable.getSelectionModel().select(rowIndex - 1);
            }
        });

        this.downButton.setGraphic(Iconography.ARROW_DOWN.getIconographic());
        this.downButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            if (rowIndex < returnTable.getItems().size() - 1) {
                AttributeSpecification row = returnTable.getItems().remove(rowIndex);
                returnTable.getItems().add(rowIndex + 1, row);
                returnTable.getSelectionModel().select(rowIndex + 1);
            }
        });
        this.trashButton.setGraphic(Iconography.DELETE_TRASHCAN.getIconographic());
        this.trashButton.setOnAction((event) -> {
            int rowIndex = returnTable.getSelectionModel().getSelectedIndex();
            returnTable.getItems().remove(rowIndex);
            returnTable.getSelectionModel().select(rowIndex);
        });

    }

    private void addChildClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.getChildren()
                .add(new ClauseTreeItem(new QueryClause(clause, manifold, this.forPropertySheet.getForAssemblagesProperty(),
                        joinProperties, letPropertySheet.getStampCoordinateKeys(), letPropertySheet.getLetItemObjectMap())));

    }

    private void addSiblingClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.getParent()
                .getChildren()
                .add(new ClauseTreeItem(new QueryClause(clause, manifold, this.forPropertySheet.getForAssemblagesProperty(),
                        joinProperties, letPropertySheet.getStampCoordinateKeys(), letPropertySheet.getLetItemObjectMap())));

    }

    private void changeClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.setValue(new QueryClause(clause, manifold, this.forPropertySheet.getForAssemblagesProperty(),
                joinProperties, letPropertySheet.getStampCoordinateKeys(), letPropertySheet.getLetItemObjectMap()));
    }

    // changeClause->, addSibling->, addChild->,
    private void deleteClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        ClauseTreeItem treeItem = (ClauseTreeItem) rowValue.getTreeItem();

        treeItem.getParent()
                .getChildren()
                .remove(treeItem);
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
                Clause[] siblings = clause.getClause()
                        .getAllowedSiblingClauses();
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

                if ((treeItem.getParent() != this.root) || (this.root.getChildren().size() > 1)) {
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
        this.query = query;
        this.joinProperties.clear();
        forPropertySheet.getForAssemblagesProperty().clear();
        for (ConceptSpecification assemblageSpec : this.query.getForSetSpecification().getForSet()) {
            forPropertySheet.getForAssemblagesProperty().add(assemblageSpec);
        }
        this.query.setForSetSpecification(forPropertySheet.getForSetSpecification());
        this.letPropertySheet.reset();
        for (Map.Entry<LetItemKey, Object> entry : this.query.getLetDeclarations().entrySet()) {
            this.letPropertySheet.addItem(entry.getKey(), entry.getValue());
        }
        this.query.setLetDeclarations(this.letPropertySheet.getLetItemObjectMap());

        QueryClause rootQueryClause = new QueryClause(this.query.getRoot(), this.manifold,
                this.forPropertySheet.getForAssemblagesProperty(),
                this.joinProperties,
                letPropertySheet.getStampCoordinateKeys(),
                letPropertySheet.getLetItemObjectMap());
        this.root = new ClauseTreeItem(rootQueryClause);
        addChildren(this.query.getRoot(), this.root);
        this.root.setExpanded(true);
        this.whereTreeTable.setRoot(root);

        // add return specifications
        this.returnSpecificationController.getReturnSpecificationRows().clear();
        for (AttributeSpecification attributeSpecification : query.getReturnAttributeList()) {
            this.returnSpecificationController.getReturnSpecificationRows().add(attributeSpecification);
        }
    }

    private void addChildren(Clause parent, ClauseTreeItem parentTreeItem) {
        for (Clause child : parent.getChildren()) {
            QueryClause childQueryClause = new QueryClause(child, this.manifold,
                    this.forPropertySheet.getForAssemblagesProperty(),
                    this.joinProperties,
                    letPropertySheet.getStampCoordinateKeys(),
                    letPropertySheet.getLetItemObjectMap());
            ClauseTreeItem childTreeItem = new ClauseTreeItem(childQueryClause);
            parentTreeItem.getChildren().add(childTreeItem);
            addChildren(child, childTreeItem);
        }
    }

    //~--- set methods ---------------------------------------------------------
    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.letPropertySheet = new LetPropertySheet(this.manifold.deepClone());
        stampCoordinateColumn.setCellValueFactory((param) -> {
            return param.getValue().stampCoordinateKeyProperty();
        });
        stampCoordinateColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(this.letPropertySheet.getStampCoordinateKeys()));

        functionColumn.setCellValueFactory((param) -> {
            return param.getValue().attributeFunctionProperty();
        });
        functionColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(cellFunctions));

        this.forPropertySheet = new ForPanel(manifold);
        this.query = new Query(forPropertySheet.getForSetSpecification());
        this.root = new ClauseTreeItem(new QueryClause(Clause.getRootClause(), manifold, this.forPropertySheet.getForAssemblagesProperty(),
                joinProperties, letPropertySheet.getStampCoordinateKeys(), letPropertySheet.getLetItemObjectMap()));

        this.root.getValue().getClause().setEnclosingQuery(this.query);

        this.root.getChildren()
                .add(new ClauseTreeItem(new QueryClause(new DescriptionLuceneMatch(this.query), manifold, this.forPropertySheet.getForAssemblagesProperty(),
                        joinProperties, letPropertySheet.getStampCoordinateKeys(), letPropertySheet.getLetItemObjectMap())));
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

        this.letAnchorPane.getChildren()
                .add(letPropertySheet.getNode());
        this.forAnchorPane.getChildren().add(this.forPropertySheet.getNode());
        this.returnSpecificationController = new ReturnSpecificationController(
                this.forPropertySheet.getForAssemblagesProperty(),
                this.letPropertySheet.getLetItemObjectMap(),
                this.cellFunctions, joinProperties,
                this.addRowButton.getItems(),
                this.manifold);
        this.returnSpecificationController.addReturnSpecificationListener(this::returnSpecificationListener);
        this.returnTable.setItems(this.returnSpecificationController.getReturnSpecificationRows());
    }

    public void returnSpecificationListener(ListChangeListener.Change<? extends AttributeSpecification> c) {
        resultTable.getColumns().clear();
        resultColumns.clear();
        int columnIndex = 0;
        for (AttributeSpecification rowSpecification : c.getList()) {
            final int currentIndex = columnIndex++;
            TableColumn<List<String>, String> column
                    = new TableColumn<>(rowSpecification.getColumnName());
            column.setCellValueFactory(param
                    -> new ReadOnlyObjectWrapper<>(param.getValue().get(currentIndex)));
            resultTable.getColumns().add(column);
            resultColumns.add(rowSpecification);
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

    void setLetItemsController(LetItemsController letItemsController) {
        this.letItemsController = letItemsController;
        this.letPropertySheet.setLetItemsController(letItemsController);
    }
}
