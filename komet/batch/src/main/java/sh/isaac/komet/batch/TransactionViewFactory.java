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
import sh.komet.gui.manifold.Manifold;

import javax.inject.Singleton;

@Service(name = "Transaction view factory")
@Singleton
public class TransactionViewFactory implements ExplorationNodeFactory<TransactionViewNode> {

    public static final String TRANSACTION_VIEW = "Transaction View";

    @Override
    public TransactionViewNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences) {
        return new TransactionViewNode(viewProperties, nodePreferences);
    }

    @Override
    public String getMenuText() {
        return TRANSACTION_VIEW;
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
        return MetaData.TRANSACTION_LIST_PANEL____SOLOR;
    }
}
