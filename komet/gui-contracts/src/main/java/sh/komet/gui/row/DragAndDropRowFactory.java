package sh.komet.gui.row;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.drag.drop.DragHelper;

public class DragAndDropRowFactory implements
        Callback<TableView<IdentifiedObject>, TableRow<IdentifiedObject>> {

    boolean isDragging = false;

    @Override
    public TableRow<IdentifiedObject> call(TableView<IdentifiedObject> param) {
        TableRow<IdentifiedObject> row = new TableRow<>();
        new DragHelper(row,
                () -> row.getTableView().getItems().get(row.getIndex()),
                mouseEvent ->
                        row.getTableView().getItems().size() > row.getIndex() &&
                        row.getTableView().getItems().get(row.getIndex()) != null,
                this::setDragging);
        return row;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setDragging(Boolean dragging) {
        isDragging = dragging;
    }
}
