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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.HashMap;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;

import javafx.util.StringConverter;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.And;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.clauses.ConceptIsChildOf;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;

import sh.komet.gui.contract.ExplorationNode;
import sh.komet.gui.contract.GuiStyles;
import sh.komet.gui.contract.Manifold;

import static sh.komet.gui.contract.GuiStyles.EMPTY_BACKGROUND_CSS_COLOR;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class SearchView
        implements ExplorationNode {

   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty(
           "Search view");
   private final BorderPane searchPane = new BorderPane();
   private final TreeTableView<QueryClause> treeTable = new TreeTableView<>();
   private final TreeTableColumn<QueryClause, QueryClause> clauseNameColumn = new TreeTableColumn<>("Clause Name");
   private final TreeTableColumn<QueryClause, Long> lastModifiedCol = new TreeTableColumn<>("Size");
   private final ObservableList<QueryClause> allClauseClauseList = FXCollections.observableArrayList();
   TreeItem<QueryClause> root;
   private final Manifold manifold;

   //~--- constructors --------------------------------------------------------
   public SearchView(Manifold manifold) {
      this.manifold = manifold;
      this.root = new TreeItem<>(new QueryClause(Clause.getRootClause()));
      this.treeTable.setRoot(root);
      this.treeTable.setShowRoot(false);
      this.treeTable.setEditable(true);
      TreeItem andTreeItem = new TreeItem<>(new QueryClause(new And()));
         andTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new ConceptIsChildOf())));
         andTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch())));
         andTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new And())));
         andTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new Or())));
      TreeItem orTreeItem = new TreeItem<>(new QueryClause(new Or()));
         orTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new ConceptIsChildOf())));
         orTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch())));
         orTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new And())));
         orTreeItem.getChildren().add(new TreeItem<>(new QueryClause(new Or())));
      this.root.getChildren()
              .add(andTreeItem);
      this.root.getChildren()
              .add(orTreeItem);
      this.treeTable.getColumns()
              .setAll(clauseNameColumn, lastModifiedCol);
      this.clauseNameColumn.setCellFactory(
              (TreeTableColumn<QueryClause, QueryClause> p) -> {
                 ComboBoxTreeTableCell<QueryClause, QueryClause> cell = new ComboBoxTreeTableCell<QueryClause, QueryClause>(
                         new ClauseStringConverter(),
                         allClauseClauseList) {
            @Override
            public void updateItem(QueryClause item, boolean empty) {
               super.updateItem(item, empty);
               updateStyle(item, empty, getTreeTableRow(), this);
            }
         };

                 return cell;
              });

      // Given the data in the row, return the observable value for the column.
      this.clauseNameColumn.setCellValueFactory(
              (TreeTableColumn.CellDataFeatures<QueryClause, QueryClause> p) -> p.getValue()
                      .valueProperty());
      this.lastModifiedCol.setCellValueFactory(new TreeItemPropertyValueFactory("lastModified"));

      this.treeTable.setTreeColumn(clauseNameColumn);
      this.searchPane.setCenter(new Label("Search View"));
      this.searchPane.setTop(treeTable);
   }

   //~--- methods -------------------------------------------------------------
   private void updateStyle(QueryClause item, boolean empty, TreeTableRow<QueryClause> ttr, Node nodeToStyle) {

      String style = "-fx-background-color:" + EMPTY_BACKGROUND_CSS_COLOR;

      if ((item == null) || empty) {
         ttr.setStyle(style);
         nodeToStyle.setStyle(style);
         outputStyleInfo("Empty: ", nodeToStyle);
      } else {
         ConceptSpecification clauseConcept = item.clauseProperty.get()
                 .getClauseConcept();

         if (clauseConcept.equals(TermAux.AND_QUERY_CLAUSE)) {
            if (ttr.isSelected()) {
               outputStyleInfo("and, selected: ", nodeToStyle);
               style = "-fx-background-color:" + GuiStyles.QUERY_AND_SELECTED_CSS_COLOR;
            } else {
               outputStyleInfo("and, not selected: ", nodeToStyle);
               style = "-fx-background-color:" + GuiStyles.QUERY_AND_CSS_COLOR;
            }
         } else if (clauseConcept.equals(TermAux.OR_QUERY_CLAUSE)) {
            if (ttr.isSelected()) {
               outputStyleInfo("or, selected: ", nodeToStyle);
               style = "-fx-background-color:" + GuiStyles.QUERY_OR_SELECTED_CSS_COLOR;
            } else {
               outputStyleInfo("or, not selected: ", nodeToStyle);
               style = "-fx-background-color:" + GuiStyles.QUERY_OR_CSS_COLOR;
            }
         } else {
            if (ttr.isSelected()) {
               outputStyleInfo("other, selected: ", nodeToStyle);
            } else {
               outputStyleInfo("other, not selected: ", nodeToStyle);
            }
            
         }

         //ttr.setStyle(style);
         //nodeToStyle.setStyle(style);
      }
   }

   private void outputStyleInfo(String prefix, Node nodeToStyle) {
      System.out.println(prefix + " css metadata: " + nodeToStyle.getCssMetaData());
      System.out.println(prefix + " style: " + nodeToStyle.getStyle());
      System.out.println(prefix + " style classes: " + nodeToStyle.getStyleClass());
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public Manifold getManifold() {
      return manifold;
   }

   //~--- set methods ---------------------------------------------------------
   @Override
   public void setParent(BorderPane parent) {
      parent.setCenter(searchPane);
   }

   //~--- get methods ---------------------------------------------------------
   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }

   //~--- inner classes -------------------------------------------------------
   public class ClauseStringConverter
           extends StringConverter<QueryClause> {

      HashMap<String, Constructor<Clause>> stringToClauseMap = new HashMap<>();

      //~--- constructors -----------------------------------------------------
      public ClauseStringConverter() {
         Arrays.stream(Clause.getAllClauses())
                 .forEach(
                         (clause) -> {
                            try {
                               String key = manifold.getPreferredDescriptionText(clause.getClauseConcept());

                               stringToClauseMap.put(key, (Constructor<Clause>) clause.getClass()
                                       .getConstructor());

                               QueryClause qc = new QueryClause(clause);

                               allClauseClauseList.add(qc);
                            } catch (NoSuchMethodException | SecurityException ex) {
                               throw new RuntimeException(ex);
                            }
                         });
      }

      //~--- methods ----------------------------------------------------------
      @Override
      public QueryClause fromString(String name) {
         try {
            return new QueryClause(stringToClauseMap.get(name).newInstance());
         } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException ex) {
            throw new RuntimeException();
         }
      }

      @Override
      public String toString(SearchView.QueryClause cq) {
         try {
            String key = manifold.getPreferredDescriptionText(cq.clauseProperty.get()
                    .getClauseConcept());

            stringToClauseMap.put(key, (Constructor<Clause>) cq.clauseProperty.get()
                    .getClass()
                    .getConstructor());
            return key;
         } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
         }
      }
   }

   public class QueryClause {

      SimpleObjectProperty<Clause> clauseProperty;
      SimpleStringProperty clauseName;

      //~--- constructors -----------------------------------------------------
      private QueryClause(Clause clause) {
         this.clauseProperty = new SimpleObjectProperty<>(this, "clauseProperty", clause);
         this.clauseName = new SimpleStringProperty(
                 this,
                 "clauseName",
                 manifold.getTaxonomyCoordinate().getPreferredDescriptionText(clause.getClauseConcept()));
         this.clauseProperty.addListener(
                 (ov, oldClause, newClause) -> {
                    this.clauseName.setValue(
                            manifold.getTaxonomyCoordinate()
                                    .getPreferredDescriptionText(newClause.getClauseConcept()));
                 });
      }

      //~--- methods ----------------------------------------------------------
      @Override
      public String toString() {
         return clauseName.get();
      }

      //~--- get methods ------------------------------------------------------
      public String getName() {
         return clauseName.getValue();
      }
   }
}
