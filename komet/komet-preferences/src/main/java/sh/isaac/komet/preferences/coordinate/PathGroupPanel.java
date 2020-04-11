package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.PathCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class PathGroupPanel extends ParentPanel {
    public static final UuidStringKey DEVELOPMENT_PATH_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecc72c-5e5e-11ea-bc55-0242ac130003"), "Development path");
    public static final UuidStringKey MASTER_PATH_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecca42-5e5e-11ea-bc55-0242ac130003"), "Master path");

    public PathGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Filter"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            PathCoordinate developmentLatest = Coordinates.Path.Development();
            IsaacPreferences childPreferences = addChild(DEVELOPMENT_PATH_COORDINATE_KEY.getUuid().toString(), PathItemPanel.class);
            new PathItemPanel(developmentLatest, DEVELOPMENT_PATH_COORDINATE_KEY.getString(), childPreferences, manifold, kpc);

            PathCoordinate masterLatest = Coordinates.Path.Master();
            IsaacPreferences masterChildPreferences = addChild(MASTER_PATH_COORDINATE_KEY.getUuid().toString(), PathItemPanel.class);
            new PathItemPanel(masterLatest, MASTER_PATH_COORDINATE_KEY.getString(), masterChildPreferences, manifold, kpc);

            save();

        }
    }

    @Override
    protected Class getChildClass() {
        return PathItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}