package sh.isaac.komet.batch;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.manifold.Manifold;

import javax.inject.Singleton;

@Service(name = "Composite action factory")
@Singleton
public class CompositeActionFactory implements ExplorationNodeFactory<CompositeActionNode> {

    public static final String ACTION_VIEW = "Composite Action";

    @Override
    public CompositeActionNode createNode(Manifold manifold, IsaacPreferences nodePreferences) {
        return new CompositeActionNode(manifold);
    }

    @Override
    public String getMenuText() {
        return ACTION_VIEW;
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.EDIT_PENCIL.getStyledIconographic();
    }

    @Override
    public Manifold.ManifoldGroup[] getDefaultManifoldGroups() {
        return new Manifold.ManifoldGroup[] {Manifold.ManifoldGroup.UNLINKED};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.COMPOSITE_ACTION_PANEL____SOLOR;
    }
}
