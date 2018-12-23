package sh.komet.gui.control.badged;

import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

public final class Version  extends BadgedVersion {
    public Version(Manifold manifold, ObservableCategorizedVersion categorizedVersion, OpenIntIntHashMap stampOrderHashMap) {
        super(manifold, categorizedVersion, stampOrderHashMap);
        if (categorizedVersion.getStampSequence() == -1) {
            throw new IllegalStateException("StampSequence = -1: \n" + categorizedVersion);
        }
        this.revertCheckBox.setSelected(false);
        getPane().getStyleClass()
                .add(StyleClasses.VERSION_PANEL.toString());
        this.expandControl.setVisible(false);
        this.addAttachmentControl.setVisible(false);
    }


    @Override
    protected boolean isLatestPanel() {
        return false;
    }

}
