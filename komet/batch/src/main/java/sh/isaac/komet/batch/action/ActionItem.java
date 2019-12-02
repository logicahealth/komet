package sh.isaac.komet.batch.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.provider.commit.TransactionImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

public abstract class ActionItem {

    protected static final Logger LOG = LogManager.getLogger();

    PropertySheet propertySheet = new PropertySheet();
    {
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);

    }

    public ActionItem(Manifold manifold) {
        this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
    }

    public PropertySheet getPropertySheet() {
        return this.propertySheet;
    }

    public abstract void apply(Chronology chronology,
                               Transaction transaction,
                               StampCoordinate stampCoordinate,
                               EditCoordinate editCoordinate);
}
