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



package sh.komet.gui.provider.concept.detail;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import java.util.ResourceBundle;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ObservableValue;

import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;


import sh.isaac.api.Get;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;

import sh.komet.gui.cell.TreeTableConceptCellFactory;
import sh.komet.gui.cell.TreeTableGeneralCellFactory;
import sh.komet.gui.cell.TreeTableTimeCellFactory;
import sh.komet.gui.cell.TreeTableWhatCellFactory;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

public class ConceptDetailController {
   private final TreeTableTimeCellFactory timeCellFactory = new TreeTableTimeCellFactory();
   @FXML  // ResourceBundle that was given to the FXMLLoader
   private ResourceBundle                                                              resources;
   @FXML  // URL location of the FXML file that was given to the FXMLLoader
   private URL                                                                         location;
   @FXML  // fx:id="conceptExtensionTreeTable"
   private TreeTableView<ObservableCategorizedVersion>                                 conceptExtensionTreeTable;
   @FXML  // fx:id="conceptExtensionWhat"
   private TreeTableColumn<?, ?>                                                       conceptExtensionWhat;
   @FXML  // fx:id="conceptExtensionGeneral"
   private TreeTableColumn<?, ?>                                                       conceptExtensionGeneral;
   @FXML  // fx:id="conceptExtensionStatus"
   private TreeTableColumn<?, ?>                                                       conceptExtensionStatus;
   @FXML  // fx:id="conceptExtensionTime"
   private TreeTableColumn<?, ?>                                                       conceptExtensionTime;
   @FXML  // fx:id="conceptExtensionAuthor"
   private TreeTableColumn<?, ?>                                                       conceptExtensionAuthor;
   @FXML  // fx:id="conceptExtensionModule"
   private TreeTableColumn<?, ?>                                                       conceptExtensionModule;
   @FXML  // fx:id="conceptExtensionPath"
   private TreeTableColumn<?, ?>                                                       conceptExtensionPath;
   @FXML  // fx:id="descriptionTreeTable"
   private TreeTableView<ObservableCategorizedVersion>                                 descriptionTreeTable;
   @FXML  // fx:id="descWhatColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> descWhatColumn;
   @FXML  // fx:id="descGeneralColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> descGeneralColumn;
   @FXML  // fx:id="descStatusColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer>                      descStatusColumn;
   @FXML  // fx:id="descTimeColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Long>                         descTimeColumn;
   @FXML  // fx:id="descAuthorColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer>                      descAuthorColumn;
   @FXML  // fx:id="descModuleColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer>                      descModuleColumn;
   @FXML  // fx:id="descPathColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer>                      descPathColumn;
   @FXML  // fx:id="statedParentPane"
   private BorderPane                                                                  statedParentPane;
   @FXML  // fx:id="inferredParentPane"
   private BorderPane                                                                  inferredParentPane;
   @FXML  // fx:id="topScroller"
   private ScrollPane                                                                  topScroller;
   private Manifold                                                                    manifold;
   private TreeTableConceptCellFactory                                                 conceptCellFactory;
   private TreeTableWhatCellFactory                                                    whatCellFactory;
   private TreeTableGeneralCellFactory generalCellFactory;

   //~--- methods -------------------------------------------------------------

   @FXML  // This method is called by the FXMLLoader when initialization is complete
   void initialize() {
      assert topScroller != null: "fx:id=\"topScroller\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionTreeTable != null:
             "fx:id=\"conceptExtensionTreeTable\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionWhat != null:
             "fx:id=\"conceptExtensionWhat\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionGeneral != null:
             "fx:id=\"conceptExtensionGeneral\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionStatus != null:
             "fx:id=\"conceptExtensionStatus\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionTime != null:
             "fx:id=\"conceptExtensionTime\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionAuthor != null:
             "fx:id=\"conceptExtensionAuthor\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionModule != null:
             "fx:id=\"conceptExtensionModule\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptExtensionPath != null:
             "fx:id=\"conceptExtensionPath\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descriptionTreeTable != null:
             "fx:id=\"descriptionTreeTable\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descWhatColumn != null:
             "fx:id=\"descWhatColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descGeneralColumn != null:
             "fx:id=\"descGeneralColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descStatusColumn != null:
             "fx:id=\"descStatusColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descTimeColumn != null:
             "fx:id=\"descTimeColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descAuthorColumn != null:
             "fx:id=\"descAuthorColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descModuleColumn != null:
             "fx:id=\"descModuleColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert descPathColumn != null:
             "fx:id=\"descPathColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert statedParentPane != null:
             "fx:id=\"statedParentPane\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert inferredParentPane != null:
             "fx:id=\"inferredParentPane\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      descriptionTreeTable.setTableMenuButtonVisible(true);
   }

