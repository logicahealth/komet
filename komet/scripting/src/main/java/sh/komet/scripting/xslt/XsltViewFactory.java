package sh.komet.scripting.xslt;

import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.scripting.groovy.GroovyViewProvider;

public class XsltViewFactory implements ExplorationNodeFactory {

    @Override
    public ExplorationNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        XsltViewProvider xsltViewProvider = new XsltViewProvider(viewProperties);
        return xsltViewProvider;
    }

    @Override
    public String getMenuText() {
        return "XSLT";
    }

    @Override
    public Node getMenuIcon() {
        return Iconography.JAVASCRIPT.getIconographic();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.UNLINKED};
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.GROOVY_SCRIPTING_PANEL____SOLOR;
    }
}
