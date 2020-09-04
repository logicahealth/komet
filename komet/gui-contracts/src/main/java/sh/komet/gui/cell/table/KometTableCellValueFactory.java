package sh.komet.gui.cell.table;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;

public class KometTableCellValueFactory {
    private final ManifoldCoordinate manifoldCoordinate;

    //~--- constructors --------------------------------------------------------

    public KometTableCellValueFactory(ManifoldCoordinate viewProperties) {
        this.manifoldCoordinate = viewProperties;
    }

    //~--- get methods ---------------------------------------------------------

    public ObservableValue<ObservableVersion> getCellValue(
            TableColumn.CellDataFeatures<ObservableChronology,
                    ObservableVersion> param) {
        LatestVersion<ObservableVersion> version = param.getValue().getLatestObservableVersion(this.manifoldCoordinate.getVertexStampFilter());
        if (version.isPresent()) {
            return new SimpleObjectProperty<>(version.get());
        }
        return new SimpleObjectProperty<>();
    }
}
