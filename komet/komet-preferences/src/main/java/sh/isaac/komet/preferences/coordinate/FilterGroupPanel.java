package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class FilterGroupPanel extends AbstractPreferences {
    public FilterGroupPanel(IsaacPreferences preferencesNode, String groupName, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, groupName, viewProperties, kpc);
    }

    public FilterGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Filter"), viewProperties, kpc);
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
