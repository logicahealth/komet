package sh.isaac.komet.batch.fxml;
/**
 * 'ListViewNode.fxml' Controller Class
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.komet.batch.AddConceptsInModule;
import sh.komet.gui.control.manifold.CoordinateMenuFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;

public class ListViewNodeController implements ComponentList {
    private static final Logger LOG = LogManager.getLogger();
    public static final int MAX_PROPAGATION_SIZE = 10;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane batchAnchor;

    @FXML
    private BorderPane batchBorderPane;

    @FXML
    private TextField listName;

    @FXML
    private Menu navigationMenu;

    @FXML
    private Menu addConceptsInModuleMenu;

    @FXML
    private Menu addConceptsOnPathMenu;

    @FXML
    private Menu addConceptsByAuthorMenu;

    @FXML
    private MenuItem listCount;

    private VersionTable versionTable;

    private ViewProperties viewProperties;
    private DropHelper dropHelper;

    private final UUID listId = UUID.randomUUID();
    private ActivityFeed activityFeed;

    private final ListChangeListener<ObservableChronology> listChangeListener = this::listChanged;
    private final ListChangeListener<ObservableChronology> selectionChangedListener = this::selectionChanged;

    @FXML
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        this.listName.setText("Unnamed List");
        FxGet.addComponentList(this);
    }

    public ObservableList<ObservableChronology> getItemList() {
        return this.versionTable.getRootNode().getItems();
    }

    private void listChanged(ListChangeListener.Change<? extends ObservableChronology> change) {
        listCount.setText("Count: " + change.getList().size());
    }



    public void setViewProperties(ViewProperties viewProperties, ActivityFeed activityFeed) {
        this.viewProperties = viewProperties;
        this.activityFeed = activityFeed;
        this.viewProperties.getManifoldCoordinate().addListener(observable -> {
            if (this.versionTable != null) {
                this.versionTable.getRootNode().getItems().removeListener(this.listChangeListener);
                this.versionTable.getRootNode().getSelectionModel().getSelectedItems().removeListener(this.selectionChangedListener);
            }
            VersionTable oldVersionTable = this.versionTable;

            setupVersionTable();

            //TODO get the visible columns from preferences, and write them to preferences when changed...
            this.versionTable.setAuthorTimeColumnVisible(oldVersionTable.isAuthorTimeColumnVisible());
            this.versionTable.setModulePathColumnVisible(oldVersionTable.isModulePathColumnVisible());
            this.versionTable.setWhatColumnVisible(oldVersionTable.isWhatColumnVisible());
            this.versionTable.setStatusColumnVisible(oldVersionTable.isStatusColumnVisible());
            this.versionTable.setTimeColumnVisible(oldVersionTable.isTimeColumnVisible());
            this.versionTable.setAuthorColumnVisible(oldVersionTable.isAuthorColumnVisible());
            this.versionTable.setModuleColumnVisible(oldVersionTable.isModuleColumnVisible());
            this.versionTable.setPathColumnVisible(oldVersionTable.isPathColumnVisible());

            this.versionTable.getRootNode().getItems().setAll(oldVersionTable.getRootNode().getItems());
        });


        setupVersionTable();
        //TODO get the visible columns from preferences, and write them to preferences when changed...
        this.versionTable.setAuthorTimeColumnVisible(false);
        this.versionTable.setModulePathColumnVisible(false);

        Get.stampService().getModulesInUse().forEach(moduleNid -> {
            MenuItem menuItem = new MenuItem(this.viewProperties.getPreferredDescriptionText(moduleNid));
            addConceptsInModuleMenu.getItems().add(menuItem);
            menuItem.setOnAction(event -> {
                Get.executor().submit(new AddConceptsInModule(moduleNid, viewProperties,
                        this));
            });
        });
        addConceptsInModuleMenu.getItems().sort((o1, o2) -> o1.getText().compareTo(o2.getText()));

        Get.stampService().getPathsInUse().forEach(moduleNid -> {
            //addConceptsInPathMenu
        });

        Get.stampService().getAuthorsInUse().forEach(moduleNid -> {
            //addConceptsForAuthorsMenu
        });

    }

    private void setupVersionTable() {
        this.navigationMenu.getItems().clear();
        CoordinateMenuFactory.makeCoordinateDisplayMenu(this.viewProperties.getManifoldCoordinate(),
                this.navigationMenu.getItems(), this.viewProperties.getManifoldCoordinate());
        this.versionTable = new VersionTable(this.viewProperties.getManifoldCoordinate());
        this.versionTable.getRootNode().getItems().addListener(this.listChangeListener);
        this.versionTable.getRootNode().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.versionTable.getRootNode().getSelectionModel().getSelectedItems().addListener(this.selectionChangedListener);
        DragAndDropRowFactory dragAndDropRowFactory = new DragAndDropRowFactory();
        this.versionTable.getRootNode().setRowFactory(dragAndDropRowFactory);
        this.dropHelper = new DropHelper(versionTable.getRootNode(),
                this::addIdentifiedObject, dragEvent -> true, dragAndDropRowFactory::isDragging);

        this.batchBorderPane.setCenter(this.versionTable.getRootNode());
    }

    private void selectionChanged(ListChangeListener.Change<? extends ObservableChronology> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //nothing to do...
                }
            } else if (c.wasUpdated()) {
                //nothing to do
            } else {
                if (!c.getRemoved().isEmpty()) {
                    List<? extends ObservableChronology> removed = c.getRemoved();
                    if (removed.size() > MAX_PROPAGATION_SIZE) {
                        this.activityFeed.feedSelectionProperty().clear();
                        LOG.info("Suppressing selection remove propagation of size: " + c.getRemoved().size());
                    } else {
                        this.activityFeed.feedSelectionProperty().removeAll(removed);
                    }

                }
                if (!c.getAddedSubList().isEmpty()) {
                    List<? extends ObservableChronology> added = c.getAddedSubList();
                    if (added.size() > MAX_PROPAGATION_SIZE) {
                        added = added.subList(0, MAX_PROPAGATION_SIZE - 1);
                        LOG.info("Reducing selection add propagation of size: " + c.getAddedSubList().size() +
                                " to size: " + added.size());
                    }
                    this.activityFeed.feedSelectionProperty().addAll(added);
                }
            }
        }
        // Check to make sure lists are equal in size/properly synchronized.
        if (c.getList().size() <= MAX_PROPAGATION_SIZE && this.activityFeed.feedSelectionProperty().get().size() != c.getList().size()) {
            // lists are out of sync, reset with fresh list.
            this.activityFeed.feedSelectionProperty().setAll(c.getList());
        }
    }


    @FXML
    void clearList(ActionEvent event) {
        listName.setText("untitled list");
        listName.selectAll();
        listName.requestFocus();
        versionTable.getRootNode().getItems().clear();
    }

    @FXML
    void exportList(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify file for export");
        if (!listName.getText().isEmpty()) {
            fileChooser.setInitialFileName(listName.getText() + ".txt");
        } else {
            fileChooser.setInitialFileName("list-export.txt");
        }
        fileChooser.setInitialDirectory(FxGet.solorDirectory());
        final File exportFile = fileChooser.showSaveDialog(null);
        if (exportFile != null) {
            try {
                exportFile.createNewFile();
                try (FileWriter writer = new FileWriter(exportFile)) {
                    writer.write(listName.getText() + "\n");
                    for (ObservableChronology item: versionTable.getRootNode().getItems()) {
                        writer.write(item.getPrimordialUuid().toString() + "\t" + item.toUserString() + "\n");
                    }
                    writer.flush();
                }
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }
    }

    @FXML
    void importList(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify file for import");
        fileChooser.setInitialFileName("list-export.txt");

        fileChooser.setInitialDirectory(FxGet.solorDirectory());
        File importFile = fileChooser.showOpenDialog(null);
        if (importFile != null) {
            try (FileReader reader = new FileReader(importFile); BufferedReader bufferedReader = new BufferedReader(reader)) {
                ObservableList<ObservableChronology> items = versionTable.getRootNode().getItems();
                List<ObservableChronology> newList = new ArrayList<>();
                listName.setText(importFile.getName().substring(0, importFile.getName().lastIndexOf(".")));
                items.clear();
                final AtomicInteger lineCount = new AtomicInteger(0);
                bufferedReader.lines().forEach(lineString -> {
                    String[] columns = lineString.split("\t");
                    if (lineCount.getAndIncrement() == 0 &! UUIDUtil.isUUID(lineString)) {
                        listName.setText(lineString);
                    } else {
                        newList.add(Get.observableChronology(UUID.fromString(columns[0])));
                        //items.add(Get.observableChronology(UUID.fromString(lineString)));
                    }
                });
                Platform.runLater(() -> items.setAll(newList));
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }

    }

    @FXML
    void deleteSelectedItems(ActionEvent event) {
        versionTable.deleteSelectedItems(event);
    }
    @FXML
    void copyItems(ActionEvent event) {
        versionTable.copySelectionToClipboard(event);
    }

    @FXML
    void pasteItems(ActionEvent event) {
        versionTable.pasteClipboard(event);
    }

    @FXML
    void deDupe(ActionEvent event) {
        MutableIntSet uniqueComponents = IntSets.mutable.empty();
        MutableList<ObservableChronology> duplicates = Lists.mutable.empty();
        for (int i = 0; i < getComponents().size(); i++) {
            ObservableChronology item = getComponents().get(i);
            if (uniqueComponents.contains(item.getNid())) {
                while (item != null && uniqueComponents.contains(item.getNid())) {
                    duplicates.add(item);
                    getComponents().remove(i);
                    if (i < getComponents().size()) {
                        item = getComponents().get(i);
                    } else {
                        item = null;
                    }
                }
                if (item != null) {
                    uniqueComponents.add(item.getNid());
                }

            } else {
                uniqueComponents.add(item.getNid());
            }
        }
        FxGet.statusMessageService().reportStatus("Removed " + duplicates.size() + " duplicates");
    }


    public void close() {
        LOG.debug("Closing ListViewNodeController");
        FxGet.removeComponentList(this);
    }

    @Override
    public Stream<Chronology> getComponentStream() {
        return versionTable.getRootNode().getItems().stream().map(observableChronology -> (Chronology) observableChronology);
    }

    @Override
    public Optional<ObservableList<ObservableChronology>>  getOptionalObservableComponentList() {
        return Optional.of(versionTable.getRootNode().getItems());
    }

    private ObservableList<ObservableChronology> getComponents() {
        return versionTable.getRootNode().getItems();
    }
    @Override
    public int listSize() {
        return versionTable.getRootNode().getItems().size();
    }

    @Override
    public StringProperty nameProperty() {
        return listName.textProperty();
    }

    @Override
    public UuidStringKey getUuidStringKey() {
        return new UuidStringKey(listId, nameProperty().getValue());
    }

    public void addIdentifiedObject(IdentifiedObject object) {
        if (object == null) {
            return;
        }
        ObservableChronology chronology;
        if (object instanceof ObservableChronology) {
            chronology = (ObservableChronology) object;
        } else {
            chronology = Get.observableChronology(object.getNid());
        }
        TableView<ObservableChronology> table = versionTable.getRootNode();
        table.getItems().add(chronology);
    }

    public void addIdentifiedObjects(MutableList<IdentifiedObject> objectsToAdd) {
        objectsToAdd.forEach(identifiedObject -> {
            addIdentifiedObject(identifiedObject);
        });
    }
}
