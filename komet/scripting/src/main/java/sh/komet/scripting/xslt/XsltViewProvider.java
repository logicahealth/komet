package sh.komet.scripting.xslt;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.scripting.ScriptingController;

import java.io.IOException;
import java.util.Optional;

public class XsltViewProvider extends ExplorationNodeAbstract {
    {
        toolTipProperty.setValue("XSLT runner");
        super.getTitle().setValue("XSLT");
        menuIconProperty.setValue(Iconography.JAVASCRIPT.getIconographic());
    }
    private final ScriptingController scriptingController;
    private final Node titleNode = Iconography.JAVASCRIPT.getIconographic();


    public XsltViewProvider(ViewProperties viewProperties) {
        super(viewProperties);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sh/komet/scripting/scriptrunner.fxml"));
            loader.load();
            this.scriptingController = loader.getController();
            this.scriptingController.setEngine("XSLT");
            this.scriptingController.setScript("import sh.isaac.api.Get\n"
                    + "println(Get.conceptDescriptionText(-2147483643))");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.JAVASCRIPT.getIconographic();
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Node getNode() {
        return scriptingController.getTopPane();
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleNode);
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
