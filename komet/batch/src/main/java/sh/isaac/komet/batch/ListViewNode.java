package sh.isaac.komet.batch;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import sh.isaac.komet.batch.fxml.ListViewNodeController;
import sh.isaac.komet.batch.iconography.PluginIcons;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;
import java.util.Optional;

import static sh.isaac.komet.batch.ListViewFactory.LIST_VIEW;

public class ListViewNode implements ExplorationNode {

    final Manifold manifold;
    final SimpleStringProperty title = new SimpleStringProperty(LIST_VIEW);
    final SimpleStringProperty toolTip = new SimpleStringProperty("List view to create batches of content for processing, export, or similar uses.");
    final AnchorPane root;
    final ListViewNodeController controller;

    public ListViewNode(Manifold manifold) {
        try {
            this.manifold = manifold;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ListViewNode.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setManifold(manifold);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
    public Node getMenuIcon() {
        return PluginIcons.SCRIPT_ICON.getStyledIconographic();
    }
}
