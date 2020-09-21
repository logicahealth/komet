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

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.identity.IdentifiedObject;
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
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

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
   private ViewProperties viewProperties;
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
      assemblageExtensionTreeTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
   }

   private void addChildren(TreeItem<ObservableCategorizedVersion> parent,
           ObservableList<? extends ObservableChronology> children, boolean addSemantics) {
      for (ObservableChronology child : children) {
         TreeItem<ObservableCategorizedVersion> parentToAddTo = parent;
         CategorizedVersions<ObservableCategorizedVersion> categorizedVersions = child.getCategorizedVersions(viewProperties.getManifoldCoordinate().getViewStampFilter());

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
   public void updateFocus(IdentifiedObject focusObject, int count) {


      if (focusObject ==  null) {
         assemblageExtensionTreeTable.setRoot(null);
      } else {
         if (focusObject.getNid() == TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid()) {
            FxGet.dialogs().showInformationDialog("Solor concept assemblage selected",
                    "The Solor concept assemblage is to large for viewing.\n" +
                            "Please use the search and taxonomy to explore the Solor concepts");
         } else if (count > 100000) {
            FxGet.dialogs().showInformationDialog("More than 100,000 assemblage elements",
                    "This assemblage is to large for viewing.");
         } else {
               ObservableConceptChronology observableConceptChronology = Get.observableChronologyService()
                       .getObservableConceptChronology(focusObject.getNid());
               CategorizedVersions<ObservableCategorizedVersion> categorizedVersions
                       = observableConceptChronology.getCategorizedVersions(
                       viewProperties.getManifoldCoordinate().getViewStampFilter());

               TreeItem<ObservableCategorizedVersion> assemblageRoot = new TreeItem<>(categorizedVersions.getLatestVersion().get());
               ObservableList<ObservableChronology> children = FXCollections.observableArrayList();
               ObservableChronologyService observableChronologyService = Get.observableChronologyService();
               Get.identifierService().getNidsForAssemblage(focusObject.getNid(), false)
                       .forEach((nid) ->
                               children.add(observableChronologyService.getObservableChronology(nid)));
               addChildren(assemblageRoot, children, true);
               assemblageExtensionTreeTable.setRoot(assemblageRoot);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------
   public BorderPane getAssemblageDetailRootPane() {
      return assemblageDetailRootPane;
   }
   //~--- set methods ---------------------------------------------------------
   public void setViewProperties(ViewProperties viewProperties) {
      this.viewProperties = viewProperties;
      manifoldCoordinateChanged(this.viewProperties.getManifoldCoordinate(), null, this.viewProperties.getManifoldCoordinate());
      this.viewProperties.getManifoldCoordinate().addListener(this::manifoldCoordinateChanged);

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
      this.assemblageAuthorTimeColumn.setVisible(false);
      this.assemblageModulePathColumn.setVisible(false);
      this.assemblageWhatColumn.setVisible(false);
   }

   private void manifoldCoordinateChanged(ObservableValue<? extends ManifoldCoordinate> manifoldProperty, ManifoldCoordinate oldManifold, ManifoldCoordinate newManifold) {
      //@TODO maybe just refresh cells instead of creating new factories? This is leftover from change to
      // ViewProperties from Manifold.

      this.assemblageCellFactory = new TreeTableConceptCellFactory(this.viewProperties.getManifoldCoordinate());
      this.assemblageAuthorColumn.setCellFactory(this.assemblageCellFactory::call);
      this.assemblageModuleColumn.setCellFactory(this.assemblageCellFactory::call);

      this.whatCellFactory = new TreeTableWhatCellFactory(this.viewProperties.getManifoldCoordinate());
      this.assemblageWhatColumn.setCellValueFactory(this.whatCellFactory::getCellValue);
      this.assemblageWhatColumn.setCellFactory(this.whatCellFactory::call);

      this.generalCellFactory = new TreeTableGeneralCellFactory(this.viewProperties.getManifoldCoordinate());
      this.assemblageGeneralColumn.setCellValueFactory(this.generalCellFactory::getCellValue);
      this.assemblageGeneralColumn.setCellFactory(this.generalCellFactory::call);

      this.modulePathCellFactory = new TreeTableModulePathCellFactory(this.viewProperties.getManifoldCoordinate());
      this.assemblageModulePathColumn.setCellValueFactory(this.modulePathCellFactory::getCellValue);
      this.assemblageModulePathColumn.setCellFactory(this.modulePathCellFactory::call);

      this.authorTimeCellFactory = new TreeTableAuthorTimeCellFactory(this.viewProperties.getManifoldCoordinate());
      this.assemblageAuthorTimeColumn.setCellValueFactory(this.authorTimeCellFactory::getCellValue);
      this.assemblageAuthorTimeColumn.setCellFactory(this.authorTimeCellFactory::call);

      this.assemblageExtensionTreeTable.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<ObservableCategorizedVersion>>() {
         @Override
         public void onChanged(Change<? extends TreeItem<ObservableCategorizedVersion>> c) {
            ActivityFeed activityFeed = viewProperties.getActivityFeed(ViewProperties.LIST);
            if (activityFeed != null) {
               while (c.next()) {
                  if (c.wasPermutated()) {
                     for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //nothing to do...
                     }
                  } else if (c.wasUpdated()) {
                     //nothing to do
                  } else {
                     for (TreeItem<? extends ObservableCategorizedVersion> remitem : c.getRemoved()) {
                        if (remitem.getValue() != null) {
                           ObservableCategorizedVersion version = remitem.getValue();
                           activityFeed.feedSelectionProperty().remove(new ComponentProxy(version.getNid(), version.toUserString()));
                        }
                     }
                     for (TreeItem<ObservableCategorizedVersion> additem : c.getAddedSubList()) {
                        if (additem.getValue() != null) {
                           ObservableCategorizedVersion version = additem.getValue();
                           activityFeed.feedSelectionProperty().add(new ComponentProxy(version.getNid(), version.toUserString()));
                        }
                     }
                  }
               }
               // Check to make sure lists are equal in size/properly synchronized.
               if (activityFeed.feedSelectionProperty().size() != c.getList().size()) {
                  // lists are out of sync, reset with fresh list.
                  ComponentProxy[] selectedItems = new ComponentProxy[c.getList().size()];
                  for (int i = 0; i < selectedItems.length; i++) {
                     ObservableCategorizedVersion version = c.getList().get(i).getValue();
                     selectedItems[i] = new ComponentProxy(version.getNid(), version.toUserString());
                  }
                  activityFeed.feedSelectionProperty().setAll(selectedItems);
               }
            }
         }
      });
   }

   public TreeTableView<ObservableCategorizedVersion> getAssemblageExtensionTreeTable() {
      return assemblageExtensionTreeTable;
   }

}