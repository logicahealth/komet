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
package sh.komet.gui.search;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

//~--- JDK imports ------------------------------------------------------------
import javax.validation.constraints.NotNull;

//~--- non-JDK imports --------------------------------------------------------
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import sh.isaac.api.Get;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.sememe.SememeSnapshotService;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.observable.sememe.version.ObservableDescriptionSememe;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.ComponentCollectionTypes;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.ParentClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.QueryBuilder;
import static sh.isaac.api.query.QueryBuilder.DEFAULT_MANIFOLD_COORDINATE_KEY;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;

import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.contract.ExplorationNode;
import sh.komet.gui.contract.Manifold;
import sh.komet.gui.contract.StyleClasses;

//~--- classes ----------------------------------------------------------------
public class QueryController
        implements ExplorationNode {

   private static final String CLAUSE = "clause";

   public static final boolean OUTPUT_CSS_STYLE_INFO = false;

   //~--- fields --------------------------------------------------------------
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("FLOWR query view");
   @FXML                                                           // ResourceBundle that was given to the FXMLLoader
   private ResourceBundle resources;
   @FXML  // URL location of the FXML file that was given to the FXMLLoader
   private URL location;
   @FXML                                                           // fx:id="anchorPane"
   private AnchorPane anchorPane;        // Value injected by FXMLLoader
   @FXML                                                           // fx:id="flowrAccordian"
   private Accordion flowrAccordian;    // Value injected by FXMLLoader
   @FXML                                                           // fx:id="forPane"
   private TitledPane forPane;           // Value injected by FXMLLoader
   @FXML                                                           // fx:id="letPane"
   private TitledPane letPane;           // Value injected by FXMLLoader
   @FXML                                                           // fx:id="orderPane"
   private TitledPane orderPane;         // Value injected by FXMLLoader
   @FXML                                                           // fx:id="wherePane"
   private TitledPane wherePane;         // Value injected by FXMLLoader
   @FXML                                                           // fx:id="whereTreeTable"
   private TreeTableView<QueryClause> whereTreeTable;    // Value injected by FXMLLoader
   @FXML                                                           // fx:id="clauseNameColumn"
   private TreeTableColumn<QueryClause, String> clauseNameColumn;  // Value injected by FXMLLoader
   @FXML                                                           // fx:id="parameterColumn"
   private TreeTableColumn<QueryClause, String> parameterColumn;   // Value injected by FXMLLoader
   @FXML                                                           // fx:id="returnPane"
   private TitledPane returnPane;        // Value injected by FXMLLoader
   @FXML                                                           // fx:id="executeButton"
   private Button executeButton;     // Value injected by FXMLLoader
   @FXML                                                           // fx:id="progressBar"
   private ProgressBar progressBar;       // Value injected by FXMLLoader
   @FXML                                                           // fx:id="cancelButton"
   private Button cancelButton;      // Value injected by FXMLLoader
   @FXML                                                           // fx:id="resultTable"
   private TableView<ObservableDescriptionSememe> resultTable;       // Value injected by FXMLLoader

   @FXML
   private RadioButton allComponents;

   @FXML
   private ToggleGroup forGroup;

   @FXML
   private RadioButton allConcepts;

   @FXML
   private RadioButton allDescriptions;

   @FXML
   private RadioButton allSememes;

   private TreeItem<QueryClause> root;
   private Manifold manifold;

   //~--- methods -------------------------------------------------------------
   @FXML  // This method is called by the FXMLLoader when initialization is complete
   void initialize() {
      assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'Query.fxml'.";
      assert flowrAccordian != null : "fx:id=\"flowrAccordian\" was not injected: check your FXML file 'Query.fxml'.";
      assert forPane != null : "fx:id=\"forPane\" was not injected: check your FXML file 'Query.fxml'.";
      assert allComponents != null : "fx:id=\"allComponents\" was not injected: check your FXML file 'Query.fxml'.";
      assert forGroup != null : "fx:id=\"forGroup\" was not injected: check your FXML file 'Query.fxml'.";
      assert allConcepts != null : "fx:id=\"allConcepts\" was not injected: check your FXML file 'Query.fxml'.";
      assert allDescriptions != null : "fx:id=\"allDescriptions\" was not injected: check your FXML file 'Query.fxml'.";
      assert allSememes != null : "fx:id=\"allSememes\" was not injected: check your FXML file 'Query.fxml'.";
      assert letPane != null : "fx:id=\"letPane\" was not injected: check your FXML file 'Query.fxml'.";
      assert orderPane != null : "fx:id=\"orderPane\" was not injected: check your FXML file 'Query.fxml'.";
      assert wherePane != null : "fx:id=\"wherePane\" was not injected: check your FXML file 'Query.fxml'.";
      assert whereTreeTable != null : "fx:id=\"whereTreeTable\" was not injected: check your FXML file 'Query.fxml'.";
      assert clauseNameColumn != null : "fx:id=\"clauseNameColumn\" was not injected: check your FXML file 'Query.fxml'.";
      assert parameterColumn != null : "fx:id=\"parameterColumn\" was not injected: check your FXML file 'Query.fxml'.";
      assert returnPane != null : "fx:id=\"returnPane\" was not injected: check your FXML file 'Query.fxml'.";
      assert executeButton != null : "fx:id=\"executeButton\" was not injected: check your FXML file 'Query.fxml'.";
      assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'Query.fxml'.";
      assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'Query.fxml'.";
      assert resultTable != null : "fx:id=\"resultTable\" was not injected: check your FXML file 'Query.fxml'.";
   }

   private void addChildClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
      TreeItem<QueryClause> treeItem = rowValue.getTreeItem();
      System.out.println(event.getSource().getClass());
      ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource())
              .getOnAction();
      Clause clause = (Clause) conceptAction.getProperties()
              .get(CLAUSE);
      treeItem.getChildren().add(new TreeItem<>(new QueryClause(clause, manifold)));
   }

   private void addSiblingClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
      TreeItem<QueryClause> treeItem = rowValue.getTreeItem();
      System.out.println(event.getSource().getClass());
      ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource())
              .getOnAction();
      Clause clause = (Clause) conceptAction.getProperties()
              .get(CLAUSE);

      treeItem.getParent()
              .getChildren()
              .add(new TreeItem<>(new QueryClause(clause, manifold)));
   }

   private void changeClause(ActionEvent event, TreeTableRow<QueryClause> rowValue) {
      TreeItem<QueryClause> treeItem = rowValue.getTreeItem();
      System.out.println(event.getSource().getClass());
      ConceptAction conceptAction = (ConceptAction) ((MenuItem) event.getSource())
              .getOnAction();
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

   private Collection<? extends Action> setupContextMenu(final TreeTableRow<QueryClause> rowValue) {
      // Firstly, create a list of Actions
      ArrayList<Action> actionList = new ArrayList();
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

   private void updateStyle(@NotNull String item,
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
   public Manifold getManifold() {
      return this.manifold;
   }

   //~--- set methods ---------------------------------------------------------
   public void setManifold(Manifold manifold) {
      this.manifold = manifold;
      this.root = new TreeItem<>(new QueryClause(Clause.getRootClause(), manifold));

      TreeItem orTreeItem = new TreeItem<>(new QueryClause(new Or(), manifold));
      orTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch(), manifold)));
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
               if (item != null && OUTPUT_CSS_STYLE_INFO) {
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
      this.parameterColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
      this.parameterColumn.setCellValueFactory(new TreeItemPropertyValueFactory("parameter"));
      this.whereTreeTable.setRoot(root);
   }

   @Override
   public void setParent(BorderPane parent) {
      anchorPane.setBorder(
              new Border(
                      new BorderStroke(Color.LIMEGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
      flowrAccordian.setExpandedPane(wherePane);
      parent.setCenter(anchorPane);
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }

   @FXML
   void executeQuery(ActionEvent event) {
      QueryBuilder queryBuilder = new QueryBuilder(this.manifold);

      if (allComponents.isSelected()) {
         queryBuilder.from(ComponentCollectionTypes.ALL_COMPONENTS);
      }
      if (allConcepts.isSelected()) {
         queryBuilder.from(ComponentCollectionTypes.ALL_CONCEPTS);
      }
      if (allDescriptions.isSelected()) {
         queryBuilder.from(ComponentCollectionTypes.ALL_SEMEMES);
      }
      if (allSememes.isSelected()) {
         queryBuilder.from(ComponentCollectionTypes.ALL_SEMEMES);
      }

      TreeItem<QueryClause> itemToProcess = this.root;
      Clause rootClause = itemToProcess.getValue().getClause();
      queryBuilder.setWhereRoot((ParentClause) rootClause);
      processQueryTreeItem(itemToProcess, queryBuilder);
      
      Query query = queryBuilder.build();
      rootClause.setEnclosingQuery(query);
      NidSet results = query.compute();
      System.out.println("Result count: " + results.size());
      
   }
   
   void displayResults(NidSet descriptionNids) {
      resultTable.getItems().clear();
      SememeSnapshotService<DescriptionSememe> descriptionSnapshotService = 
              Get.sememeService().getSnapshot(DescriptionSememe.class, manifold);
      
      descriptionNids.stream().forEach((nid) -> {
         Optional<LatestVersion<DescriptionSememe>> latestDescriptionOptional = descriptionSnapshotService.getLatestSememeVersion(nid);
         if (latestDescriptionOptional.isPresent()) {
           //Get.
         }
      });
      
   }

   /**
    * Recursive depth-first walk through the tree nodes. 
    * @param itemToProcess 
    */
   private void processQueryTreeItem(TreeItem<QueryClause> itemToProcess, QueryBuilder queryBuilder) {
      Clause clause = itemToProcess.getValue().getClause();
      if (itemToProcess.isLeaf()) {
         String parameter = itemToProcess.getValue().parameter.getValue();
         int row = whereTreeTable.getRow(itemToProcess);
         switch (clause.getClass().getSimpleName()) {
            case "DescriptionLuceneMatch":
               if (parameter == null) {
                  throw new IllegalStateException("Parameter cannot be null for DescriptionLuceneMatch");
               }
               DescriptionLuceneMatch descriptionLuceneMatch = (DescriptionLuceneMatch) clause;
               String parameterKey = clause.getClass().getSimpleName() + "-" + queryBuilder.getSequence();
               descriptionLuceneMatch.setLuceneMatchKey(parameterKey);
               queryBuilder.let(parameterKey, parameter);
               descriptionLuceneMatch.setViewCoordinateKey(DEFAULT_MANIFOLD_COORDINATE_KEY);
               break;
         }
         
      } else {
         ParentClause parent = (ParentClause) clause;
         for (TreeItem<QueryClause> child: itemToProcess.getChildren()) {
            parent.getChildren().add(child.getValue().getClause());
            processQueryTreeItem(child, queryBuilder);
         }
      }
   }

}
