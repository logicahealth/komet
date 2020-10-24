package sh.isaac.komet.batch;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import sh.isaac.komet.batch.fxml.ActionNodeController;
import sh.komet.gui.control.property.ViewProperties;

public class ActionCell extends ListCell<ActionNodeController> {
    final ListView<ActionNodeController> actionListView;
    final ViewProperties viewProperties;

    public ActionCell(ListView<ActionNodeController> actionListView, ViewProperties viewProperties) {
        this.actionListView = actionListView;
        this.viewProperties = viewProperties;
    }

    @Override
    protected void updateItem(ActionNodeController item, boolean empty) {
        if (item != null) {
            item.setActionCell(this);
        }
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
        } else {
            this.setGraphic(item.getAnchorPane());
        }
    }

    @FXML
    public void delete(ActionEvent event) {
        actionListView.getItems().remove(this.getItem());
    }

    @FXML
    public void moveDown(ActionEvent event) {
        ObservableList<ActionNodeController> items = actionListView.getItems();
        ActionNodeController thisController = this.getItem();
        int index = items.indexOf(thisController);
        if (index >= 0 && index < items.size() - 1) {
            items.remove(index);
            items.add(index + 1, thisController);
        }
    }

    @FXML
    public void moveUp(ActionEvent event) {
        ObservableList<ActionNodeController> items = actionListView.getItems();
        ActionNodeController thisController = this.getItem();
        int index = items.indexOf(thisController);
        if (index >= 1) {
            items.remove(index);
            items.add(index - 1, thisController);
        }
    }
}
