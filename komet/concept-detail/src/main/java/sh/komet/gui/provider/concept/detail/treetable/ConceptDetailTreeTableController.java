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
package sh.komet.gui.provider.concept.detail.treetable;

//~--- JDK imports ------------------------------------------------------------
import java.net.URL;

import java.util.ResourceBundle;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.value.ObservableValue;

import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.komet.gui.cell.treetable.TreeTableAuthorTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableConceptCellFactory;
import sh.komet.gui.cell.treetable.TreeTableGeneralCellFactory;
import sh.komet.gui.cell.treetable.TreeTableModulePathCellFactory;
import sh.komet.gui.cell.treetable.TreeTableTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableWhatCellFactory;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

//~--- classes ----------------------------------------------------------------
public class ConceptDetailTreeTableController {

   private final TreeTableTimeCellFactory timeCellFactory = new TreeTableTimeCellFactory();
   @FXML // fx:id="conceptDetailRootPane"
   private BorderPane conceptDetailRootPane;
   @FXML  // ResourceBundle that was given to the FXMLLoader
   private ResourceBundle resources;
   @FXML  // URL location of the FXML file that was given to the FXMLLoader
   private URL location;
   @FXML  // fx:id="conceptExtensionTreeTable"
   private TreeTableView<ObservableCategorizedVersion> conceptExtensionTreeTable;
   @FXML  // fx:id="conceptWhatColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> conceptWhatColumn;
   @FXML  // fx:id="conceptGeneralColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> conceptGeneralColumn;
   @FXML  // fx:id="conceptStatusColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Status> conceptStatusColumn;
   @FXML  // fx:id="conceptAuthorTimeColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> conceptAuthorTimeColumn;
   @FXML  // fx:id="conceptTimeColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Long> conceptTimeColumn;
   @FXML  // fx:id="conceptAuthorColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> conceptAuthorColumn;
   @FXML  // fx:id="conceptModulePathColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> conceptModulePathColumn;
   @FXML  // fx:id="conceptModuleColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> conceptModuleColumn;
   @FXML  // fx:id="conceptPathColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> conceptPathColumn;
   private Manifold manifold;
   private TreeTableConceptCellFactory conceptCellFactory;
   private TreeTableWhatCellFactory whatCellFactory;
   private TreeTableGeneralCellFactory generalCellFactory;
   private TreeTableModulePathCellFactory modulePathCellFactory;
   private TreeTableAuthorTimeCellFactory authorTimeCellFactory;

   //~--- methods -------------------------------------------------------------
   @FXML  // This method is called by the FXMLLoader when initialization is complete
   void initialize() {
      assert conceptExtensionTreeTable != null :
              "fx:id=\"conceptExtensionTreeTable\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptWhatColumn != null :
              "fx:id=\"conceptExtensionWhat\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptGeneralColumn != null :
              "fx:id=\"conceptExtensionGeneral\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptStatusColumn != null :
              "fx:id=\"conceptExtensionStatus\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptTimeColumn != null :
              "fx:id=\"conceptExtensionTime\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptAuthorColumn != null :
              "fx:id=\"conceptExtensionAuthor\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptModulePathColumn != null : "fx:id=\"conceptModulePathColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptAuthorTimeColumn != null : "fx:id=\"conceptAuthorTimeColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptModuleColumn != null :
              "fx:id=\"conceptExtensionModule\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert conceptPathColumn != null :
              "fx:id=\"conceptExtensionPath\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      conceptAuthorTimeColumn.setText("author\ntime");
      conceptModulePathColumn.setText("module\npath");
      conceptExtensionTreeTable.setTableMenuButtonVisible(true);
   }

