package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class FilterGroupPanel extends AbstractPreferences {
    public FilterGroupPanel(IsaacPreferences preferencesNode, String groupName, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, groupName, manifold, kpc);
    }

    public FilterGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Filter"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

        }
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() throws BackingStoreException {

    }
}
