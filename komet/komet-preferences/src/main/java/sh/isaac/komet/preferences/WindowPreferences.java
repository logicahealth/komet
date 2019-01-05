package sh.isaac.komet.preferences;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class WindowPreferences extends ParentPanel {

    public WindowPreferences(IsaacPreferences preferencesNode,
                             Manifold manifold,
                             KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Window configurations"), manifold, kpc);
        if (!initialized()) {
            addChild("KOMET window", WindowPreferencePanel.class);
        }
        revert();
        save();
    }

    @Override
    protected Class getChildClass() {
        return WindowPreferencePanel.class;
    }

    @Override
    void saveFields() throws BackingStoreException {

    }

    @Override
    void revertFields() {

    }
}
