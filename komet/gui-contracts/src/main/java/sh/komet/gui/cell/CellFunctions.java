package sh.komet.gui.cell;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.GridPane;
import sh.isaac.api.chronicle.VersionType;
import sh.komet.gui.control.FixedSizePane;
import sh.komet.gui.manifold.Manifold;

public interface CellFunctions {
    void search();

    void initializeConceptBuilder();

    Manifold getManifold();

    double getWidth();

    ReadOnlyDoubleProperty widthProperty();

    VersionType getVersionType();

    FixedSizePane getPaneForVersionDisplay();

    void setContentDisplay(ContentDisplay value);

    void setGraphic(Node value);

    GridPane getTextAndEditGrid();
}
