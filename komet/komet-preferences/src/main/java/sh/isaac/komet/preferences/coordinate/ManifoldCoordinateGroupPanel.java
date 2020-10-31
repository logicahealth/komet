package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.coordinate.ManifoldCoordinateItemPanel.Keys.MANIFOLD_GROUP_UUID;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;
/*
@Deprecated There is no longer an association between ManifoldCoordinates and ActivityFeeds...
 */
@Deprecated
public class ManifoldCoordinateGroupPanel extends ParentPanel {

    public ManifoldCoordinateGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Manifold"), viewProperties, kpc);
        if (!initialized()) {
            // add each default manifold...
            // Activity feed loop removed. TODO delete this class?
            save();
        }
    }

    @Override
    protected Class getChildClass() {
        return ManifoldCoordinateItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}