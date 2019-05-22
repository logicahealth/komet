package sh.komet.gui.control.badged;

import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VersionPaneModel extends BadgedVersionPaneModel {
    public VersionPaneModel(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
                            OpenIntIntHashMap stampOrderHashMap,
                            HashMap<String, AtomicBoolean> disclosureStateMap) {
        super(manifold, categorizedVersion, stampOrderHashMap, disclosureStateMap);
        if (categorizedVersion.getStampSequence() == -1) {
            throw new IllegalStateException("StampSequence = -1: \n" + categorizedVersion);
        }
        getBadgedPane().getStyleClass()
                .add(StyleClasses.VERSION_PANEL.toString());
        this.expandControl.setVisible(false);
    }


    @Override
    public void addExtras() {

        // move the badge, replace edit control with revert  checkbox.
        editControlTiles.getChildren().clear();
        editControlTiles.getChildren().add(redoButton);
        badgeFlow.getChildren().clear();
        badgeFlow.getChildren().add(badgeTiles);
    }

    @Override
    protected boolean isLatestPanel() {
        return false;
    }

}
