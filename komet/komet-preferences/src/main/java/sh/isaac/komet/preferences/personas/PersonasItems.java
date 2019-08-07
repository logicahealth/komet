package sh.isaac.komet.preferences.personas;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 2019-07-26
 * aks8m - https://github.com/aks8m
 */
public class PersonasItems extends ParentPanel {

    public PersonasItems(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Personas"), manifold, kpc);
        revertFields();
        save();
    }

    @Override
    protected Class getChildClass() {
        return PersonasItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}
