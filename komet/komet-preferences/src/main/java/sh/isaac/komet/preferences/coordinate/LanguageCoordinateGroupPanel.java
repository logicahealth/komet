package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;
import sh.isaac.api.util.UuidStringKey;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class LanguageCoordinateGroupPanel extends ParentPanel {

    public static final UuidStringKey GB_ENGLISH_PREFERRED_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd08c-5e5e-11ea-bc55-0242ac130003"), "GB English");
    public static final UuidStringKey US_ENGLISH_FQN_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd186-5e5e-11ea-bc55-0242ac130003"), "US English, fully qualified");
    public static final UuidStringKey US_ENGLISH_PREFERRED_COORDINATE_KEY = new UuidStringKey(UUID.fromString("a3ecd258-5e5e-11ea-bc55-0242ac130003"), "US English");

    public LanguageCoordinateGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Language"), viewProperties, kpc);
        if (!initialized()) {
            // add each default manifold...

            LanguageCoordinate gbEnglishPreferredCoordinate = Get.coordinateFactory().getGbEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childGbEnglishPreferences = addChild(GB_ENGLISH_PREFERRED_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(gbEnglishPreferredCoordinate, GB_ENGLISH_PREFERRED_COORDINATE_KEY.getString(), childGbEnglishPreferences, viewProperties, kpc);

            LanguageCoordinate usEnglishFullyQualifiedCoordinate = Get.coordinateFactory().getUsEnglishLanguageFullySpecifiedNameCoordinate();
            IsaacPreferences childUsEnglishFullyQualifiedPreferences = addChild(US_ENGLISH_FQN_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishFullyQualifiedCoordinate, US_ENGLISH_FQN_COORDINATE_KEY.getString(), childUsEnglishFullyQualifiedPreferences, viewProperties, kpc);


            LanguageCoordinate usEnglishPreferredCoordinate = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childUsEnglishPreferences = addChild(US_ENGLISH_PREFERRED_COORDINATE_KEY.getUuid().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishPreferredCoordinate, US_ENGLISH_PREFERRED_COORDINATE_KEY.getString(), childUsEnglishPreferences, viewProperties, kpc);

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