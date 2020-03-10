package sh.isaac.komet.gui.graphview;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import java.io.IOException;
import java.util.Optional;

import static sh.isaac.komet.gui.graphview.GraphViewExplorationNodeFactory.MENU_TEXT;

public class GraphViewExplorationNode implements ExplorationNode {

    private final SimpleStringProperty titleProperty = new SimpleStringProperty(MENU_TEXT);
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Multi-parent navigation view");
    private final Label titleLabel = new Label();
    private final SimpleObjectProperty<Node> iconProperty = new SimpleObjectProperty<>(
            Iconography.TAXONOMY_ICON.getIconographic());

    final AnchorPane root;
    final MultiParentGraphViewController controller;

    public GraphViewExplorationNode(Manifold manifold, IsaacPreferences nodePreferences) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/gui/graphview/GraphView.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setPreferences(nodePreferences);

            this.titleLabel.graphicProperty()
                    .bind(iconProperty);
            this.titleLabel.textProperty()
                    .bind(titleProperty);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }
    @Override
    public void savePreferences() {

    }

    @Override
    public SimpleObjectProperty<Node> getMenuIconProperty() {
        return iconProperty;
    }
    @Override
    public Manifold getManifold() {
        return this.controller.getManifold();
    }

    @Override
    public Node getNode() {
        return root;
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
