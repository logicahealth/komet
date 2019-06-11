package sh.isaac.komet.batch;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.batch.iconography.PluginIcons;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.manifold.Manifold;

import javax.inject.Singleton;


@Service(name = "Batch view factory")
@Singleton
public class BatchViewFactory implements ExplorationNodeFactory<BatchViewNode> {

    public static final String LIST_VIEW = "List view";

    @Override
    public BatchViewNode createNode(Manifold manifold, IsaacPreferences nodePreferences) {
        return new BatchViewNode(manifold);
    }

    @Override
    public String getMenuText() {
        return LIST_VIEW;
    }

    @Override
    public Node getMenuIcon() {
        return PluginIcons.SCRIPT_ICON.getStyledIconographic();
    }

    @Override
    public Manifold.ManifoldGroup[] getDefaultManifoldGroups() {
        return new Manifold.ManifoldGroup[] {Manifold.ManifoldGroup.UNLINKED};
    }

    @Override
    public PanelPlacement getPanelPlacement() {
        return null;
    }

    @Override
    public ConceptSpecification getPanelType() {
        return null;
    }
}
