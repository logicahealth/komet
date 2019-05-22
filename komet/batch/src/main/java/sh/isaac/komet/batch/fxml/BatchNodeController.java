package sh.isaac.komet.batch.fxml;
/**
 * 'BatchNode.fxml' Controller Class
 */

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import sh.isaac.api.Get;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class BatchNodeController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane batchAnchor;

    @FXML
    private BorderPane batchBorderPane;

    private VersionTable versionTable;

    private Manifold manifold;

    private DropHelper dropHelper;

    @FXML
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'BatchNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'BatchNode.fxml'.";
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.versionTable = new VersionTable(manifold);
        this.versionTable.getRootNode().getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        DragAndDropRowFactory dragAndDropRowFactory = new DragAndDropRowFactory();
        this.versionTable.getRootNode().setRowFactory(dragAndDropRowFactory);

        this.dropHelper = new DropHelper(versionTable.getRootNode(),
                this::addIdentifiedObject, dragEvent -> true, dragAndDropRowFactory::isDragging);

        this.batchBorderPane.setCenter(this.versionTable.getRootNode());
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
        versionTable.getRootNode().getItems().clear();
    }

    @FXML
    void exportList(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify file for export");
        fileChooser.setInitialFileName("list-export.txt");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Solor"));
        final File exportFile = fileChooser.showSaveDialog(null);
        if (exportFile != null) {
            try {
                exportFile.createNewFile();
                try (FileWriter writer = new FileWriter(exportFile)) {
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
                bufferedReader.lines().forEach(uuidStr -> items.add(Get.observableChronology(UUID.fromString(uuidStr))));
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }

    }


}
