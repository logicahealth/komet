package sh.isaac.komet.batch.fxml;
/**
 * 'ListViewNode.fxml' Controller Class
 */

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.util.UUIDUtil;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.GraphAmalgamWithManifold;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ListViewNodeController implements ComponentList {

    private static final Logger LOG = LogManager.getLogger();
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
    private ChoiceBox<UuidStringKey> viewChoiceBox;


    private VersionTable versionTable;

    private Manifold manifold;

    private DropHelper dropHelper;

    private final UUID listId = UUID.randomUUID();
    private Manifold listManifold;

    @FXML
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        this.listName.setText("Unamed List");
        FxGet.addComponentList(this);
        this.listManifold = Manifold.get(Manifold.ManifoldGroup.LIST);

        this.viewChoiceBox.setItems(FxGet.graphConfigurationKeys());
        this.viewChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            GraphAmalgamWithManifold graphAmalgamWithManifold = FxGet.graphConfiguration(newValue);
            this.manifold = graphAmalgamWithManifold.getManifold();
            this.versionTable = new VersionTable(this.manifold);
            this.versionTable.setManifold(this.manifold);
        });
        this.viewChoiceBox.getSelectionModel().select(FxGet.defaultViewKey());
    }

    public ObservableList<ObservableChronology> getItemList() {
        return this.versionTable.getRootNode().getItems();
    }

    public void setManifold(Manifold manifoldToIgnore) {
        this.versionTable = new VersionTable(this.manifold);
        this.versionTable.getRootNode().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.versionTable.getRootNode().getSelectionModel().getSelectedItems().addListener(this::selectionChanged);

        DragAndDropRowFactory dragAndDropRowFactory = new DragAndDropRowFactory();
        this.versionTable.getRootNode().setRowFactory(dragAndDropRowFactory);

        this.dropHelper = new DropHelper(versionTable.getRootNode(),
                this::addIdentifiedObject, dragEvent -> true, dragAndDropRowFactory::isDragging);

        this.batchBorderPane.setCenter(this.versionTable.getRootNode());
        //TODO get the visible columns from preferences, and write them to preferences when changed...
        this.versionTable.setAuthorTimeColumnVisible(false);
        this.versionTable.setModulePathColumnVisible(false);
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
                for (ObservableChronology remitem : c.getRemoved()) {
                    manifold.manifoldSelectionProperty().remove(new ComponentProxy(remitem.getNid(), remitem.toUserString()));
                }
                for (ObservableChronology additem : c.getAddedSubList()) {
                    manifold.manifoldSelectionProperty().add(new ComponentProxy(additem.getNid(), additem.toUserString()));
                }
            }
        }
        // Check to make sure lists are equal in size/properly synchronized.
        if (manifold.manifoldSelectionProperty().get().size() != c.getList().size()) {
            // lists are out of sync, reset with fresh list.
            ComponentProxy[] selectedItems = new ComponentProxy[c.getList().size()];
            for (int i = 0; i < selectedItems.length; i++) {
                ObservableChronology component = c.getList().get(i);
                selectedItems[i] = new ComponentProxy(component.getNid(), component.toUserString());
            }
            manifold.manifoldSelectionProperty().setAll(selectedItems);
        }
    }

    private void addIdentifiedObject(IdentifiedObject object) {
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
        table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
        table.requestFocus();
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
        HashSet<Integer> uniqueComponents = new HashSet<>();
        for (int i = 0; i < getComponents().size(); i++) {
            ObservableChronology item = getComponents().get(i);
            if (uniqueComponents.contains(item.getNid())) {
                while (item != null && uniqueComponents.contains(item.getNid())) {
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


}
