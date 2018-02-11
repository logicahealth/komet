/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.importation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.solor.rf2.direct.Rf2DirectImporter;
import static sh.komet.gui.importation.ImportItemZipEntry.FILE_PARENT_KEY;
import sh.isaac.solor.rf2.direct.ZipFileEntry;
public class ImportViewController {

    protected static final Logger LOG = LogManager.getLogger();

    @FXML
    private ChoiceBox<ImportType> importType;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addButton;

    @FXML
    private Button importDataButton;

    @FXML
    private TreeTableView<ImportItem> fileTreeTable;

    @FXML
    private TreeTableColumn<ImportItem, String> treeColumn;

    @FXML
    private TreeTableColumn<ImportItem, String> importColumn;

    Stage importStage;

    Map<TreeItem<ImportItem>, HashMap<String, TreeItem<ImportItem>>> fileItemsMap = new HashMap<>();

    @FXML
    void addImportDataLocation(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Zip files", "*.zip"));
        File selectedFile = fileChooser.showOpenDialog(importStage);
        if (selectedFile != null) {
            try {
                TreeItem<ImportItem> newFileItem = new TreeItem<>(new ImportItemZipFile(selectedFile));

                //this.fileTreeTable.getRoot().getChildren().add(newFileItem);
                ZipFile zipFile = new ZipFile(selectedFile, Charset.forName("UTF-8"));
                HashMap<String, TreeItem<ImportItem>> newTreeItems = new HashMap<>();
                fileItemsMap.put(newFileItem, newTreeItems);
                zipFile.stream().forEach((ZipEntry zipEntry) -> {
                    if (!zipEntry.getName().toUpperCase().contains("__MACOSX")
                            && !zipEntry.getName().contains("._")) {
                        ImportItemZipEntry importItem = new ImportItemZipEntry(selectedFile, zipEntry);
                        TreeItem<ImportItem> entryItem = new TreeItem<>(importItem);
                        newTreeItems.put(zipEntry.getName(), entryItem);
                    }
                });

                setupEntryTree();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected void setupEntryTree() {
        // clear all existing links
        fileTreeTable.getRoot().getChildren().clear();
        for (TreeItem<ImportItem> fileItem : fileItemsMap.keySet()) {
            fileItem.getChildren().clear();
            HashMap<String, TreeItem<ImportItem>> treeItems = fileItemsMap.get(fileItem);
            for (Map.Entry<String, TreeItem<ImportItem>> entry : treeItems.entrySet()) {
                entry.getValue().getChildren().clear();
            }
        }
        // hook all up here...
        for (TreeItem<ImportItem> fileItem : fileItemsMap.keySet()) {
            
            ImportType type = importType.getValue();
            HashMap<String, TreeItem<ImportItem>> treeItems = fileItemsMap.get(fileItem);
            
            for (Map.Entry<String, TreeItem<ImportItem>> entry : treeItems.entrySet()) {
                TreeItem<ImportItem> treeItem = entry.getValue();
                ImportItemZipEntry treeItemValue = (ImportItemZipEntry) treeItem.getValue();
                
                if (treeItemValue.importType == null || treeItemValue.importType == type ||
                        (type == ImportType.ACTIVE_ONLY && treeItemValue.importType == ImportType.SNAPSHOT)) {
                    if (treeItemValue.getParentKey().equals(FILE_PARENT_KEY)) {
                        fileTreeTable.getRoot().getChildren().add(treeItem);
                    } else {
                        TreeItem<ImportItem> parentItem = treeItems.get(treeItemValue.getParentKey());
                        if (parentItem != null) {
                            parentItem.getValue().importDataProperty().removeListener(treeItemValue);
                            parentItem.getValue().importDataProperty().addListener(treeItemValue);
                            parentItem.getChildren().add(treeItem);
                        } else {
                            LOG.error("Null parent for: " + treeItemValue);
                        }
                    }
                }
            }
            this.fileTreeTable.getRoot().expandedProperty().setValue(Boolean.TRUE);
        }
    }

    @FXML
    void importData(ActionEvent event) {
        List<ZipFileEntry> entriesToImport = new ArrayList<>();
        recursiveAddToImport(fileTreeTable.getRoot(), entriesToImport);
        
        sh.isaac.solor.rf2.direct.ImportType directImportType = null;
 ;
        switch (importType.getValue()) {
            case ACTIVE_ONLY:
                directImportType = sh.isaac.solor.rf2.direct.ImportType.ACTIVE_ONLY;
                break;
            case FULL:
                directImportType = sh.isaac.solor.rf2.direct.ImportType.FULL;
                break;
            case SNAPSHOT:
                directImportType = sh.isaac.solor.rf2.direct.ImportType.SNAPSHOT;
                break;
 
        }
        if (directImportType != null) {
            Rf2DirectImporter importer = new Rf2DirectImporter(directImportType, entriesToImport);
            Get.executor().execute(importer);
        }
        importStage.close();
    }
    
    private void recursiveAddToImport(TreeItem<ImportItem> treeItem, List<ZipFileEntry> entriesToImport) {
        ImportItem item = treeItem.getValue();
        if (item.importData()) {
            if (item instanceof ImportItemZipEntry) {
                ImportItemZipEntry zipEntry = (ImportItemZipEntry) item;
                if (!zipEntry.entry.isDirectory()) {
                    entriesToImport.add(new ZipFileEntry(zipEntry.zipFile, zipEntry.entry));
                }
            }
            for (TreeItem<ImportItem> childItem: treeItem.getChildren()) {
                recursiveAddToImport(childItem, entriesToImport);
            }
         }
    }

    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'ImportView.fxml'.";
        assert fileTreeTable != null : "fx:id=\"fileTreeTable\" was not injected: check your FXML file 'ImportView.fxml'.";
        assert treeColumn != null : "fx:id=\"treeColumn\" was not injected: check your FXML file 'ImportView.fxml'.";
        assert importColumn != null : "fx:id=\"importColumn\" was not injected: check your FXML file 'ImportView.fxml'.";
        assert importDataButton != null : "fx:id=\"importDataButton\" was not injected: check your FXML file 'ImportView.fxml'.";

        this.treeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));

        this.importColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("importData"));
        
        this.importColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(
                    (Integer index) -> this.fileTreeTable.getTreeItem(index).getValue().importDataProperty()));
        this.importColumn.setEditable(true);
        
        this.fileTreeTable.setRoot(new TreeItem<>(new ImportRoot()));
        this.fileTreeTable.setShowRoot(false);
        this.fileTreeTable.setEditable(true);
        this.fileTreeTable.treeColumnProperty().set(treeColumn);

        this.importType.getItems().addAll(ImportType.ACTIVE_ONLY, ImportType.SNAPSHOT, ImportType.FULL);
        this.importType.getSelectionModel().select(ImportType.ACTIVE_ONLY);
        this.importType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.importTypeChanged(newValue);
        });
    }

    private void importTypeChanged(ImportType importType) {
        setupEntryTree();
    }

    public Stage getImportStage() {
        return importStage;
    }

    public void setImportStage(Stage importStage) {
        this.importStage = importStage;
    }
}
