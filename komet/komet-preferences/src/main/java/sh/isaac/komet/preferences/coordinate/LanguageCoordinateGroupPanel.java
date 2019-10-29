package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.Get;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class LanguageCoordinateGroupPanel extends ParentPanel {

    public LanguageCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Language"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            LanguageCoordinate gbEnglishPreferredCoordinate = Get.coordinateFactory().getGbEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childGbEnglishPreferences = addChild(UUID.randomUUID().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(gbEnglishPreferredCoordinate, "GB English", childGbEnglishPreferences, manifold, kpc);

            LanguageCoordinate usEnglishFullyQualifiedCoordinate = Get.coordinateFactory().getUsEnglishLanguageFullySpecifiedNameCoordinate();
            IsaacPreferences childUsEnglishFullyQualifiedPreferences = addChild(UUID.randomUUID().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishFullyQualifiedCoordinate, "US English, fully qualified", childUsEnglishFullyQualifiedPreferences, manifold, kpc);


            LanguageCoordinate usEnglishPreferredCoordinate = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
            IsaacPreferences childUsEnglishPreferences = addChild(UUID.randomUUID().toString(), LanguageCoordinateItemPanel.class);
            new LanguageCoordinateItemPanel(usEnglishPreferredCoordinate, "US English", childUsEnglishPreferences, manifold, kpc);

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