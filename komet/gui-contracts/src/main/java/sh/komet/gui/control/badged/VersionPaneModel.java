package sh.komet.gui.control.badged;

import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Status;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.style.StyleClasses;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static sh.komet.gui.style.PseudoClasses.UNCOMMITTED_PSEUDO_CLASS;

public final class VersionPaneModel extends BadgedVersionPaneModel {
    public VersionPaneModel(ViewProperties viewProperties, ObservableCategorizedVersion categorizedVersion,
                            OpenIntIntHashMap stampOrderHashMap,
                            HashMap<String, AtomicBoolean> disclosureStateMap) {
        super(viewProperties, categorizedVersion, stampOrderHashMap, disclosureStateMap);

        if (categorizedVersion.getStatus() == Status.CANCELED) {
            throw new IllegalStateException("Version is CANCELED: \n" + categorizedVersion);
        }
        if (categorizedVersion.getStampSequence() == -1) {
            throw new IllegalStateException("StampSequence = -1: \n" + categorizedVersion);
        }
        getBadgedPane().getStyleClass()
                .add(StyleClasses.VERSION_PANEL.toString());
        getBadgedPane().pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, categorizedVersion.isUncommitted());
        this.expandControl.setVisible(false);
    }


    @Override
    public void addExtras() {

        // move the badge, replace edit control with revert  checkbox.
        editControlTiles.getChildren().clear();
        if (!getCategorizedVersion().isUncommitted()) {
            editControlTiles.getChildren().add(redoButton);
        }

        badgeFlow.getChildren().clear();
        badgeFlow.getChildren().add(badgeTiles);
    }

    @Override
    protected boolean isLatestPanel() {
        return false;
    }

}