   private void addChildren(TreeItem<ObservableCategorizedVersion> parent,
           ObservableList<ObservableSemanticChronology> children, boolean addSememes) {
      for (ObservableSemanticChronology child : children) {
         TreeItem<ObservableCategorizedVersion> parentToAddTo = parent;
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = child.getCategorizedVersions(manifold);

         if (categorizedVersions.getLatestVersion()
                 .isPresent()) {
            TreeItem<ObservableCategorizedVersion> childTreeItem = new TreeItem<>(
                    categorizedVersions.getLatestVersion().get());

            parentToAddTo.getChildren()
                    .add(childTreeItem);
            parentToAddTo = childTreeItem;

            for (ObservableCategorizedVersion contradictionVersion : categorizedVersions.getLatestVersion()
                    .contradictions()) {
               TreeItem<ObservableCategorizedVersion> contradictionTreeItem = new TreeItem<>(contradictionVersion);

               parentToAddTo.getChildren()
                       .add(contradictionTreeItem);
            }

            for (ObservableCategorizedVersion historicVersion : categorizedVersions.getHistoricVersions()) {
               TreeItem<ObservableCategorizedVersion> historicTreeItem = new TreeItem<>(historicVersion);

               parentToAddTo.getChildren()
                       .add(historicTreeItem);
            }
            if (addSememes) {
               addChildren(childTreeItem, child.getObservableSemanticList(), addSememes);
            }
         }
      }
   }

   private void focusConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
           ConceptSpecification oldSpecification,
           ConceptSpecification newSpecification) {
      ConceptChronology newValue = Get.concept(newSpecification);
      if (newValue == null) {
         conceptExtensionTreeTable.setRoot(null);
      } else {
         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         newValue.getNid());
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                 = observableConceptChronology.getCategorizedVersions(
                         manifold);
          if (categorizedVersions.getLatestVersion().isPresent()) {
            TreeItem<ObservableCategorizedVersion> conceptRoot = new TreeItem<>(categorizedVersions.getLatestVersion().get());
            addChildren(conceptRoot, observableConceptChronology.getObservableSemanticList(), true);
            conceptExtensionTreeTable.setRoot(conceptRoot);
          } else {
             throw new IllegalStateException("Latest version is null: " + categorizedVersions);
          }
      }
   }

   //~--- get methods ---------------------------------------------------------
   public BorderPane getConceptDetailRootPane() {
      return conceptDetailRootPane;
   }

   public Manifold getManifold() {
      return manifold;
   }

   //~--- set methods ---------------------------------------------------------
   public void setManifold(Manifold manifold) {
      if ((this.manifold != null) && (this.manifold != manifold)) {
         throw new UnsupportedOperationException("Manifold previously set... " + manifold);
      }

      this.manifold = manifold;
      this.manifold.focusedConceptProperty()
              .addListener(this::focusConceptChanged);
      this.conceptCellFactory = new TreeTableConceptCellFactory(manifold);
      this.whatCellFactory = new TreeTableWhatCellFactory(manifold);
      this.generalCellFactory = new TreeTableGeneralCellFactory(manifold);
      this.modulePathCellFactory = new TreeTableModulePathCellFactory(manifold);
      this.authorTimeCellFactory = new TreeTableAuthorTimeCellFactory(manifold);
      
      conceptWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      conceptWhatColumn.setCellFactory(this.whatCellFactory::call);
      conceptGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      conceptGeneralColumn.setCellFactory(this.generalCellFactory::call);
      conceptStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
      conceptTimeColumn.setVisible(false);
      conceptTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
      conceptTimeColumn.setCellFactory(this.timeCellFactory::call);
      conceptAuthorColumn.setVisible(false);
      conceptAuthorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("authorSequence"));
      conceptAuthorColumn.setCellFactory(this.conceptCellFactory::call);
      
      
      conceptModulePathColumn.setCellValueFactory(this.modulePathCellFactory::getCellValue);
      conceptModulePathColumn.setCellFactory(this.modulePathCellFactory::call);
      
      conceptAuthorTimeColumn.setCellValueFactory(this.authorTimeCellFactory::getCellValue);
      conceptAuthorTimeColumn.setCellFactory(this.authorTimeCellFactory::call);
      
      conceptModuleColumn.setVisible(false);
      conceptModuleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("moduleSequence"));
      conceptModuleColumn.setCellFactory(this.conceptCellFactory::call);
      conceptPathColumn.setVisible(false);
      conceptPathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pathSequence"));
      conceptPathColumn.setCellFactory(this.conceptCellFactory::call);
   }
}
