package sh.isaac.komet.batch;

import javafx.scene.control.ListCell;
import sh.isaac.komet.batch.fxml.ActionNodeController;

public class ActionCell extends ListCell<ActionNodeController> {

    @Override
    protected void updateItem(ActionNodeController item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
        } else {
            this.setGraphic(item.getAnchorPane());
        }

    }
}
