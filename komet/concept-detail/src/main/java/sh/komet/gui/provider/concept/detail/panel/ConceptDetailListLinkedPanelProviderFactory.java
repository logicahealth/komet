package sh.komet.gui.provider.concept.detail.panel;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNode;

import javax.inject.Singleton;

@Service(name = "Concept Detail List-Linked Provider")
@Singleton
public class ConceptDetailListLinkedPanelProviderFactory extends ConceptDetailPanelProviderFactory {
    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CONCEPT_DETAILS_LIST_VIEW_LINKED_PANEL____SOLOR;
    }

    @Override
    public String getMenuText() {
        return "Concept Details - List Linked";
    }

    @Override
    public DetailNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        // Check preferences...
        preferencesNode.put(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME,
                preferencesNode.get(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME, ViewProperties.LIST));

        return new ConceptDetailPanelNode(viewProperties, activityFeed, preferencesNode);
    }

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.LIST, ViewProperties.UNLINKED,
                ViewProperties.NAVIGATION, ViewProperties.SEARCH, ViewProperties.CONCEPT_BUILDER};
    }

}
