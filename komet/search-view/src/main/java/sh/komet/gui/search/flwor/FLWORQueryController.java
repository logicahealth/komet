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
import java.net.URL;

import java.util.*;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.*;

//~--- JDK imports ------------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.MetaData;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.observable.ObservableSnapshotService;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.ParentClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.QueryBuilder;
import sh.isaac.api.query.clauses.*;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.search.control.LetPropertySheet;
import sh.komet.gui.search.control.WhereParameterCell;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.table.DescriptionTableCell;
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
    private TableView<ObservableDescriptionVersion> resultTable;       // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="textColumn"
    private TableColumn<ObservableDescriptionVersion, String> textColumn;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="typeColumn"
    private TableColumn<ObservableDescriptionVersion, Integer> typeColumn;        // Value injected by FXMLLoader
    @FXML                                                                         // fx:id="languageColumn"
    private TableColumn<ObservableDescriptionVersion, Integer> languageColumn;    // Value injected by FXMLLoader
    @FXML
    private AnchorPane letAnchorPane;
    @FXML
    private MenuButton forMenu;
    private ConceptSpecification forAssemblage;

    private TreeItem<QueryClause> root;
    private Manifold manifold;
    private LetPropertySheet letPropertySheet;
    
    private LetItemsController letItemsController;

    //~--- methods -------------------------------------------------------------
    @Override
    public Node getMenuIcon() {
        return Iconography.FLWOR_SEARCH.getIconographic();
    }

    void displayResults(NidSet resultNids) {
        ObservableList<ObservableDescriptionVersion> tableItems = resultTable.getItems();

        tableItems.clear();

        ObservableSnapshotService snapshot = Get.observableSnapshotService(this.manifold);

//      ObservableSnapshotService snapshot = Get.observableSnapshotService(this.letPropertySheet.getManifold());
        for (int nid : resultNids.asArray()) {
            switch (Get.identifierService().getObjectTypeForComponent(nid)) {
                case CONCEPT: {
                    // convert to a description. 
                    LatestVersion<DescriptionVersion> latestDescriptionForConcept = manifold.getDescription(nid, manifold.getManifoldCoordinate());
                    if (latestDescriptionForConcept.isPresent()) {
                        LatestVersion<ObservableDescriptionVersion> latestDescription
                                = (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(
                                        latestDescriptionForConcept.get().getNid());

                        if (latestDescription.isPresent()) {
                            tableItems.add(latestDescription.get());
                        } else {
                            LOG.error("No latest description for concept: " + Get.conceptDescriptionText(nid));
                        }
                    }
                }
                break;
                case SEMANTIC:
                    LatestVersion<ObservableDescriptionVersion> latestDescription
                            = (LatestVersion<ObservableDescriptionVersion>) snapshot.getObservableSemanticVersion(
                                    nid);

                    if (latestDescription.isPresent()) {
                        tableItems.add(latestDescription.get());
                    } else {
                        LOG.error("No latest description for: " + nid);
                    }
                    break;
                default:
                    LOG.error("Can't handle type in result display: "
                            + Get.identifierService().getObjectTypeForComponent(nid) + " for: " + nid);
            }
        }
    }

    @FXML
    void executeQuery(ActionEvent event) {
        QueryBuilder queryBuilder = new QueryBuilder()
                .from(this.forAssemblage);

        TreeItem<QueryClause> itemToProcess = this.root;
        Clause rootClause = itemToProcess.getValue()
                .getClause();

        queryBuilder.setWhereRoot((ParentClause) rootClause);
        processQueryTreeItem(itemToProcess, queryBuilder);

        Query query = queryBuilder.build();

        rootClause.setEnclosingQuery(query);

        NidSet results = query.compute();

        FxGet.statusMessageService()
                .reportSceneStatus(anchorPane.getScene(), "Query result count: " + results.size());
        displayResults(results);
    }

    @FXML  // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert flowrAccordian != null : "fx:id=\"flowrAccordian\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert letPane != null : "fx:id=\"letPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert orderPane != null : "fx:id=\"orderPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert wherePane != null : "fx:id=\"wherePane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert whereTreeTable != null : "fx:id=\"whereTreeTable\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert clauseNameColumn != null :
                "fx:id=\"clauseNameColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert clausePropertiesColumn != null : "fx:id=\"clausePropertiesColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert returnPane != null : "fx:id=\"returnPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert executeButton != null : "fx:id=\"executeButton\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert textColumn != null : "fx:id=\"textColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert typeColumn != null : "fx:id=\"typeColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert languageColumn != null : "fx:id=\"languageColumn\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert letAnchorPane != null : "fx:id=\"letAnchorPane\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        assert forMenu != null : "fx:id=\"forMenu\" was not injected: check your FXML file 'FLOWRQuery.fxml'.";
        textColumn.setCellValueFactory(
                (TableColumn.CellDataFeatures<ObservableDescriptionVersion, String> param) -> param.getValue()
                        .textProperty());
        textColumn.setCellFactory(
                (TableColumn<ObservableDescriptionVersion, String> stringText) -> new DescriptionTableCell());
        resultTable.setOnDragDetected(new DragDetectedCellEventHandler());
        resultTable.setOnDragDone(new DragDoneEventHandler());

        
    }

    protected void setupForMenu() {
        forMenu.getItems().clear();
        Menu favorites = new Menu("favorites");
        forMenu.getItems().add(favorites);
        favorites.getItems().add(makeMenuFromAssemblageNid(MetaData.SOLOR_CONCEPT____SOLOR.getNid()));
        favorites.getItems().add(makeMenuFromAssemblageNid(MetaData.ENGLISH_LANGUAGE____SOLOR.getNid()));
        
        
        Menu byType = new Menu("by type");
        forMenu.getItems().add(byType);
        
        HashMap<VersionType, Menu> versionTypeMenuMap = new HashMap();
        for (VersionType versionType : VersionType.values()) {
            Menu versionTypeMenu = new Menu(versionType.toString());
            versionTypeMenuMap.put(versionType, versionTypeMenu);
            byType.getItems().add(versionTypeMenu);
        }
        int[] assembalgeNids = Get.assemblageService().getAssemblageConceptNids();
        LOG.debug("Assemblage nid count: " + assembalgeNids.length + "\n" + org.apache.mahout.math.Arrays.toString(assembalgeNids));
        
        Menu byName = new Menu("by name");
        forMenu.getItems().add(byName);
        
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            MenuItem menu = makeMenuFromAssemblageNid(assemblageNid);
            byName.getItems().add(menu);
            
            MenuItem menu2 = makeMenuFromAssemblageNid(assemblageNid);
            VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
            versionTypeMenuMap.get(versionType).getItems().add(menu2);
        }
        byName.getItems().sort((o1, o2) -> {
            return o1.getText().compareTo(o2.getText());
        });
        for (Menu menu: versionTypeMenuMap.values()) {
            menu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText()); 
            });
        }
    }

    protected MenuItem makeMenuFromAssemblageNid(int assemblageNid) {
        MenuItem menu = new MenuItem(manifold.getPreferredDescriptionText(assemblageNid));
        menu.setOnAction((event) -> {
            this.forMenu.setText(manifold.getPreferredDescriptionText(assemblageNid));
            this.manifold.setFocusedConceptChronology(Get.concept(assemblageNid));
            this.forAssemblage = Get.conceptSpecification(assemblageNid);
            
        });
        return menu;
    }

    private void addChildClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        TreeItem<QueryClause> treeItem = rowValue.getTreeItem();

        System.out.println(event.getSource()
                .getClass());

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(clause, manifold)));

    }

    private void addSiblingClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        TreeItem<QueryClause> treeItem = rowValue.getTreeItem();

        System.out.println(event.getSource()
                .getClass());

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.getParent()
                .getChildren()
                .add(new TreeItem<>(new QueryClause(clause, manifold)));

    }

    private void changeClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        TreeItem<QueryClause> treeItem = rowValue.getTreeItem();

        System.out.println(event.getSource()
                .getClass());

        ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource()).getOnAction();
        Clause clause = (Clause) conceptAction.getProperties()
                .get(CLAUSE);

        treeItem.setValue(new QueryClause(clause, manifold));
    }

    // changeClause->, addSibling->, addChild->,
    private void deleteClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
        TreeItem<QueryClause> treeItem = rowValue.getTreeItem();

        treeItem.getParent()
                .getChildren()
                .remove(treeItem);
    }

    private void outputStyleInfo(String prefix, TreeTableCell nodeToStyle) {
        // System.out.println(prefix + " css metadata: " + nodeToStyle.getCssMetaData());
        // System.out.println(prefix + " style: " + nodeToStyle.getStyle());
        System.out.println(prefix + " style classes: " + nodeToStyle.getStyleClass());
    }

    /**
     * Recursive depth-first walk through the tree nodes.
     *
     * @param itemToProcess
     */
    private void processQueryTreeItem(TreeItem<QueryClause> itemToProcess, QueryBuilder queryBuilder) {
        Clause clause = itemToProcess.getValue().getClause();

        if (itemToProcess.isLeaf()) {

            if (clause.getClass().equals(AssemblageContainsConcept.class)) {

            } else if (clause.getClass().equals(AssemblageContainsKindOfConcept.class)) {

            } else if (clause.getClass().equals(AssemblageContainsString.class)) {

            } else if (clause.getClass().equals(AssemblageLuceneMatch.class)) {

            } else if (clause.getClass().equals(ConceptForComponent.class)) {

            } else if (clause.getClass().equals(ConceptIs.class)) {

            } else if (clause.getClass().equals(ConceptIsChildOf.class)) {

            } else if (clause.getClass().equals(ConceptIsDescendentOf.class)) {

            } else if (clause.getClass().equals(ConceptIsKindOf.class)) {

            } else if (clause.getClass().equals(DescriptionActiveLuceneMatch.class)) {

            } else if (clause.getClass().equals(DescriptionActiveRegexMatch.class)) {

            } else if (clause.getClass().equals(DescriptionLuceneMatch.class)) {

            } else if (clause.getClass().equals(DescriptionRegexMatch.class)) {

            } else if (clause.getClass().equals(ChangedBetweenVersions.class)) {

            } else if (clause.getClass().equals(FullyQualifiedNameForConcept.class)) {

            } else if (clause.getClass().equals(PreferredNameForConcept.class)) {

            } else if (clause.getClass().equals(RelationshipIsCircular.class)) {

            } else if (clause.getClass().equals(RelRestriction.class)) {

            } else {
                System.out.println("Missed a clause!");
            }

        } else {
            ParentClause parent = (ParentClause) clause;

            itemToProcess.getChildren()
                    .stream()
                    .map(
                            (child) -> {
                                parent.getChildren()
                                        .add(child.getValue()
                                                .getClause());
                                return child;
                            })
                    .forEachOrdered(
                            (child) -> {
                                processQueryTreeItem(child, queryBuilder);
                            });
        }
    }

    private Collection<? extends Action> setupContextMenu(final TreeTableRow<QueryClause> rowValue) {
        // Firstly, create a list of Actions
        ArrayList<Action> actionList = new ArrayList<>();
        final TreeItem<QueryClause> treeItem = rowValue.getTreeItem();

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

            TreeItem<QueryClause> rowItem = nodeToStyle.getTreeTableRow()
                    .getTreeItem();

            if (rowItem != null) {
                TreeItem<QueryClause> parentItem = rowItem.getParent();
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

    //~--- set methods ---------------------------------------------------------
    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.root = new TreeItem<>(new QueryClause(Clause.getRootClause(), manifold));

        TreeItem orTreeItem = new TreeItem<>(new QueryClause(new Or(), manifold));

        orTreeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch(), manifold)));
        this.root.getChildren()
                .add(orTreeItem);
        orTreeItem.setExpanded(true);
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

        // Given the data in the row, return the observable value for the column.
        this.clauseNameColumn.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<QueryClause, String> p) -> p.getValue()
                        .getValue().clauseName);

        this.clausePropertiesColumn.setCellValueFactory(new TreeItemPropertyValueFactory("clause"));
        this.clausePropertiesColumn.setCellFactory(param -> new WhereParameterCell());
        this.whereTreeTable.setRoot(root);
        this.whereTreeTable.setFixedCellSize(-1);
        this.textColumn.setCellValueFactory(new PropertyValueFactory("text"));
        this.typeColumn.setCellValueFactory(new PropertyValueFactory("descriptionTypeConceptSequence"));
        this.typeColumn.setCellFactory(
                column -> {
                    return new TableCell<ObservableDescriptionVersion, Integer>() {
                @Override
                protected void updateItem(Integer conceptSequence, boolean empty) {
                    super.updateItem(conceptSequence, empty);

                    if ((conceptSequence == null) || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(manifold.getPreferredDescriptionText(conceptSequence));
                    }
                }
            };
                });
        this.languageColumn.setCellValueFactory(new PropertyValueFactory("languageConceptSequence"));

        // TODO: make concept description cell factory...
        this.languageColumn.setCellFactory(
                column -> {
                    return new TableCell<ObservableDescriptionVersion, Integer>() {
                @Override
                protected void updateItem(Integer conceptSequence, boolean empty) {
                    super.updateItem(conceptSequence, empty);

                    if ((conceptSequence == null) || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(manifold.getPreferredDescriptionText(conceptSequence));
                    }
                }
            };
                });
        resultTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) -> {
                            if (newSelection != null) {
                                manifold.setFocusedConceptChronology(
                                        Get.conceptService()
                                                .getConceptChronology(newSelection.getReferencedComponentNid()));
                            }
                        });

        letPropertySheet = new LetPropertySheet(this.manifold.deepClone());
        this.letAnchorPane.getChildren()
                .add(letPropertySheet.getNode());
        
        setupForMenu();
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
