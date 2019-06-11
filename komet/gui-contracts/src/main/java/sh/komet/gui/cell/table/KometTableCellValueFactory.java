package sh.komet.gui.cell.table;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.manifold.Manifold;

public class KometTableCellValueFactory {
    private final Manifold manifold;

    //~--- constructors --------------------------------------------------------

    public KometTableCellValueFactory(Manifold manifold) {
        this.manifold = manifold;
    }

    //~--- get methods ---------------------------------------------------------

    public ObservableValue<ObservableVersion> getCellValue(
            TableColumn.CellDataFeatures<ObservableChronology,
                    ObservableVersion> param) {
        LatestVersion<ObservableVersion> version = param.getValue().getLatestObservableVersion(this.manifold);
        if (version.isPresent()) {
            return new SimpleObjectProperty<>(version.get());
        }
        return new SimpleObjectProperty<>();
    }
}
