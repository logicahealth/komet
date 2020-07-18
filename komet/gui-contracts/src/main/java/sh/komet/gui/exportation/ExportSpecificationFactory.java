package sh.komet.gui.exportation;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNode;

import javax.inject.Singleton;

@Service(name = "Export Specification View Provider",
        metadata = "fqn={Export specification panel (SOLOR),uuid={04b4e66a-6cc0-596e-ae45-d181d23c1b69}")
@Singleton
public class ExportSpecificationFactory implements ExplorationNodeFactory {
    @Override
    public ExplorationNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        return new ExportSpecificationNode(viewProperties);
    }

    @Override
    public String getMenuText() {
        return "Create Export Specification";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.ICON_EXPORT.getIconographic();
    }

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.UNLINKED };
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.EXPORT_SPECIFICATION_PANEL____SOLOR;
    }
}
