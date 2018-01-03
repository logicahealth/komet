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
package sh.komet.assemblage.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.komet.gui.cell.treetable.TreeTableAuthorTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableConceptCellFactory;
import sh.komet.gui.cell.treetable.TreeTableGeneralCellFactory;
import sh.komet.gui.cell.treetable.TreeTableModulePathCellFactory;
import sh.komet.gui.cell.treetable.TreeTableTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableWhatCellFactory;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

/**
 *
 * @author kec
 */
public class AssemblageDetailController {

   private final TreeTableTimeCellFactory timeCellFactory = new TreeTableTimeCellFactory();
   @FXML // fx:id="assemblageDetailRootPane"
   private BorderPane assemblageDetailRootPane;
   @FXML  // ResourceBundle that was given to the FXMLLoader
   private ResourceBundle resources;
   @FXML  // URL location of the FXML file that was given to the FXMLLoader
   private URL location;
   @FXML  // fx:id="assemblageExtensionTreeTable"
   private TreeTableView<ObservableCategorizedVersion> assemblageExtensionTreeTable;
   @FXML  // fx:id="assemblageWhatColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> assemblageWhatColumn;
   @FXML  // fx:id="assemblageGeneralColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> assemblageGeneralColumn;
   @FXML  // fx:id="assemblageStatusColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Status> assemblageStatusColumn;
   @FXML  // fx:id="assemblageAuthorTimeColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> assemblageAuthorTimeColumn;
   @FXML  // fx:id="assemblageTimeColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Long> assemblageTimeColumn;
   @FXML  // fx:id="assemblageAuthorColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> assemblageAuthorColumn;
   @FXML  // fx:id="assemblageModulePathColumn"
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> assemblageModulePathColumn;
   @FXML  // fx:id="assemblageModuleColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> assemblageModuleColumn;
   @FXML  // fx:id="assemblagePathColumn"
   private TreeTableColumn<ObservableCategorizedVersion, Integer> assemblagePathColumn;
   private Manifold manifold;
   private TreeTableConceptCellFactory assemblageCellFactory;
   private TreeTableWhatCellFactory whatCellFactory;
   private TreeTableGeneralCellFactory generalCellFactory;
   private TreeTableModulePathCellFactory modulePathCellFactory;
   private TreeTableAuthorTimeCellFactory authorTimeCellFactory;

   //~--- methods -------------------------------------------------------------
   @FXML  // This method is called by the FXMLLoader when initialization is complete
   void initialize() {
      assert assemblageExtensionTreeTable != null :
              "fx:id=\"assemblageExtensionTreeTable\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageWhatColumn != null :
              "fx:id=\"assemblageExtensionWhat\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageGeneralColumn != null :
              "fx:id=\"assemblageExtensionGeneral\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageStatusColumn != null :
              "fx:id=\"assemblageExtensionStatus\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageTimeColumn != null :
              "fx:id=\"assemblageExtensionTime\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageAuthorColumn != null :
              "fx:id=\"assemblageExtensionAuthor\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageModulePathColumn != null : "fx:id=\"assemblageModulePathColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageAuthorTimeColumn != null : "fx:id=\"assemblageAuthorTimeColumn\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblageModuleColumn != null :
              "fx:id=\"assemblageExtensionModule\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assert assemblagePathColumn != null :
              "fx:id=\"assemblageExtensionPath\" was not injected: check your FXML file 'ConceptDetail.fxml'.";
      assemblageAuthorTimeColumn.setText("author\ntime");
      assemblageModulePathColumn.setText("module\npath");
      assemblageExtensionTreeTable.setTableMenuButtonVisible(true);
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
           ConceptSpecification oldValue,
           ConceptSpecification newValue) {
      if (newValue == null) {
         assemblageExtensionTreeTable.setRoot(null);
      } else {
         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         newValue.getNid());
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                 = observableConceptChronology.getCategorizedVersions(
                         manifold);

         TreeItem<ObservableCategorizedVersion> assemblageRoot = new TreeItem<>(categorizedVersions.getLatestVersion().get());
         ObservableList<ObservableSemanticChronology> children = FXCollections.observableArrayList();
         ObservableChronologyService observableChronologyService = Get.observableChronologyService();
         Get.assemblageService().getSemanticNidsFromAssemblage(observableConceptChronology.getNid())
                 .stream().forEach((sememeId) -> 
                 children.add(observableChronologyService.getObservableSemanticChronology(sememeId)));
         addChildren(assemblageRoot, children, true);
         assemblageExtensionTreeTable.setRoot(assemblageRoot);
      }
   }

   //~--- get methods ---------------------------------------------------------
   public BorderPane getAssemblageDetailRootPane() {
      return assemblageDetailRootPane;
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
      this.assemblageCellFactory = new TreeTableConceptCellFactory(manifold);
      this.whatCellFactory = new TreeTableWhatCellFactory(manifold);
      this.generalCellFactory = new TreeTableGeneralCellFactory(manifold);
      this.modulePathCellFactory = new TreeTableModulePathCellFactory(manifold);
      this.authorTimeCellFactory = new TreeTableAuthorTimeCellFactory(manifold);
      
      assemblageWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      assemblageWhatColumn.setCellFactory(this.whatCellFactory::call);
      assemblageGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      assemblageGeneralColumn.setCellFactory(this.generalCellFactory::call);
      assemblageStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
      assemblageTimeColumn.setVisible(false);
      assemblageTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
      assemblageTimeColumn.setCellFactory(this.timeCellFactory::call);
      assemblageAuthorColumn.setVisible(false);
      assemblageAuthorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("authorSequence"));
      assemblageAuthorColumn.setCellFactory(this.assemblageCellFactory::call);
      
      
      assemblageModulePathColumn.setCellValueFactory(this.modulePathCellFactory::getCellValue);
      assemblageModulePathColumn.setCellFactory(this.modulePathCellFactory::call);
      
      assemblageAuthorTimeColumn.setCellValueFactory(this.authorTimeCellFactory::getCellValue);
      assemblageAuthorTimeColumn.setCellFactory(this.authorTimeCellFactory::call);
      
      assemblageModuleColumn.setVisible(false);
      assemblageModuleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("moduleSequence"));
      assemblageModuleColumn.setCellFactory(this.assemblageCellFactory::call);
      assemblagePathColumn.setVisible(false);
      assemblagePathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pathSequence"));
      assemblagePathColumn.setCellFactory(this.assemblageCellFactory::call);
   }

}