package sh.isaac.komet.batch;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import sh.isaac.komet.batch.fxml.CompositeActionNodeController;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;
import java.util.Optional;

import static sh.isaac.komet.batch.CompositeActionFactory.ACTION_VIEW;


public class CompositeActionNode implements ExplorationNode {

    final Manifold manifold;
    final SimpleStringProperty title = new SimpleStringProperty(ACTION_VIEW);
    final SimpleStringProperty toolTip = new SimpleStringProperty("Action view to create composite actions");
    final AnchorPane root;
    final CompositeActionNodeController controller;
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.EDIT_PENCIL.getStyledIconographic());

    public CompositeActionNode(Manifold manifold) {
        try {
            this.manifold = manifold;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/CompositeActionNode.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setManifold(manifold);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return title;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTip;
    }

    @Override
    public Manifold getManifold() {
        return manifold;
    }

    @Override
    public Node getNode() {
        return root;
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
