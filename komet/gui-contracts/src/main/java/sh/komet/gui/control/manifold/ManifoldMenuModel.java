package sh.komet.gui.control.manifold;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.util.UuidStringKey;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

public class ManifoldMenuModel {
    private final ChangeListener<ManifoldCoordinateImmutable> manifoldChangedListener = this::manifoldCoordinateChanged;
    private final ViewProperties viewProperties;
    private final Control baseControlToShowOverride;

    public Menu getCoordinateMenu() {
        return coordinateMenu;
    }

    private final Menu coordinateMenu;


    public ManifoldMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride) {
        this(viewProperties, baseControlToShowOverride, new Menu("Coordinates"));
    }

    public ManifoldMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride, Menu coordinateMenu) {
        this.viewProperties = viewProperties;
        this.coordinateMenu = coordinateMenu;
        this.viewProperties.getManifoldCoordinate().addListener(this.manifoldChangedListener);
        FxGet.pathCoordinates().addListener((MapChangeListener<UuidStringKey, StampPathImmutable>) change -> updateManifoldMenu());

        this.baseControlToShowOverride = baseControlToShowOverride;
        updateManifoldMenu();

    }


    private void manifoldCoordinateChanged(ObservableValue<? extends ManifoldCoordinateImmutable> observable,
                                           ManifoldCoordinateImmutable oldValue,
                                           ManifoldCoordinateImmutable newValue) {
        updateManifoldMenu();

    }

    public void updateManifoldMenu() {
        if (this.viewProperties.getManifoldCoordinate().hasOverrides()) {
            this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, orange;");
        } else {
            this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;");
        }
        //this.manifoldMenu.setTooltip();

        Platform.runLater(() -> {
            this.coordinateMenu.getItems().clear();
            CoordinateMenuFactory.makeCoordinateDisplayMenu(this.viewProperties.getManifoldCoordinate(), this.coordinateMenu.getItems(),
                    this.viewProperties.getManifoldCoordinate());
        });
    }


}
