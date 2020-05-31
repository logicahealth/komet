package sh.komet.gui.provider.concept.detail.panel;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;

import javax.inject.Singleton;

@Service(name = "Concept Detail Taxonomy-Linked Provider")
@Singleton
public class ConceptDetailTaxonomyLinkedPanelProviderFactory extends ConceptDetailPanelProviderFactory {
    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.CONCEPT_DETAILS_TAXONOMY_LINKED_PANEL____SOLOR;
    }

    @Override
    public String getMenuText() {
        return "Concept Details - Taxonomy Linked";
    }

    @Override
    public DetailNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        preferencesNode.put(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME,
                preferencesNode.get(ConceptDetailPanelNode.Keys.ACTIVITY_FEED_NAME, Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES.getGroupName()));

        return new ConceptDetailPanelNode(viewProperties, activityFeed, preferencesNode);
    }

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.NAVIGATION,
                ViewProperties.UNLINKED, ViewProperties.SEARCH};
    }

}
