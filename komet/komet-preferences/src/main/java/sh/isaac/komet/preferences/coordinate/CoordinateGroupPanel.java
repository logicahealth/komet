package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;

import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class CoordinateGroupPanel extends ParentPanel {

    public CoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Coordinates"), manifold, kpc);
        if (!initialized()) {
            addChild("Path filter", FilterGroupPanel.class);
            addChild("Language", LanguageCoordinateGroupPanel.class);
            addChild("Logic", LogicCoordinateGroupPanel.class);
            addChild("Digraph", DigraphGroupPanel.class);
            addChild("Manifold", ManifoldCoordinateGroupPanel.class);
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