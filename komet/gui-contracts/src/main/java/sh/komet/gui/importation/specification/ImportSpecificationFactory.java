package sh.komet.gui.importation.specification;

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

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */

@Service(name = "Import Specification View Provider",
        metadata = "fqn={Import specification panel (SOLOR)},uuid={de59f527-95f5-53ad-97fa-c57574f3befa}")
@Singleton
public class ImportSpecificationFactory implements ExplorationNodeFactory {

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.UNLINKED};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.IMPORT_SPECIFICATION_PANEL____SOLOR;
    }

    @Override
    public ExplorationNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences) {
        return new ImportSpecificationNode(viewProperties);
    }

    @Override
    public String getMenuText() {
        return "Create Import Specification";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.ICON_IMPORT.getIconographic();
    }
}
