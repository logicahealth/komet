package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class LogicCoordinateGroupPanel extends ParentPanel {

    public LogicCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Logic"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...
            LogicCoordinate elProfileLogicCoordinate = Get.coordinateFactory().createStandardElProfileLogicCoordinate();
            IsaacPreferences childElProfilePreferences = addChild(UUID.randomUUID().toString(), LogicCoordinateItemPanel.class);
            new LogicCoordinateItemPanel(elProfileLogicCoordinate, "EL++ profile", childElProfilePreferences, manifold, kpc);

            save();
        }
    }

    @Override
    protected Class getChildClass() {
        return LogicCoordinateItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}