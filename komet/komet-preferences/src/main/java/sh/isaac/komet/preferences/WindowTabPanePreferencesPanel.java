package sh.isaac.komet.preferences;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

public class WindowTabPanePreferencesPanel extends ParentPanel {
    public enum Keys {
        ITEM_NAME
    }

    public WindowTabPanePreferencesPanel(IsaacPreferences preferencesNode,
                                         String groupName, Manifold manifold,
                                         KometPreferencesController kpc) {
        super(preferencesNode, groupName, manifold, kpc);
    }

    @Override
    protected Class getChildClass() {
        return null;
    }

    @Override
    void saveFields() throws BackingStoreException {

    }

    @Override
    void revertFields() throws BackingStoreException {

    }
}
