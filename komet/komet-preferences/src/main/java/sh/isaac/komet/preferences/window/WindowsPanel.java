package sh.isaac.komet.preferences.window;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.isaac.komet.preferences.personas.PersonaItemPanel;
import sh.komet.gui.contract.preferences.WindowsParentPreferences;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.manifold.Manifold;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class WindowsPanel extends ParentPanel implements WindowsParentPreferences {

    public WindowsPanel(IsaacPreferences preferencesNode,
                        Manifold manifold,
                        KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Window configurations"), manifold, kpc);
        if (!initialized()) {
            IsaacPreferences windowPreferences = addChild(UUID.randomUUID().toString(), WindowPreferencePanel.class);


            WindowPreferencesItem windowPreferencesItem = PersonaItemPanel.createNewDefaultWindowPreferences(windowPreferences, manifold, kpc);
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

    public Node getTopPanel(Manifold manifold) {
        return new ToolBar(new Label("Window preferences"));
    }

    @Override
    public IsaacPreferences addWindow(String windowName, UUID windowUuid) {
        IsaacPreferences windowPreferences = addChildPanel(windowUuid, Optional.of(windowName));
        windowPreferences.put(GROUP_NAME, windowName);
        return windowPreferences;
    }

    @Override
    protected void revertFields() {

    }
}
