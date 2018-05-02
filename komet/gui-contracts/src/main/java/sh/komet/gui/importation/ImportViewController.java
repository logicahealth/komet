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

import static sh.komet.gui.importation.ImportItemZipEntry.FILE_PARENT_KEY;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import sh.isaac.api.Get;
import sh.isaac.api.util.StringUtils;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.direct.ImportType;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.FxUtils;

public class ImportViewController {

    protected static final Logger LOG = LogManager.getLogger();

    @FXML
    private ChoiceBox<SelectedImportType> importType;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addButton;

    @FXML
    private Button addArtifactButton;

    @FXML
    private Button importDataButton;

    @FXML
    private TreeTableView<ImportItem> fileTreeTable;

    @FXML
    private TreeTableColumn<ImportItem, String> treeColumn;

    @FXML
    private TreeTableColumn<ImportItem, String> importColumn;

    Stage importStage;

    Map<TreeItem<ImportItem>, ConcurrentHashMap<String, TreeItem<ImportItem>>> fileItemsMap = new ConcurrentHashMap<>();
    private Manifold manifold;

    private final StoredPrefs storedPrefs = new StoredPrefs("".toCharArray());

    @FXML
    void addImportDataLocation(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Zip files", "*.zip"));
        addFiles(fileChooser.showOpenMultipleDialog(importStage));
    }

