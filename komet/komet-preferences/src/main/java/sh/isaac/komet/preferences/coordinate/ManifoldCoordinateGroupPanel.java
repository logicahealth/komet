package sh.isaac.komet.preferences.coordinate;

import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.NaturalOrder;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.coordinate.LanguageCoordinateGroupPanel.US_ENGLISH_FQN_COORDINATE_KEY;
import static sh.isaac.komet.preferences.coordinate.LanguageCoordinateGroupPanel.US_ENGLISH_PREFERRED_COORDINATE_KEY;
import static sh.isaac.komet.preferences.coordinate.LogicCoordinateGroupPanel.EL_PROFILE_LOGIC_COORDINATE_KEY;
import static sh.isaac.komet.preferences.coordinate.StampCoordinateGroupPanel.DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY;
import static sh.isaac.komet.preferences.coordinate.StampCoordinateGroupPanel.DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.coordinate.ManifoldCoordinateItemPanel.Keys.MANIFOLD_GROUP_UUID;

public class ManifoldCoordinateGroupPanel extends ParentPanel {
    public static final UuidStringKey INFERRED_GRAPH_NAVIGATION_ANY_NODE_MANIFOLD_KEY = new UuidStringKey(
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE.getGroupUuid(),
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE.getGroupName());
    public static final UuidStringKey INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES_MANIFOLD_KEY = new UuidStringKey(
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES.getGroupUuid(),
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES.getGroupName());
    public static final UuidStringKey INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES_MANIFOLD_KEY = new UuidStringKey(
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES.getGroupUuid(),
            Manifold.ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES.getGroupName());
    public static final UuidStringKey STATED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES_MANIFOLD_KEY = new UuidStringKey(
            Manifold.ManifoldGroup.STATED_GRAPH_NAVIGATION_ANY_NODE.getGroupUuid(),
            Manifold.ManifoldGroup.STATED_GRAPH_NAVIGATION_ANY_NODE.getGroupName());

    public ManifoldCoordinateGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Manifold"), manifold, kpc);
        if (!initialized()) {
            // add each default manifold...

            Manifold.ManifoldGroup[] manifoldValues = Manifold.ManifoldGroup.values();
            Arrays.sort(manifoldValues, (o1, o2) -> NaturalOrder.compareStrings(o1.getGroupName(), o2.getGroupName()));
            for (Manifold.ManifoldGroup group: manifoldValues) {
                IsaacPreferences childPreferences = getPreferencesNode().node(group.getGroupUuid().toString());
                childPreferences.put(GROUP_NAME, group.getGroupName());
                switch (group) {
                    case CLASSIFICATON:
                    case CLINICAL_STATEMENT:
                    case CORRELATION:
                    case FLWOR:
                    case KOMET:
                    case LIST:
                    case SEARCH:
                    case INFERRED_GRAPH_NAVIGATION_ANY_NODE:
                    case UNLINKED:
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_ORIGIN_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_DESTINATION_KEY,
                                DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LANGUAGE_COORDINATE_KEY,
                                US_ENGLISH_PREFERRED_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LOGIC_COORDINATE_KEY,
                                EL_PROFILE_LOGIC_COORDINATE_KEY.toStringArray());
                        childPreferences.put(ManifoldCoordinateItemPanel.Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name());
                        break;
                    case INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES:
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_ORIGIN_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_DESTINATION_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LANGUAGE_COORDINATE_KEY,
                                US_ENGLISH_PREFERRED_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LOGIC_COORDINATE_KEY,
                                EL_PROFILE_LOGIC_COORDINATE_KEY.toStringArray());
                        childPreferences.put(ManifoldCoordinateItemPanel.Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name());
                        break;
                    case INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES:
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_ORIGIN_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_DESTINATION_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LANGUAGE_COORDINATE_KEY,
                                US_ENGLISH_FQN_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LOGIC_COORDINATE_KEY,
                                EL_PROFILE_LOGIC_COORDINATE_KEY.toStringArray());
                        childPreferences.put(ManifoldCoordinateItemPanel.Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name());
                        break;
                    case STATED_GRAPH_NAVIGATION_ANY_NODE:
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_ORIGIN_KEY,
                                DEVELOPMENT_LATEST_ACTIVE_ONLY_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.STAMP_FOR_DESTINATION_KEY,
                                DEVELOPMENT_LATEST_STAMP_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LANGUAGE_COORDINATE_KEY,
                                US_ENGLISH_PREFERRED_COORDINATE_KEY.toStringArray());
                        childPreferences.putArray(ManifoldCoordinateItemPanel.Keys.LOGIC_COORDINATE_KEY,
                                EL_PROFILE_LOGIC_COORDINATE_KEY.toStringArray());
                        childPreferences.put(ManifoldCoordinateItemPanel.Keys.PREMISE_TYPE_KEY, PremiseType.STATED.name());
                }

                IsaacPreferences manifoldPreferences = addChildPanel(group.getGroupUuid(), Optional.of(group.getGroupName()));
                manifoldPreferences.putUuid(MANIFOLD_GROUP_UUID, group.getGroupUuid());
                Manifold manifoldForChild = Manifold.get(group);

                ManifoldCoordinateItemPanel manifoldCoordinateItemPanel = new ManifoldCoordinateItemPanel(manifoldPreferences, manifoldForChild, kpc);
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