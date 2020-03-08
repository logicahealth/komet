package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class LogicCoordinateGroupPanel extends ParentPanel {
    public static final UuidStringKey EL_PROFILE_LOGIC_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3eccd94-5e5e-11ea-bc55-0242ac130003"), "EL++ profile");

    public LogicCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Logic"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...
            LogicCoordinate elProfileLogicCoordinate = Get.coordinateFactory().createStandardElProfileLogicCoordinate();
            IsaacPreferences childElProfilePreferences = addChild(EL_PROFILE_LOGIC_COORDINATE_KEY.getUuid().toString(), LogicCoordinateItemPanel.class);
            new LogicCoordinateItemPanel(elProfileLogicCoordinate, EL_PROFILE_LOGIC_COORDINATE_KEY.getString(), childElProfilePreferences, manifold, kpc);

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