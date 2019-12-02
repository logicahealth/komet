package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.NaturalOrder;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;

import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.coordinate.ManifoldCoordinateItemPanel.Keys.MANIFOLD_GROUP_UUID;

public class ManifoldCoordinateGroupPanel extends ParentPanel {

    public ManifoldCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Manifold"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            Manifold.ManifoldGroup[] manifoldValues = Manifold.ManifoldGroup.values();
            Arrays.sort(manifoldValues, (o1, o2) -> NaturalOrder.compareStrings(o1.getGroupName(), o2.getGroupName()));
            for (Manifold.ManifoldGroup group: manifoldValues) {
                IsaacPreferences manifoldPreferences = addChildPanel(group.getGroupUuid(), Optional.of(group.getGroupName()));
                manifoldPreferences.putUuid(MANIFOLD_GROUP_UUID, group.getGroupUuid());

                ManifoldCoordinateItemPanel manifoldCoordinateItemPanel = new ManifoldCoordinateItemPanel(manifoldPreferences, manifold, kpc);
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