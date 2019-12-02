package sh.isaac.komet.batch.fxml;
/**
 * 'ListViewNode.fxml' Controller Class
 */

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.util.UUIDUtil;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class ListViewNodeController implements ComponentList {

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

    private VersionTable versionTable;

    private Manifold manifold;

    private DropHelper dropHelper;

    private final UUID listId = UUID.randomUUID();
    private Manifold listManifold;

    @FXML
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'ListViewNode.fxml'.";
        this.listName.setText("Unamed " + UUID.randomUUID().toString());
        FxGet.addComponentList(this);
        this.listManifold = Manifold.get(Manifold.ManifoldGroup.LIST);
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.versionTable = new VersionTable(manifold);
        this.versionTable.getRootNode().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.versionTable.getRootNode().getSelectionModel().getSelectedItems().addListener(this::selectionChanged);

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
                for (ObservableChronology remitem : c.getRemoved()) {
                    manifold.manifoldSelectionProperty().remove(new ComponentProxy(remitem.getNid(), remitem.toUserString()));
                }
                for (ObservableChronology additem : c.getAddedSubList()) {
                    manifold.manifoldSelectionProperty().add(new ComponentProxy(additem.getNid(), additem.toUserString()));
                }
            }
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
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Solor"));
        final File exportFile = fileChooser.showSaveDialog(null);
        if (exportFile != null) {
            try {
                exportFile.createNewFile();
                try (FileWriter writer = new FileWriter(exportFile)) {
                    writer.write(listName.getText() + "\n");
                    for (ObservableChronology item: versionTable.getRootNode().getItems()) {
                        writer.write(item.getPrimordialUuid().toString() + "\n");
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

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Solor"));
        File importFile = fileChooser.showOpenDialog(null);
        if (importFile != null) {
            try (FileReader reader = new FileReader(importFile); BufferedReader bufferedReader = new BufferedReader(reader)) {
                ObservableList<ObservableChronology> items = versionTable.getRootNode().getItems();
                listName.setText(importFile.getName().substring(0, importFile.getName().lastIndexOf(".")));
                items.clear();
                int lineCount = 0;
                bufferedReader.lines().forEach(lineString -> {
                    if (lineCount == 0 &! UUIDUtil.isUUID(lineString)) {
                        listName.setText(lineString);
                    } else {
                        items.add(Get.observableChronology(UUID.fromString(lineString)));
                    }
                });
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }

    }

    public void close() {
        System.out.println("Closing ListViewNodeController");
        FxGet.removeComponentList(this);
    }

    @Override
    public ObservableList<ObservableChronology> getComponents() {
        return versionTable.getRootNode().getItems();
    }

    @Override
    public StringProperty nameProperty() {
        return listName.textProperty();
    }

    @Override
    public UUID getListId() {
        return listId;
    }
}
