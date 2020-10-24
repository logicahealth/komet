package sh.isaac.komet.batch;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.batch.iconography.PluginIcons;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import jakarta.inject.Singleton;


@Service(name = "List view factory")
@Singleton
public class ListViewFactory implements ExplorationNodeFactory<ListViewNode> {

    public static final String LIST_VIEW = "List View";
    {
        FxGet.addComponentList(new AllConceptsList());
        FxGet.addComponentList(new AllComponentList());
    }

    @Override
    public ListViewNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences) {
        return new ListViewNode(viewProperties.makeOverride(), nodePreferences);
    }

    @Override
    public String getMenuText() {
        return LIST_VIEW;
    }

    @Override
    public Node getMenuIcon() {
        return PluginIcons.SCRIPT_ICON.getStyledIconographic();
    }

    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.LIST};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.COMPONENT_LIST_PANEL____SOLOR;
    }
}
