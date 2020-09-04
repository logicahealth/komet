package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class DigraphGroupPanel extends AbstractPreferences {

    public DigraphGroupPanel(IsaacPreferences preferencesNode, String groupName, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, groupName, viewProperties, kpc);
    }

    public DigraphGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Digraph"), viewProperties, kpc);
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
