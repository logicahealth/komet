package sh.isaac.komet.preferences.window;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.isaac.komet.preferences.personas.PersonasItemPanel;
import sh.isaac.komet.preferences.personas.PersonasItems;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class WindowsPanel extends ParentPanel implements WindowPreferences {

    public WindowsPanel(IsaacPreferences preferencesNode,
                        Manifold manifold,
                        KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Window configurations"), manifold, kpc);
        if (!initialized()) {
            IsaacPreferences windowPreferences = addChild("KOMET window", WindowPreferencePanel.class);
            WindowPreferencesItem windowPreferencesItem = PersonasItemPanel.createNewDefaultWindowPreferences(windowPreferences, manifold, kpc);
            windowPreferencesItem.save();
        }
        revert();
        save();
    }

    @Override
    protected Class getChildClass() {
        return WindowPreferencePanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    public IsaacPreferences addWindow() {
        return addChild(UUID.randomUUID().toString(), WindowPreferencePanel.class);
    }

    @Override
    protected void revertFields() {

    }
}
