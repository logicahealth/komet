package sh.komet.gui.row;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.drag.drop.DragHelper;

public class DragAndDropRowFactory implements
        Callback<TableView<ObservableChronology>, TableRow<ObservableChronology>> {

    boolean isDragging = false;

    @Override
    public TableRow<ObservableChronology> call(TableView<ObservableChronology> param) {
        TableRow<ObservableChronology> row = new TableRow<>();
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
