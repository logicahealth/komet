package sh.komet.gui.importation.specification;

import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;
import java.util.Optional;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationNode implements ExplorationNode {

    private final Manifold manifold;
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("Import Specification Builder");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Import Specification Builder");
    private final SimpleBooleanProperty closeExplorationNodeProperty = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.ICON_IMPORT.getIconographic());

    private final BorderPane borderPane;

    public ImportSpecificationNode(Manifold manifold) {
        try {
            this.manifold = manifold;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImportSpecification.fxml"));
            this.borderPane = loader.load();
            ImportSpecificationController importSpecificationController = loader.getController();
            importSpecificationController.setManifold(manifold);
            importSpecificationController.setCloseExplorationNodeProperty(this.closeExplorationNodeProperty);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return this.titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return this.toolTipProperty;
    }

    @Override
    public Manifold getManifold() {
        return this.manifold;
    }

    @Override
    public Node getNode() {
        return borderPane;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void savePreferences() {

    }

    @Override
    public ObjectProperty<Node> getMenuIconProperty() {
        return menuIconProperty;
    }
}
