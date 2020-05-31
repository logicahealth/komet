package sh.komet.gui.exportation;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringPropertyBase;
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

public class ExportSpecificationNode extends ExplorationNodeAbstract {
    {
        titleProperty.setValue("Export specification builder");
        toolTipProperty.setValue("Export specification builder");
        menuIconProperty.setValue(Iconography.ICON_EXPORT.getIconographic());
    }

    private final BorderPane borderPane;

    public ExportSpecificationNode(ViewProperties viewProperties) {
        super(viewProperties);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExportSpecification.fxml"));
            this.borderPane = loader.load();
            ExportSpecificationController exportController = loader.getController();
            exportController.setViewProperties(viewProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.ICON_EXPORT.getIconographic();
    }

    @Override
    public void savePreferences() {

    }
    @Override
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
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
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
