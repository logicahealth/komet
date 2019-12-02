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
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.komet.gui.cell.treetable.TreeTableAuthorTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableConceptCellFactory;
import sh.komet.gui.cell.treetable.TreeTableGeneralCellFactory;
import sh.komet.gui.cell.treetable.TreeTableModulePathCellFactory;
import sh.komet.gui.cell.treetable.TreeTableTimeCellFactory;
import sh.komet.gui.cell.treetable.TreeTableWhatCellFactory;
import sh.komet.gui.manifold.Manifold;

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
   @FXML
   private TreeTableColumn<ObservableCategorizedVersion, ObservableCategorizedVersion> rowExpanderColumn;
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
   private SimpleObjectProperty<Manifold> manifoldProperty;
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
      assert rowExpanderColumn != null : "fx:id=\"rowExpanderColumn\" was not injected: check your FXML file 'AssemblageDetail.fxml'.";

      assemblageAuthorTimeColumn.setText("author\ntime");
      assemblageModulePathColumn.setText("module\npath");
      assemblageExtensionTreeTable.setTableMenuButtonVisible(true);
   }

   private void addChildren(TreeItem<ObservableCategorizedVersion> parent,
           ObservableList<? extends ObservableChronology> children, boolean addSemantics) {
      for (ObservableChronology child : children) {
         TreeItem<ObservableCategorizedVersion> parentToAddTo = parent;
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = child.getCategorizedVersions(manifoldProperty.get());

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
            if (addSemantics) {
                
               addChildren(childTreeItem, child.getObservableSemanticList(), addSemantics);
            }
         }
      }
   }
   private void selectionChanged(ListChangeListener.Change<? extends ComponentProxy> c) {


      if (c.getList().isEmpty()) {
         assemblageExtensionTreeTable.setRoot(null);
      } else {
         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         //TODO handle list properly...
                         c.getList().get(0).getNid());
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                 = observableConceptChronology.getCategorizedVersions(
                 manifoldProperty.get());

         TreeItem<ObservableCategorizedVersion> assemblageRoot = new TreeItem<>(categorizedVersions.getLatestVersion().get());
         ObservableList<ObservableChronology> children = FXCollections.observableArrayList();
         ObservableChronologyService observableChronologyService = Get.observableChronologyService();
         //TODO handle list properly...
         Get.identifierService().getNidsForAssemblage(c.getList().get(0).getNid())
                 .forEach((nid) ->
                         children.add(observableChronologyService.getObservableChronology(nid)));
         addChildren(assemblageRoot, children, true);
         assemblageExtensionTreeTable.setRoot(assemblageRoot);
      }
   }

   //~--- get methods ---------------------------------------------------------
   public BorderPane getAssemblageDetailRootPane() {
      return assemblageDetailRootPane;
   }
   //~--- set methods ---------------------------------------------------------
   public void setManifoldProperty(SimpleObjectProperty<Manifold> manifoldProperty) {


      this.manifoldProperty = manifoldProperty;
      manifoldChanged(manifoldProperty, null, manifoldProperty.get());
      manifoldProperty.addListener(this::manifoldChanged);

      this.assemblageStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
      this.assemblageTimeColumn.setVisible(false);
      this.assemblageTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
      this.assemblageTimeColumn.setCellFactory(this.timeCellFactory::call);
      this.assemblageAuthorColumn.setVisible(false);
      this.assemblageAuthorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("authorSequence"));

      this.assemblageModuleColumn.setVisible(false);
      this.assemblageModuleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("moduleSequence"));
      this.assemblagePathColumn.setVisible(false);
      this.assemblagePathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pathSequence"));
      this.assemblagePathColumn.setCellFactory(this.assemblageCellFactory::call);
   }

   private void manifoldChanged(ObservableValue<? extends Manifold> manifoldProperty, Manifold oldManifold, Manifold newManifold) {

      if (oldManifold != null) {
         oldManifold.manifoldSelectionProperty().get().removeListener(this::selectionChanged);
      }
       newManifold.manifoldSelectionProperty().get().addListener(this::selectionChanged);
      this.assemblageCellFactory = new TreeTableConceptCellFactory(newManifold);
      this.assemblageAuthorColumn.setCellFactory(this.assemblageCellFactory::call);
      this.assemblageModuleColumn.setCellFactory(this.assemblageCellFactory::call);


      this.whatCellFactory = new TreeTableWhatCellFactory(newManifold);
      this.assemblageWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      this.assemblageWhatColumn.setCellFactory(this.whatCellFactory::call);

      this.generalCellFactory = new TreeTableGeneralCellFactory(newManifold);
      this.assemblageGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      this.assemblageGeneralColumn.setCellFactory(this.generalCellFactory::call);

      this.modulePathCellFactory = new TreeTableModulePathCellFactory(newManifold);
      this.assemblageModulePathColumn.setCellValueFactory(this.modulePathCellFactory::getCellValue);
      this.assemblageModulePathColumn.setCellFactory(this.modulePathCellFactory::call);

      this.authorTimeCellFactory = new TreeTableAuthorTimeCellFactory(newManifold);
      this.assemblageAuthorTimeColumn.setCellValueFactory(this.authorTimeCellFactory::getCellValue);
      this.assemblageAuthorTimeColumn.setCellFactory(this.authorTimeCellFactory::call);
   }

}