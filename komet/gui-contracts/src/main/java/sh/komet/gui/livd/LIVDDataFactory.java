package sh.komet.gui.livd;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;

import javax.inject.Singleton;


@Service(name = "LIVD Exploration View Provider")
@Singleton
public class LIVDDataFactory implements ExplorationNodeFactory {
    @Override
    public ExplorationNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
        return new LIVDDataNode(manifold);
    }

    @Override
    public String getMenuText() {
        return "LIVD Exploration View";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.ICON_EXPORT.getIconographic();
    }

    @Override
    public ManifoldGroup[] getDefaultManifoldGroups() {
        return new ManifoldGroup[] { ManifoldGroup.UNLINKED };
    }

    @Override
    public PanelPlacement getPanelPlacement() {
        return null;
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.EXPORT_SPECIFICATION_PANEL____SOLOR;
    }
}
