package sh.komet.gui.provider.concept.detail.panel;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNode;

import jakarta.inject.Singleton;

@Service(name = "Concept Detail New-Concept-Linked Provider")
@Singleton
public class ConceptDetailNewConceptLinkedProviderFactory extends ConceptDetailPanelProviderFactory {
    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CONCEPT_DETAILS_NEW_CONCEPT_LINKED_PANEL____SOLOR;
    }

    @Override
    public String getMenuText() {
        return "Concept Details - New Concept Linked";
    }

    @Override
    public DetailNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        // Check preferences...
        preferencesNode.put(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME,
                preferencesNode.get(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME, ViewProperties.CONCEPT_BUILDER));

        return new ConceptDetailPanelNode(viewProperties.makeOverride(), activityFeed, preferencesNode);
    }

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.CONCEPT_BUILDER, ViewProperties.UNLINKED,
                ViewProperties.NAVIGATION, ViewProperties.SEARCH, ViewProperties.CONCEPT_BUILDER};
    }

}
