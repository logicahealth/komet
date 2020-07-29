package sh.isaac.komet.batch.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.komet.gui.control.property.PropertyEditorFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Separate gui class from data class... Maybe when records are available.
 */
public abstract class ActionItem implements Marshalable {

    protected static final Logger LOG = LogManager.getLogger();

    PropertySheet propertySheet;

    public PropertySheet getPropertySheet() {
        return this.propertySheet;
    }

    public void setupForGui(ManifoldCoordinate manifold) {
        // TODO make property sheet transparent so background list row striping shows through properly...
        this.propertySheet = new PropertySheet();
        this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);
        setupItemForGui(manifold);
    }

    protected abstract void setupItemForGui(ManifoldCoordinate manifold);

    /**
     * Called once before calling the apply methods, so that an action can setup and cache any objects
     * that may be used repeatedly during the apply calls, or that are necessary to ensure a consistent state.
     * ActionItems should use enum keys defined within their own class so that there is no duplicate assignment
     * of a key.
     * @param cache
     * @param manifoldCoordinate
     */
    protected abstract void setupForApply(ConcurrentHashMap<Enum, Object> cache,
                                          Transaction transaction,
                                          ManifoldCoordinate manifoldCoordinate);

    /**
     * This is the call that actually performs the action.
     * @param chronology
     * @param cache
     * @param transaction
     * @param manifoldCoordinate
     */
    protected abstract void apply(Chronology chronology,
                                  ConcurrentHashMap<Enum, Object> cache,
                                  Transaction transaction,
                                  ManifoldCoordinate manifoldCoordinate,
                                  VersionChangeListener versionChangeListener);

    public abstract String getTitle();

}
