package sh.isaac.komet.batch;

import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.batch.fxml.TransactionViewNodeController;
import sh.isaac.komet.batch.iconography.PluginIcons;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;

import java.io.IOException;
import java.util.Optional;

import static sh.isaac.komet.batch.TransactionViewFactory.TRANSACTION_VIEW;

public class TransactionViewNode extends ExplorationNodeAbstract {

    final SimpleStringProperty title = new SimpleStringProperty(TRANSACTION_VIEW);
    final SimpleStringProperty toolTip = new SimpleStringProperty("View of items in a transaction.");
    final AnchorPane root;
    final TransactionViewNodeController controller;
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(PluginIcons.SCRIPT_ICON.getStyledIconographic());

    public TransactionViewNode(ViewProperties viewProperties, IsaacPreferences preferences) {
        super(viewProperties);
        try {
            // The manifold group specified in the preferences takes precedence.
            this.viewProperties = viewProperties;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/TransactionViewNode.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setViewProperties(viewProperties);

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
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        controller.close();
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
