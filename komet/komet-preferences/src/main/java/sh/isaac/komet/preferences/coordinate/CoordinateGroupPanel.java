package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class CoordinateGroupPanel extends ParentPanel {

    public CoordinateGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Coordinates"), viewProperties, kpc);
        if (!initialized()) {
            addChild("Path filter", FilterGroupPanel.class);
            addChild("Language", LanguageCoordinateGroupPanel.class);
            addChild("Logic", LogicCoordinateGroupPanel.class);
            addChild("Navigation", DigraphGroupPanel.class);
            addChild("Manifold coordinate", ManifoldCoordinateGroupPanel.class);
        }
    }

    @Override
    protected Class getChildClass() {
        return ManifoldCoordinateGroupPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}