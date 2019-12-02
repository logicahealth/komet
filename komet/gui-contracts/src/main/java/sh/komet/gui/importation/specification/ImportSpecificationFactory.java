package sh.komet.gui.importation.specification;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.constants.MetadataConceptConstant;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

import javax.inject.Singleton;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */

@Service(name = "Import Specification View Provider")
@Singleton
public class ImportSpecificationFactory implements ExplorationNodeFactory {

    @Override
    public Manifold.ManifoldGroup[] getDefaultManifoldGroups() {
        return new Manifold.ManifoldGroup[]{Manifold.ManifoldGroup.UNLINKED};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.IMPORT_SPECIFICATION_PANEL____SOLOR;
    }

    @Override
    public ExplorationNode createNode(Manifold manifold, IsaacPreferences nodePreferences) {
        return new ImportSpecificationNode(manifold);
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