    private void addFiles(List<File> files) {
        Task<Void> t = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (File file : files) {
                    try (ZipFile zipFile = new ZipFile(file, Charset.forName("UTF-8"))) {
                        TreeItem<ImportItem> newFileItem = new TreeItem<>(new ImportItemZipFile(file));
                        ConcurrentHashMap<String, TreeItem<ImportItem>> newTreeItems = new ConcurrentHashMap<>();
                        fileItemsMap.put(newFileItem, newTreeItems);
                        zipFile.stream().forEach((ZipEntry zipEntry) -> {
                            if (zipEntry.getName().toLowerCase().endsWith(".zip")) {
                                // maven artifact structure with nested zip file for actual content
                                try (ZipFile nestedZipFile = new ZipFile(file, Charset.forName("UTF-8"))) {
                                    ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(zipEntry), Charset.forName("UTF-8"));
                                    ZipEntry nestedEntry = zis.getNextEntry();
                                    while (nestedEntry != null) {
                                        if (!nestedEntry.getName().toUpperCase().contains("__MACOSX") && !nestedEntry.getName().contains("._")) {
                                            byte[] itemBytes = null;
                                            if (nestedEntry.getSize() < (500 * 1024 * 1024)) {
                                                //We have to cache these unzipped bytes, as otherwise, 
                                                //the import is terribly slow, because the java zip API only provides stream access
                                                //to nested files, and when you try to unzip from a stream, it can't jump ahead whe you 
                                                //call next entry, so you end up re-extracting the entire file for each file, which more 
                                                //that triples the load times.
                                                LOG.debug("Caching unzipped content");
                                                itemBytes = IOUtils.toByteArray(zis);
                                            } else {
                                                LOG.info("content file too large to cache");
                                            }
                                            ImportItemZipEntry nestedImportItem = new ImportItemZipEntry(file, zipEntry, nestedEntry, itemBytes);
                                            TreeItem<ImportItem> nestedEntryItem = new TreeItem<>(nestedImportItem);
                                            newTreeItems.put(zipEntry.getName() + "/" + nestedEntry.getName(), nestedEntryItem);
                                        }
                                        nestedEntry = zis.getNextEntry();
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                ImportItemZipEntry importItem = new ImportItemZipEntry(file, zipEntry);
                                TreeItem<ImportItem> entryItem = new TreeItem<>(importItem);
                                newTreeItems.put(zipEntry.getName(), entryItem);
                            } else if (!zipEntry.getName().toUpperCase().contains("__MACOSX") && !zipEntry.getName().contains("._")) {
                                if (file.getName().toLowerCase().startsWith("rxnorm_")) {
                                    if (zipEntry.getName().toLowerCase().endsWith(".rrf")) {
                                        ImportItemZipEntry importItem = new ImportItemZipEntry(file, zipEntry);
                                        TreeItem<ImportItem> entryItem = new TreeItem<>(importItem);
                                        if (importItem.nameProperty.get().toUpperCase().endsWith("RXNCONSO.RRF")
                                                && !importItem.parentKey.toLowerCase().contains("prescribe")) {
                                            importItem.importData.set(true);
                                        } else {
                                            importItem.importData.set(false);
                                        }

                                        newTreeItems.put(zipEntry.getName(), entryItem);
                                    }
                                } else if (file.getName().toLowerCase().startsWith("loinc_")) {
                                    if (zipEntry.getName().toLowerCase().equals("loinc.csv")) {
                                        ImportItemZipEntry importItem = new ImportItemZipEntry(file, zipEntry);
                                        TreeItem<ImportItem> entryItem = new TreeItem<>(importItem);
                                        newTreeItems.put(zipEntry.getName(), entryItem);
                                    }
                                } else {
                                    ImportItemZipEntry importItem = new ImportItemZipEntry(file, zipEntry);
                                    TreeItem<ImportItem> entryItem = new TreeItem<>(importItem);
                                    newTreeItems.put(zipEntry.getName(), entryItem);
                                }
                            }
                        });

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            Platform.runLater(() -> setupEntryTree());
            return null;
            }
        };
        Get.workExecutors().getExecutor().execute(t);
        FxUtils.waitWithProgress("Reading file", "Reading selected file", t, importStage.getOwner());

    }

    protected void setupEntryTree() {
        // clear all existing links
        fileTreeTable.getRoot().getChildren().clear();
        for (TreeItem<ImportItem> fileItem : fileItemsMap.keySet()) {
            fileItem.getChildren().clear();
            ConcurrentHashMap<String, TreeItem<ImportItem>> treeItems = fileItemsMap.get(fileItem);
            for (Map.Entry<String, TreeItem<ImportItem>> entry : treeItems.entrySet()) {
                entry.getValue().getChildren().clear();
            }
        }
        // hook all up here...
        for (TreeItem<ImportItem> fileItem : fileItemsMap.keySet()) {

            SelectedImportType type = importType.getValue();
            ConcurrentHashMap<String, TreeItem<ImportItem>> treeItems = fileItemsMap.get(fileItem);

            for (Map.Entry<String, TreeItem<ImportItem>> entry : treeItems.entrySet()) {
                TreeItem<ImportItem> treeItem = entry.getValue();
                if (treeItem.getValue() instanceof ImportItemZipEntry) {
                    ImportItemZipEntry treeItemValue = (ImportItemZipEntry) treeItem.getValue();

                    if (treeItemValue.importType == null || treeItemValue.importType == type
                            || (type == SelectedImportType.ACTIVE_ONLY && treeItemValue.importType == SelectedImportType.SNAPSHOT)) {
                        if (treeItemValue.getParentKey().equals(FILE_PARENT_KEY)) {
                            if (!fileTreeTable.getRoot().getChildren().contains(fileItem)) {
                                fileTreeTable.getRoot().getChildren().add(fileItem);
                            }
                            fileItem.getChildren().add(treeItem);
                        } else {
                            TreeItem<ImportItem> parentItem = treeItems.get(treeItemValue.getParentKey());
                            if (parentItem == null) {
                                // Add... In some zip files, the directories are not added, just the files. 
                                // So we may encounter a need for a parent directory
                                if (!fileTreeTable.getRoot().getChildren().contains(fileItem)) {
                                    fileTreeTable.getRoot().getChildren().add(fileItem);
                                }

                                ImportItemDirectory importItemDirectory = new ImportItemDirectory();
                                importItemDirectory.setName(treeItemValue.getParentKey());
                                TreeItem<ImportItem> directoryItem = new TreeItem<>(importItemDirectory);
                                fileItem.getChildren().add(directoryItem);
                                importItemDirectory.importData.set(treeItemValue.importData());
                                treeItems.put(treeItemValue.getParentKey(), directoryItem);
                                directoryItem.getChildren().add(treeItem);

                            } else {
                                parentItem.getValue().importDataProperty().removeListener(treeItemValue);
                                parentItem.getValue().importDataProperty().addListener(treeItemValue);
                                parentItem.getChildren().add(treeItem);
                                if (treeItemValue.importData()) {
                                    parentItem.getValue().importDataProperty().set(true);
                                }
                            }
                        }
                    }
                }
            }

            this.fileTreeTable.getRoot().expandedProperty().setValue(Boolean.TRUE);
        }
    }

    @FXML
    void importData(ActionEvent event) {
        List<ContentProvider> entriesToImport = new ArrayList<>();
        recursiveAddToImport(fileTreeTable.getRoot(), entriesToImport);
        ImportType directImportType = null;
        switch (importType.getValue()) {
            case ACTIVE_ONLY:
                directImportType = ImportType.ACTIVE_ONLY;
                break;
            case FULL:
                directImportType = ImportType.FULL;
                break;
            case SNAPSHOT:
                directImportType = ImportType.SNAPSHOT;
                break;
            case IGNORE:
                break;
            case DELTA:
            default:
                throw new RuntimeException("oops");

        }
        if (directImportType != null) {
            ImportSelectedAndTransformTask importer
                    = new ImportSelectedAndTransformTask(manifold, directImportType, entriesToImport);
            Get.executor().execute(importer);
        }
        importStage.close();
    }

    private void recursiveAddToImport(TreeItem<ImportItem> treeItem, List<ContentProvider> entriesToImport) {
        ImportItem item = treeItem.getValue();
        if (item.importData()) {
            if (item instanceof ImportItemZipEntry) {
                ImportItemZipEntry zipEntry = (ImportItemZipEntry) item;
                if (!zipEntry.entry.isDirectory()) {
                    entriesToImport.add(zipEntry.getContent());
                }
            }
            for (TreeItem<ImportItem> childItem : treeItem.getChildren()) {
                recursiveAddToImport(childItem, entriesToImport);
            }
        }
    }

    @FXML
    void initialize() {
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'ImportView.fxml'.";
        assert addArtifactButton != null : "fx:id=\"addArtifactButton\" was not injected: check your FXML file 'ImportView.fxml'.";
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

        if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
            this.importType.getItems().addAll(SelectedImportType.ACTIVE_ONLY, SelectedImportType.SNAPSHOT, SelectedImportType.FULL);
        } else {
            this.importType.getItems().addAll(SelectedImportType.ACTIVE_ONLY);
            this.addArtifactButton.setVisible(false);
        }
        
        this.importType.getSelectionModel().select(SelectedImportType.ACTIVE_ONLY);
        this.importType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.importTypeChanged(newValue);
        });

        ArrayList<SDOSourceContent> sdoSourceFiles_ = new ArrayList<>();

        this.addArtifactButton.setOnAction((action)
                -> {
            ListView<SDOSourceContent> sdoPicker = new ListView<>();

            FxUtils.waitWithProgress("Reading SDO Files", "Reading available SDO Source Files",
                    MavenArtifactUtils.readAvailableSourceFiles(storedPrefs, (results)
                            -> {
                        sdoSourceFiles_.clear();
                        //TODO tie this to some sort of dynamic thing about what types are supported by the direct importer...
                        for (SDOSourceContent sdo : results) {
                            SupportedConverterTypes found = SupportedConverterTypes.findBySrcArtifactId(sdo.getArtifactId());
                            if (SupportedConverterTypes.SCT == found || SupportedConverterTypes.SCT_EXTENSION == found) {
                                sdoSourceFiles_.add(sdo);
                            }
                        }

                    }), importStage.getScene().getWindow());

            sdoPicker.setItems(FXCollections.observableArrayList(sdoSourceFiles_));

            sdoPicker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sdoPicker.setCellFactory(param -> new ListCell<SDOSourceContent>() {
                @Override
                protected void updateItem(SDOSourceContent item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getArtifactId() + (item.hasClassifier() ? " : " + item.getClassifier() : "") + " : " + item.getVersion());
                    }
                }
            });

            Alert sdoDialog = new Alert(AlertType.CONFIRMATION);
            sdoDialog.setTitle("Select Files");
            sdoDialog.setHeaderText("Select 1 or more SDO Files to add");
            sdoDialog.getDialogPane().setContent(sdoPicker);
            sdoPicker.setPrefWidth(1024);
            sdoDialog.initOwner(importStage.getOwner());

            if (sdoDialog.showAndWait().orElse(null) == ButtonType.OK) {
                for (SDOSourceContent sdo : sdoPicker.getSelectionModel().getSelectedItems()) {
                    Optional<File> local = sdo.getLocalPath(storedPrefs);
                    if (local.isPresent()) {
                        
                        addFiles(Arrays.asList(new File[] { local.get() }));
                    }
                }
            }
        });

        //TODO tie this to a real StoredPrefs in the GUI.  For now, just a default, so we can at least read a local .m2 folder
        //make this system property read go away
        String temp = System.getProperty("M2_PATH");
        if (StringUtils.isNotBlank(temp)) {
            this.storedPrefs.setLocalM2FolderPath(temp);
        }
    }

    private void importTypeChanged(SelectedImportType importType) {
        setupEntryTree();
    }

    public Stage getImportStage() {
        return importStage;
    }

    public void setImportStage(Stage importStage) {
        this.importStage = importStage;
    }

    void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }
}
