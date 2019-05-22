package sh.isaac.komet.preferences.window;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class WindowTabPanePreferencesPanel extends ParentPanel {
    public enum Keys {
        ITEM_NAME
    }

    public WindowTabPanePreferencesPanel(IsaacPreferences preferencesNode,
                                          Manifold manifold,
                                         KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Window tab configurations"), manifold, kpc);
    }

    @Override
    protected Class getChildClass() {
        return null;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() throws BackingStoreException {

    }
}
