package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class LanguageCoordinateGroupPanel extends ParentPanel {

    public static final UuidStringKey GB_ENGLISH_PREFERRED_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd08c-5e5e-11ea-bc55-0242ac130003"), "GB English");
    public static final UuidStringKey US_ENGLISH_FQN_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd186-5e5e-11ea-bc55-0242ac130003"), "US English, fully qualified");
    public static final UuidStringKey US_ENGLISH_PREFERRED_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd258-5e5e-11ea-bc55-0242ac130003"), "US English");

    public LanguageCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Language"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            LanguageCoordinate gbEnglishPreferredCoordinate = Get.coordinateFactory().getGbEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childGbEnglishPreferences = addChild(GB_ENGLISH_PREFERRED_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(gbEnglishPreferredCoordinate, GB_ENGLISH_PREFERRED_COORDINATE_KEY.getString(), childGbEnglishPreferences, manifold, kpc);

            LanguageCoordinate usEnglishFullyQualifiedCoordinate = Get.coordinateFactory().getUsEnglishLanguageFullySpecifiedNameCoordinate();
            IsaacPreferences childUsEnglishFullyQualifiedPreferences = addChild(US_ENGLISH_FQN_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishFullyQualifiedCoordinate, US_ENGLISH_FQN_COORDINATE_KEY.getString(), childUsEnglishFullyQualifiedPreferences, manifold, kpc);


            LanguageCoordinate usEnglishPreferredCoordinate = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childUsEnglishPreferences = addChild(US_ENGLISH_PREFERRED_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishPreferredCoordinate, US_ENGLISH_PREFERRED_COORDINATE_KEY.getString(), childUsEnglishPreferences, manifold, kpc);

            save();
        }
    }

    @Override
    protected Class getChildClass() {
        return LanguageCoordinateItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}