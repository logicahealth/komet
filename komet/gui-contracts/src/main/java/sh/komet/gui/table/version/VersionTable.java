package sh.komet.gui.table.version;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.control.property.ViewProperties;

import java.io.IOException;
import java.net.URL;

public class VersionTable {
    final VersionTableController controller;
    final TableView<ObservableChronology> tableView;

    public VersionTable(ManifoldCoordinate manifoldCoordinate) {
        try {
            URL resource = VersionTableController.class.getResource("VersionTable.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            this.controller = loader.getController();
            this.tableView = loader.getRoot();
            this.controller.setManifoldCoordinate(manifoldCoordinate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setManifoldCoordinate(ManifoldCoordinate manifoldCoordinate) {
        controller.setManifoldCoordinate(manifoldCoordinate);
    }

    public VersionTableController getController() {
        return controller;
    }

    public TableView<ObservableChronology> getRootNode() {
        return tableView;
    }

    public void setWhatColumnVisible(boolean value) {
        controller.setWhatColumnVisible(value);
    }

    public boolean isWhatColumnVisible() {
        return controller.isWhatColumnVisible();
    }

    public void setStatusColumnVisible(boolean value) {
        controller.setStatusColumnVisible(value);
    }

    public boolean isStatusColumnVisible() {
        return controller.isStatusColumnVisible();
    }

    public void setTimeColumnVisible(boolean value) {
        controller.setTimeColumnVisible(value);
    }

    public boolean isTimeColumnVisible() {
        return controller.isTimeColumnVisible();
    }


    public void setModulePathColumnVisible(boolean value) {
        controller.setModulePathColumnVisible(value);
    }

    public boolean isModulePathColumnVisible() {
        return controller.isModulePathColumnVisible();
    }

    public void setAuthorTimeColumnVisible(boolean value) {
        controller.setAuthorTimeColumnVisible(value);
    }

    public boolean isAuthorTimeColumnVisible() {
        return controller.isAuthorTimeColumnVisible();
    }

    public void setAuthorColumnVisible(boolean value) {
        controller.setAuthorColumnVisible(value);
    }

    public boolean isAuthorColumnVisible() {
        return controller.isAuthorColumnVisible();
    }

    public void setModuleColumnVisible(boolean value) {
        controller.setModuleColumnVisible(value);
    }

    public boolean isModuleColumnVisible() {
        return controller.isModuleColumnVisible();
    }

    public void setPathColumnVisible(boolean value) {
        controller.setPathColumnVisible(value);
    }

    public boolean isPathColumnVisible() {
        return controller.isPathColumnVisible();
    }

    public void copySelectionToClipboard(Event event) {
        getController().copySelectionToClipboard(event);
    }

    public void pasteClipboard(Event event) {
        getController().pasteClipboard(event);
    }

    public void deleteSelectedItems(ActionEvent event) {
        getController().deleteSelectedItems(event);
    }
}
