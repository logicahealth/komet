package sh.komet.gui.table.version;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.control.property.ViewProperties;

import java.io.IOException;
import java.net.URL;

public class VersionTable {
    final VersionTableController controller;
    final TableView<ObservableChronology> tableView;

    public VersionTable(ViewProperties viewProperties) {
        try {
            URL resource = VersionTableController.class.getResource("VersionTable.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            this.controller = loader.getController();
            this.tableView = loader.getRoot();
            this.controller.setViewProperties(viewProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setViewProperties(ViewProperties viewProperties) {
        controller.setViewProperties(viewProperties);
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

    public void setStatusColumnVisible(boolean value) {
        controller.setStatusColumnVisible(value);
    }

    public void setTimeColumnVisible(boolean value) {
        controller.setTimeColumnVisible(value);
    }

    public void setModulePathColumnVisible(boolean value) {
        controller.setModulePathColumnVisible(value);
    }

    public void setAuthorTimeColumnVisible(boolean value) {
        controller.setAuthorTimeColumnVisible(value);
    }

    public void setAuthorColumnVisible(boolean value) {
        controller.setAuthorColumnVisible(value);
    }

    public void setModuleColumnVisible(boolean value) {
        controller.setModuleColumnVisible(value);
    }

    public void setPathColumnVisible(boolean value) {
        controller.setPathColumnVisible(value);
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
