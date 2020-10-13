package sh.komet.gui.control.manifold;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

public class ManifoldMenuModel {
    private final ChangeListener<ManifoldCoordinateImmutable> manifoldChangedListener = this::manifoldCoordinateChanged;
    private final ViewProperties viewProperties;
    private final Control baseControlToShowOverride;
    private final Shape baseControlGraphic;
    private String oldFill = null;
    public Menu getCoordinateMenu() {
        return coordinateMenu;
    }

    private final Menu coordinateMenu;


    public ManifoldMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride) {
        this(viewProperties, baseControlToShowOverride, new Menu("Coordinates", Iconography.COORDINATES.getStyledIconographic()));
    }

    public ManifoldMenuModel(ViewProperties viewProperties, Control baseControlToShowOverride, Menu coordinateMenu) {
        this.viewProperties = viewProperties;
        this.coordinateMenu = coordinateMenu;
        this.viewProperties.getManifoldCoordinate().addListener(this.manifoldChangedListener);
        FxGet.pathCoordinates().addListener((MapChangeListener<UuidStringKey, StampPathImmutable>) change -> updateManifoldMenu());

        this.baseControlToShowOverride = baseControlToShowOverride;
        if (baseControlToShowOverride instanceof Labeled) {
            Node graphic = ((Labeled)this.baseControlToShowOverride).getGraphic();
            if (graphic instanceof AnchorPane) {
                Node childZero = ((AnchorPane) graphic).getChildren().get(0);
                this.baseControlGraphic = (Shape) childZero;
            } else {
                baseControlGraphic = null;
            }
        } else {
            this.baseControlGraphic = null;
        }
        updateManifoldMenu();

    }


    private void manifoldCoordinateChanged(ObservableValue<? extends ManifoldCoordinateImmutable> observable,
                                           ManifoldCoordinateImmutable oldValue,
                                           ManifoldCoordinateImmutable newValue) {
        updateManifoldMenu();

    }

    public void updateManifoldMenu() {
        if (this.viewProperties.getManifoldCoordinate().hasOverrides()) {
            if (this.baseControlGraphic != null) {
                if (this.oldFill == null) {
                    this.oldFill = this.baseControlGraphic.getFill().toString().replace("0x", "#");
                }
                this.baseControlGraphic.setStyle("-fx-font-family: 'Material Design Icons'; -fx-font-size: 18.0; -icons-color: #ff9100;");
            } else {
                this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, #ff9100;");
            }
        } else {
            if (this.baseControlGraphic != null) {
                this.baseControlGraphic.setStyle("-fx-font-family: 'Material Design Icons'; -fx-font-size: 18.0; -icons-color: " +
                        this.oldFill + ";");
            } else {
                this.baseControlToShowOverride.setStyle("-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;");
            }
        }

        //this.manifoldMenu.setTooltip();

        Platform.runLater(() -> {
            this.coordinateMenu.getItems().clear();
            CoordinateMenuFactory.makeCoordinateDisplayMenu(this.viewProperties.getManifoldCoordinate(), this.coordinateMenu.getItems(),
                    this.viewProperties.getManifoldCoordinate());
        });
    }


}
