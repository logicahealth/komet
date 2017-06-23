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
package sh.komet.gui.search;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import sh.komet.gui.contract.ExplorationNode;
import sh.komet.gui.contract.Manifold;

/**
 *
 * @author kec
 */
public class SearchView implements ExplorationNode {

   public class QueryClause {
      
      public QueryClause(String name) {
         this.name = new SimpleStringProperty(name);
      }
      private StringProperty name;

      public void setName(String value) {
         nameProperty().set(value);
      }

      public String getName() {
         return nameProperty().get();
      }

      public StringProperty nameProperty() {         
         if (name == null) {
            name = new SimpleStringProperty(this, "name");
         }
         return name;         
      }
      
      private LongProperty lastModified;

      public void setLastModified(long value) {
         lastModifiedProperty().set(value);
      }

      public long getLastModified() {
         return lastModifiedProperty().get();
      }

      public LongProperty lastModifiedProperty() {
         if (lastModified == null) {
            lastModified = new SimpleLongProperty(this, "lastModified");
         }
         return lastModified;         
      }      
   }
   
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Search view");
   private final BorderPane searchPane = new BorderPane();
   private Manifold manifold;
   
   TreeTableView<QueryClause> treeTable = new TreeTableView<>();
   TreeItem<QueryClause> root = new TreeItem<>(new QueryClause("AND"));
   TreeTableColumn<QueryClause, String> fileNameCol = new TreeTableColumn<>("Filename");
   TreeTableColumn<QueryClause, Long> lastModifiedCol = new TreeTableColumn<>("Size");
   
   {
      treeTable.setRoot(root);
      treeTable.setEditable(true);
      root.getChildren().add(new TreeItem<>(new QueryClause("A")));
      root.getChildren().add(new TreeItem<>(new QueryClause("B")));
      
      treeTable.getColumns().setAll(fileNameCol, lastModifiedCol);
      //fileNameCol.setCellValueFactory(new ComboBoxTreeTableCell("name"));
      lastModifiedCol.setCellValueFactory(new TreeItemPropertyValueFactory("lastModified"));
   }
   
   public SearchView(Manifold manifold) {
      this.manifold = manifold;
      searchPane.setCenter(new Label("Search View"));
      searchPane.setTop(treeTable);
   }
   
   @Override
   public Manifold getManifold() {
      return manifold;
   }
   
   @Override
   public void setParent(BorderPane parent) {
      parent.setCenter(searchPane);
   }
   
   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }
}
