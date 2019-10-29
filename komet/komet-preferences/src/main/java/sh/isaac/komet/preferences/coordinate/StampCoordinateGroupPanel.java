package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class StampCoordinateGroupPanel extends ParentPanel {

    public StampCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Stamp"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            StampCoordinate developmentLatest = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
            IsaacPreferences childPreferences = addChild(UUID.randomUUID().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(developmentLatest, "Development latest", childPreferences, manifold, kpc);

            StampCoordinate developmentLatestActiveOnly = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
            IsaacPreferences activeOnlyChildPreferences = addChild(UUID.randomUUID().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(developmentLatestActiveOnly, "Development latest, active only", activeOnlyChildPreferences, manifold, kpc);

            StampCoordinate masterLatest = Get.coordinateFactory().createMasterLatestStampCoordinate();
            IsaacPreferences masterChildPreferences = addChild(UUID.randomUUID().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(masterLatest, "Master latest", masterChildPreferences, manifold, kpc);

            StampCoordinate masterLatestActiveOnly = Get.coordinateFactory().createDevelopmentLatestActiveOnlyStampCoordinate();
            IsaacPreferences masterActiveOnlyChildPreferences = addChild(UUID.randomUUID().toString(), StampCoordinateItemPanel.class);
            new StampCoordinateItemPanel(masterLatestActiveOnly, "Master latest, active only", masterActiveOnlyChildPreferences, manifold, kpc);
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