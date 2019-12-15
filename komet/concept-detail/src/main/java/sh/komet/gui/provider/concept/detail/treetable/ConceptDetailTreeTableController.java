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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;

import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptChronology;
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
   private SimpleObjectProperty<Manifold> manifoldProperty = new SimpleObjectProperty<>();
   private final SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty(0);
   private TreeTableConceptCellFactory conceptCellFactory;
   private TreeTableWhatCellFactory whatCellFactory;
   private TreeTableGeneralCellFactory generalCellFactory;
   private TreeTableModulePathCellFactory modulePathCellFactory;
   private TreeTableAuthorTimeCellFactory authorTimeCellFactory;
   private final ListChangeListener<ComponentProxy> selectionChangedListener = c -> this.selectionChanged(c);

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

   //~--- get methods ---------------------------------------------------------
   public BorderPane getConceptDetailRootPane() {
      return conceptDetailRootPane;
   }

   public Manifold getManifold() {
      return this.manifoldProperty.get();
   }

   //~--- set methods ---------------------------------------------------------
   public void setManifoldProperty(SimpleObjectProperty<Manifold> manifoldProperty) {
      this.manifoldProperty = manifoldProperty;
      manifoldChanged(manifoldProperty, null, manifoldProperty.get());
      manifoldProperty.addListener(this::manifoldChanged);

      this.manifoldProperty.get().manifoldSelectionProperty()
              .addListener(this.selectionChangedListener);

      conceptStatusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("state"));
      conceptTimeColumn.setVisible(false);
      conceptTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("time"));
      conceptTimeColumn.setCellFactory(this.timeCellFactory::call);
      conceptAuthorColumn.setVisible(false);
      conceptAuthorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("authorSequence"));
      conceptModuleColumn.setVisible(false);
      conceptModuleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("moduleSequence"));
      conceptPathColumn.setVisible(false);
      conceptPathColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pathSequence"));


      selectionListChanged(this.manifoldProperty.get().manifoldSelectionProperty());
   }
   private void manifoldChanged(ObservableValue<? extends Manifold> manifoldProperty, Manifold oldManifold, Manifold newManifold) {

      if (oldManifold != null) {
         oldManifold.manifoldSelectionProperty().get().removeListener(this.selectionChangedListener);
      }
      newManifold.manifoldSelectionProperty().get().addListener(this.selectionChangedListener);
      this.conceptCellFactory = new TreeTableConceptCellFactory(newManifold);
      this.conceptAuthorColumn.setCellFactory(this.conceptCellFactory::call);
      this.conceptModuleColumn.setCellFactory(this.conceptCellFactory::call);
      this.conceptPathColumn.setCellFactory(this.conceptCellFactory::call);

      this.whatCellFactory = new TreeTableWhatCellFactory(newManifold);
      this.conceptWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      this.conceptWhatColumn.setCellFactory(this.whatCellFactory::call);

      this.generalCellFactory = new TreeTableGeneralCellFactory(newManifold);
      this.conceptGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      this.conceptGeneralColumn.setCellFactory(this.generalCellFactory::call);

      this.modulePathCellFactory = new TreeTableModulePathCellFactory(newManifold);
      this.conceptModulePathColumn.setCellValueFactory(this.modulePathCellFactory::getCellValue);
      this.conceptModulePathColumn.setCellFactory(this.modulePathCellFactory::call);

      this.authorTimeCellFactory = new TreeTableAuthorTimeCellFactory(newManifold);
      this.conceptAuthorTimeColumn.setCellValueFactory(this.authorTimeCellFactory::getCellValue);
      this.conceptAuthorTimeColumn.setCellFactory(this.authorTimeCellFactory::call);
   }

   private void selectionChanged(ListChangeListener.Change<? extends ComponentProxy> c) {
      selectionListChanged(c.getList());
   }

   private void selectionListChanged(ObservableList<? extends ComponentProxy> selectionList) {
      if (selectionList.isEmpty()) {
         conceptExtensionTreeTable.setRoot(null);
      } else {
         ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                 .getObservableConceptChronology(
                         selectionList.get(this.selectionIndexProperty.get()).getNid());
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                 = observableConceptChronology.getCategorizedVersions(
                 manifoldProperty.get());

         TreeItem<ObservableCategorizedVersion> assemblageRoot = new TreeItem<>(categorizedVersions.getLatestVersion().get());
         addChildren(assemblageRoot, observableConceptChronology.getObservableSemanticList().sorted(), true);
         conceptExtensionTreeTable.setRoot(assemblageRoot);
      }
   }


   private void addChildren(TreeItem<ObservableCategorizedVersion> parent,
                            ObservableList<ObservableSemanticChronology> children, boolean addSemantics) {
      for (ObservableSemanticChronology child : children) {
         TreeItem<ObservableCategorizedVersion> parentToAddTo = parent;
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = child.getCategorizedVersions(getManifold());

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

}
