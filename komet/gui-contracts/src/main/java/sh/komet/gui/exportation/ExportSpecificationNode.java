package sh.komet.gui.exportation;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;
import java.util.Optional;

public class ExportSpecificationNode implements ExplorationNode {
    private final Manifold manifold;
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("Export specification builder");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Export specification builder");
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.ICON_EXPORT.getIconographic());

    private final BorderPane borderPane;

    public ExportSpecificationNode(Manifold manifold) {
        try {
            this.manifold = manifold;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExportSpecification.fxml"));
            this.borderPane = loader.load();
            ExportSpecificationController exportController = loader.getController();
            exportController.setManifold(manifold);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void savePreferences() {

    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public Manifold getManifold() {
        return manifold;
    }

    @Override
    public Node getNode() {
        return borderPane;
    }

    @Override
    public SimpleObjectProperty getMenuIconProperty() {
        return menuIconProperty;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
