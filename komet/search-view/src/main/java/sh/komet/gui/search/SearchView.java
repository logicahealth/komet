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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;

//~--- JDK imports ------------------------------------------------------------

import javax.validation.constraints.NotNull;

//~--- non-JDK imports --------------------------------------------------------

import static javafx.scene.control.TreeTableView.CONSTRAINED_RESIZE_POLICY;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.And;
import sh.isaac.api.query.Clause;
import sh.isaac.api.query.Or;
import sh.isaac.api.query.clauses.ConceptIsChildOf;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;

import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.action.ConceptActionGroup;
import sh.komet.gui.contract.ExplorationNode;
import sh.komet.gui.contract.Manifold;
import sh.komet.gui.contract.StyleClasses;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class SearchView
         implements ExplorationNode {
   private final SimpleStringProperty                 toolTipProperty  = new SimpleStringProperty("search view");
   private final BorderPane                           searchPane       = new BorderPane();
   private final TreeTableView<QueryClause>           treeTable        = new TreeTableView<>();
   private final TreeTableColumn<QueryClause, String> clauseNameColumn = new TreeTableColumn<>("clause");
   private final TreeTableColumn<QueryClause, String> parameterColumn  = new TreeTableColumn<>("parameter");
   private final TreeItem<QueryClause>                root;
   private final Manifold                             manifold;

   //~--- constructors --------------------------------------------------------

   public SearchView(Manifold manifold) {
      this.manifold = manifold;
      this.root     = new TreeItem<>(new QueryClause(Clause.getRootClause()));
      this.treeTable.setRoot(root);
      this.treeTable.setShowRoot(false);
      this.treeTable.setEditable(true);

      TreeItem andTreeItem = new TreeItem<>(new QueryClause(new And()));

      andTreeItem.getChildren()
                 .add(new TreeItem<>(new QueryClause(new ConceptIsChildOf())));
      andTreeItem.getChildren()
                 .add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch())));
      andTreeItem.getChildren()
                 .add(new TreeItem<>(new QueryClause(new And())));
      andTreeItem.getChildren()
                 .add(new TreeItem<>(new QueryClause(new Or())));

      TreeItem orTreeItem = new TreeItem<>(new QueryClause(new Or()));

      orTreeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(new ConceptIsChildOf())));
      orTreeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(new DescriptionLuceneMatch())));
      orTreeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(new And())));
      orTreeItem.getChildren()
                .add(new TreeItem<>(new QueryClause(new Or())));
      this.root.getChildren()
               .add(andTreeItem);
      this.root.getChildren()
               .add(orTreeItem);
      this.treeTable.getColumns()
                    .setAll(clauseNameColumn, parameterColumn);
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

                   if (item != null) {
                      outputStyleInfo("update " + item, this);
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
      this.treeTable.setTreeColumn(clauseNameColumn);
      this.searchPane.setCenter(new Label("Search View"));
      this.searchPane.setTop(treeTable);
      this.treeTable.skinProperty()
                    .addListener(
                        (ObservableValue<? extends Skin<?>> observable,
                         Skin<?> oldValue,
                         Skin<?> newValue) -> {
                           if (newValue != null) {
                              System.out.println(
                                  "TreeTable skin changed: " + treeTable.getSkin().getClass().getCanonicalName());
                           }
                        });
      this.treeTable.columnResizePolicyProperty()
                    .set(CONSTRAINED_RESIZE_POLICY);
   }

   //~--- methods -------------------------------------------------------------

   private void outputStyleInfo(String prefix, TreeTableCell nodeToStyle) {
      // System.out.println(prefix + " css metadata: " + nodeToStyle.getCssMetaData());
      System.out.println(prefix + " style: " + nodeToStyle.getStyle());
      System.out.println(prefix + " style classes: " + nodeToStyle.getStyleClass());
   }

   private Collection<? extends Action> setupContextMenu(TreeTableRow<QueryClause> rowValue) {
      // Firstly, create a list of Actions
      ArrayList<Action>     actionList = new ArrayList();
      TreeItem<QueryClause> treeItem   = rowValue.getTreeItem();

      if (treeItem != null) {
         if (treeItem.getParent() != this.root) {
            Action deleteAction = new Action("delete");

            // deleteAction.setGraphic(GlyphFonts.fontAwesome().create('\uf013').color(Color.CORAL).size(28));
            actionList.add(deleteAction);
         }

         actionList.add(
             new ConceptActionGroup(
                 MetaData.AND_ǁQUERY_CLAUSEǁ,
                 new ConceptAction(MetaData.ACCEPTABLE_ǁISAACǁ),
                 new ConceptAction(MetaData.QUERY_CLAUSES_ǁISAACǁ)));
         actionList.add(
             new ConceptActionGroup(
                 MetaData.OR_ǁQUERY_CLAUSEǁ,
                 new ConceptAction(MetaData.ACCEPTABLE_ǁISAACǁ),
                 new ConceptAction(MetaData.QUERY_CLAUSES_ǁISAACǁ)));
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
            TreeItem<QueryClause> parentItem    = rowItem.getParent();
            ConceptSpecification  parentConcept = parentItem.getValue()
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

   public class QueryClause {
      SimpleObjectProperty<Clause> clauseProperty;
      SimpleStringProperty         clauseName;
      SimpleStringProperty         parameter;

      //~--- constructors -----------------------------------------------------

      private QueryClause(@NotNull Clause clause) {
         this.clauseProperty = new SimpleObjectProperty<>(this, "clauseProperty", clause);
         this.parameter      = new SimpleStringProperty(this, "parameter", "");
         this.clauseName = new SimpleStringProperty(
             this,
             "clauseName",
             manifold.getManifoldCoordinate().getPreferredDescriptionText(clause.getClauseConcept()));
         this.clauseProperty.addListener(
             (ov, oldClause, newClause) -> {
                this.clauseName.setValue(
                    manifold.getManifoldCoordinate()
                            .getPreferredDescriptionText(newClause.getClauseConcept()));
             });
      }

      //~--- methods ----------------------------------------------------------

      public SimpleStringProperty parameterProperty() {
         return parameter;
      }

      @Override
      public String toString() {
         return clauseName.get();
      }

      //~--- get methods ------------------------------------------------------

      public @NotNull
      Clause getClause() {
         return clauseProperty.get();
      }

      public String getName() {
         return clauseName.getValue();
      }
   }
}

