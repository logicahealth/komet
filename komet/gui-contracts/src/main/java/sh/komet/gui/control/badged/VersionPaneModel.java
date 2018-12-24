package sh.komet.gui.control.badged;

import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

public final class VersionPaneModel extends BadgedVersionPaneModel {
    public VersionPaneModel(Manifold manifold, ObservableCategorizedVersion categorizedVersion, OpenIntIntHashMap stampOrderHashMap) {
        super(manifold, categorizedVersion, stampOrderHashMap);
        if (categorizedVersion.getStampSequence() == -1) {
            throw new IllegalStateException("StampSequence = -1: \n" + categorizedVersion);
        }
        this.revertCheckBox.setSelected(false);
        getBadgedPane().getStyleClass()
                .add(StyleClasses.VERSION_PANEL.toString());
        this.expandControl.setVisible(false);
        this.addAttachmentControl.setVisible(false);
    }


    @Override
    public void addExtras() {

        // move the badge, replace edit control with revert  checkbox.
        getBadgedPane().getChildren()
                .remove(editControl);
        getBadgedPane().getChildren()
                .remove(stampControl);
        getBadgedPane().getChildren()
                .remove(revertCheckBox);
        //GridPane.setConstraints(stampControl, 0, 0, 1, 1, HPos.LEFT, VPos.BASELINE, Priority.NEVER, Priority.NEVER);
        getBadgedPane().getChildren()
                .add(stampControl);
        //GridPane.setConstraints(revertCheckBox, columns, 0, 1, 1, HPos.RIGHT, VPos.BASELINE, Priority.NEVER, Priority.NEVER, new Insets(0,4,1,0));
        getBadgedPane().getChildren()
                .add(revertCheckBox);
    }

    @Override
    protected boolean isLatestPanel() {
        return false;
    }

}
