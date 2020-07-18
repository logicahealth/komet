package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.coordinate.ManifoldCoordinateItemPanel.Keys.MANIFOLD_GROUP_UUID;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class ManifoldCoordinateGroupPanel extends ParentPanel {

    public ManifoldCoordinateGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Manifold"), viewProperties, kpc);
        if (!initialized()) {
            // add each default manifold...

            for (ActivityFeed activityFeed: viewProperties.getActivityFeeds()) {
                //IsaacPreferences childPreferences = getPreferencesNode().node(activityFeed.);
                //childPreferences.put(GROUP_NAME, activityFeed.getFeedName());
                switch (activityFeed.getFeedName()) {
                    case ViewProperties.ANY:
                    case ViewProperties.CLASSIFICATION:
                    case ViewProperties.CONCEPT_BUILDER:
                    case ViewProperties.FLWOR:
                    case ViewProperties.LIST:
                    case ViewProperties.CORRELATION:
                    case ViewProperties.NAVIGATION:
                    case ViewProperties.SEARCH:
                    case ViewProperties.UNLINKED:
//                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_DESTINATION_KEY,
//                                DEVELOPMENT_PATH_COORDINATE_KEY.toStringArray());
//                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LANGUAGE_COORDINATE_KEY,
//                                US_ENGLISH_PREFERRED_COORDINATE_KEY.toStringArray());
//                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LOGIC_COORDINATE_KEY,
//                                EL_PROFILE_LOGIC_COORDINATE_KEY.toStringArray());
//                        childPreferences.put(ManifoldCoordinateItemPanel.Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name());
                        break;
                }

                IsaacPreferences manifoldPreferences = addChildPanel(activityFeed.getManifoldCoordinateUuid(), Optional.of(activityFeed.getFeedName()));
                manifoldPreferences.putUuid(MANIFOLD_GROUP_UUID, activityFeed.getManifoldCoordinateUuid());
                ManifoldCoordinateItemPanel manifoldCoordinateItemPanel = new ManifoldCoordinateItemPanel(manifoldPreferences, FxGet.preferenceViewProperties(), kpc);
            }
            save();
        }
    }

    @Override
    protected Class getChildClass() {
        return ManifoldCoordinateItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}