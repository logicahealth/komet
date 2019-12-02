package sh.komet.gui.provider.classification;

import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.manifold.Manifold;

@Service(name = "Classification results provider")
@Singleton
public class ClassificationResultsProviderFactory implements ExplorationNodeFactory<ClassificationResultsNode> {
    @Override
    public ClassificationResultsNode createNode(Manifold manifold, IsaacPreferences nodePreferences) {
        return new ClassificationResultsNode(manifold);
    }

    @Override
    public String getMenuText() {
        return "Classification results";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.INFERRED.getIconographic();
    }

    @Override
    public Manifold.ManifoldGroup[] getDefaultManifoldGroups() {
        return new Manifold.ManifoldGroup[] {Manifold.ManifoldGroup.CLASSIFICATON};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CLASSIFICATION_RESULTS_PANEL____SOLOR;
    }
}
