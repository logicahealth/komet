package sh.komet.gui.importation.specification;

import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;

import java.io.IOException;
import java.util.Optional;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationNode extends ExplorationNodeAbstract {

    {
        titleProperty.setValue("Import Specification Builder");
        toolTipProperty.setValue("Import Specification Builder");
        menuIconProperty.setValue(Iconography.ICON_IMPORT.getIconographic());
    }
    private final SimpleBooleanProperty closeExplorationNodeProperty = new SimpleBooleanProperty(false);

    private final BorderPane borderPane;

    public ImportSpecificationNode(ViewProperties viewProperties) {
        super(viewProperties);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImportSpecificationPanel.fxml"));
            this.borderPane = loader.load();
            ImportSpecificationController importSpecificationController = loader.getController();
            importSpecificationController.setViewProperties(viewProperties);
            importSpecificationController.setCloseExplorationNodeProperty(this.closeExplorationNodeProperty);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Node getMenuIconGraphic() {
        return Iconography.ICON_IMPORT.getIconographic();
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public Node getNode() {
        return borderPane;
    }

    @Override
    public void close() {

    }

    @Override
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canClose() {
        return true;
    }

    @Override
    public void savePreferences() {

    }
}