   private void addChildren(TreeItem<ObservableCategorizedVersion> parent,
                            ObservableList<ObservableSememeChronology> children) {
      for (ObservableSememeChronology child: children) {
         TreeItem<ObservableCategorizedVersion>            parentToAddTo       = parent;
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = child.getCategorizedVersions(manifold);

         if (categorizedVersions.getLatestVersion()
                                .isPresent()) {
            TreeItem<ObservableCategorizedVersion> childTreeItem = new TreeItem<>(
                                                                       categorizedVersions.getLatestVersion().get());

            parentToAddTo.getChildren()
                         .add(childTreeItem);
            parentToAddTo = childTreeItem;

            for (ObservableCategorizedVersion contradictionVersion: categorizedVersions.getLatestVersion()
                  .contradictions()) {
               TreeItem<ObservableCategorizedVersion> contradictionTreeItem = new TreeItem<>(contradictionVersion);

               parentToAddTo.getChildren()
                            .add(contradictionTreeItem);
            }

            for (ObservableCategorizedVersion historicVersion: categorizedVersions.getHistoricVersions()) {
               TreeItem<ObservableCategorizedVersion> historicTreeItem = new TreeItem<>(historicVersion);

               parentToAddTo.getChildren()
                            .add(historicTreeItem);
            }
         }
      }
   }

   private void focusConceptChanged(ObservableValue<? extends ConceptChronology> observable,
                                    ConceptChronology oldValue,
                                    ConceptChronology newValue) {
      if (newValue == null) {
         descriptionTreeTable.setRoot(null);
      } else {
         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                                                                      .getObservableConceptChronology(
                                                                            newValue.getConceptSequence());
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions =
            observableConceptChronology.getCategorizedVersions(
                manifold);
         TreeItem<ObservableCategorizedVersion> root = new TreeItem<>(categorizedVersions.getLatestVersion().get());

         addChildren(root, observableConceptChronology.getDescriptions());
         descriptionTreeTable.setRoot(root);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public Manifold getManifold() {
      return manifold;
   }

   //~--- set methods ---------------------------------------------------------

   public void setManifold(Manifold manifold) {
      if ((this.manifold != null) && (this.manifold != manifold)) {
         throw new UnsupportedOperationException("Manifold previously set... " + manifold);
      }

      this.manifold = manifold;
      this.manifold.focusedConceptChronologyProperty()
                   .addListener(this::focusConceptChanged);
      this.conceptCellFactory = new TreeTableConceptCellFactory(manifold);
      this.whatCellFactory    = new TreeTableWhatCellFactory(manifold);
      generalCellFactory = new TreeTableGeneralCellFactory(manifold);
 
      
      descWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      descWhatColumn.setCellFactory(this.whatCellFactory::call);

      descGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      descGeneralColumn.setCellFactory(this.generalCellFactory::call);
      
      descStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
      descTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
      descTimeColumn.setCellFactory(this.timeCellFactory::call);
      descAuthorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("authorSequence"));
      descAuthorColumn.setCellFactory(this.conceptCellFactory::call);
      descModuleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("moduleSequence"));
      descModuleColumn.setCellFactory(this.conceptCellFactory::call);
      descPathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pathSequence"));
      descPathColumn.setCellFactory(this.conceptCellFactory::call);
   }

   //~--- get methods ---------------------------------------------------------

   public ScrollPane getTopScroller() {
      return topScroller;
   }
}

