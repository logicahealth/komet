package sh.komet.gui.cell.table;

import javafx.scene.control.TableRow;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

public class TableStatusCell extends KometTableCell {

    public TableStatusCell() {
        getStyleClass().add("komet-status-cell");
        getStyleClass().add("isaac-version");
    }

    @Override
    protected void updateItem(TableRow<ObservableChronology> row, ObservableVersion cellValue) {
        setText(cellValue.getStatus().toString());
    }

}