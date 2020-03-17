package sh.komet.gui.provider.concept.detail.panel;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
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
    public DetailNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
        preferencesNode.put(ConceptDetailPanelNode.Keys.MANIFOLD_GROUP_NAME,
                preferencesNode.get(ConceptDetailPanelNode.Keys.MANIFOLD_GROUP_NAME, Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES.getGroupName()));

        return new ConceptDetailPanelNode(manifold, preferencesNode);
    }

    @Override
    public Manifold.ManifoldGroup[] getDefaultManifoldGroups() {
        return new Manifold.ManifoldGroup[] {Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES,
                Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES,
                Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE,
                Manifold.ManifoldGroup.STATED_GRAPH_NAVIGATION_ANY_NODE,
                Manifold.ManifoldGroup.UNLINKED, Manifold.ManifoldGroup.SEARCH};
    }

}
