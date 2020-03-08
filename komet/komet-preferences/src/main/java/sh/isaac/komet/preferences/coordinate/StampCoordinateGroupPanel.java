package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class StampCoordinateGroupPanel extends ParentPanel {
    public static final UuidStringKey DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecc72c-5e5e-11ea-bc55-0242ac130003"), "Development latest");
    public static final UuidStringKey DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecc948-5e5e-11ea-bc55-0242ac130003"), "Development latest, active only");
    public static final UuidStringKey MASTER_LATEST_STAMP_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecca42-5e5e-11ea-bc55-0242ac130003"), "Master latest");
    public static final UuidStringKey MASTER_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3eccbf0-5e5e-11ea-bc55-0242ac130003"), "Master latest, active only");

    public StampCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Stamp"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            StampCoordinate developmentLatest = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
            IsaacPreferences childPreferences = addChild(DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY.getUuid().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(developmentLatest, DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY.getString(), childPreferences, manifold, kpc);

            StampCoordinate developmentLatestActiveOnly = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
            IsaacPreferences activeOnlyChildPreferences = addChild(DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.getUuid().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(developmentLatestActiveOnly, DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.getString(), activeOnlyChildPreferences, manifold, kpc);

            StampCoordinate masterLatest = Get.coordinateFactory().createMasterLatestStampCoordinate();
            IsaacPreferences masterChildPreferences = addChild(MASTER_LATEST_STAMP_COORDINATE_KEY.getUuid().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(masterLatest, MASTER_LATEST_STAMP_COORDINATE_KEY.getString(), masterChildPreferences, manifold, kpc);

            StampCoordinate masterLatestActiveOnly = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
            IsaacPreferences masterActiveOnlyChildPreferences = addChild(MASTER_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.getUuid().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(masterLatestActiveOnly, MASTER_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.getString(), masterActiveOnlyChildPreferences, manifold, kpc);
            save();

        }
    }

    @Override
    protected Class getChildClass() {
        return StampCoordinateItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}