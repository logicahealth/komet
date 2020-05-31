package sh.komet.gui.provider.classification;

import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;

import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.manifold.Manifold;

@Service(name = "Classification results provider")
@Singleton
public class ClassificationResultsProviderFactory implements ExplorationNodeFactory<ClassificationResultsNode> {
    @Override
    public ClassificationResultsNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences) {
        return new ClassificationResultsNode(viewProperties, activityFeed, nodePreferences);
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
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.CLASSIFICATION};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CLASSIFICATION_RESULTS_PANEL____SOLOR;
    }
}
