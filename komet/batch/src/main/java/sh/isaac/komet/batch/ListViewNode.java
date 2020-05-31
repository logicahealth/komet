package sh.isaac.komet.batch;

import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.batch.fxml.ListViewNodeController;
import sh.isaac.komet.batch.iconography.PluginIcons;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;

import java.io.IOException;
import java.util.Optional;

public class ListViewNode extends ExplorationNodeAbstract {

    final SimpleStringProperty toolTip = new SimpleStringProperty("List view to create batches of content for processing, export, or similar uses.");
    final AnchorPane root;
    final ListViewNodeController controller;
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(PluginIcons.SCRIPT_ICON.getStyledIconographic());

    public ListViewNode(ViewProperties viewProperties, IsaacPreferences preferences) {
        super(viewProperties);
        try {
            // The manifold group specified in the preferences takes precedence.
            this.viewProperties = viewProperties;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ListViewNode.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setViewProperties(this.viewProperties, this.viewProperties.getActivityFeed(ViewProperties.LIST));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node getMenuIconGraphic() {
        return PluginIcons.SCRIPT_ICON.getStyledIconographic();
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void close() {
        controller.close();
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

}
