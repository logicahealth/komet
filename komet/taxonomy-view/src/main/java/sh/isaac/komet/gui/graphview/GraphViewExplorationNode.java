package sh.isaac.komet.gui.graphview;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;

import java.io.IOException;
import java.util.Optional;

import static sh.isaac.komet.gui.graphview.GraphViewExplorationNodeFactory.MENU_TEXT;

public class GraphViewExplorationNode extends ExplorationNodeAbstract {

    {
        titleProperty.setValue(MENU_TEXT);
        toolTipProperty.setValue("Multi-parent navigation view");
        menuIconProperty.setValue(Iconography.TAXONOMY_ICON.getIconographic());
    }
    private final Label titleLabel = new Label();

    final AnchorPane root;
    final MultiParentGraphViewController controller;

    public GraphViewExplorationNode(ViewProperties viewProperties, IsaacPreferences nodePreferences) {
        super(viewProperties);
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/gui/graphview/GraphView.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setProperties(viewProperties, nodePreferences);

            this.titleLabel.graphicProperty()
                    .bind(menuIconProperty);
            this.titleLabel.textProperty()
                    .bind(titleProperty);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.TAXONOMY_ICON.getIconographic();
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleLabel);
    }

    @Override
    public void revertPreferences() {

    }

    @Override
    public void savePreferences() {

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
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
