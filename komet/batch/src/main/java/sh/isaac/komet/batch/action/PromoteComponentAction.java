package sh.isaac.komet.batch.action;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

import java.util.concurrent.atomic.AtomicReference;

/**
 * If active, promote if no version on destination path, or if version is different from version on destination path.
 *
 * If inactive, promote if version is on destination path, and version is different from version on destination path.
 *
 * Do you retire from the origin path when promoting?
 *
 * How do you see the current version on the promotion path if the origin path is superceeded?
 *
 * Use the withdrawn state?
 *
 * Add a yield state... ?
 *
 * Maybe never go from master to dev... Always bring external released content into dev, then promote to master
 * Then we eliminate some of the more complicated scenarios...
 * This means:
 *      1. don't retire on dev when promoting to master.
 *      2. Dev never depends on master and vice versa.
 *      3. New changes come into dev, and contradictions get resolved before promoting to master, module by module
 *      4. Only promote an entirety of a module from one path to another?
 *
 *      5. When moving from one module to another, retire in old module (unlike for paths)
 *
 */
public class PromoteComponentAction extends ActionItem {
    // origin path
    SimpleObjectProperty<ConceptSpecification> sourcePathProperty = new SimpleObjectProperty<>(this,
            MetaData.PROMOTION_SOURCE_PATH____SOLOR.toExternalString() , TermAux.DEVELOPMENT_PATH);

    // promotion path
    SimpleObjectProperty<ConceptSpecification> promotionPathProperty = new SimpleObjectProperty<>(this,
            MetaData.PROMOTION_DESTINATION_PATH____SOLOR.toExternalString(), TermAux.MASTER_PATH);

    AtomicReference<StampCoordinate> promotionPathStampCoordinateReference = new AtomicReference<>();

    AtomicReference<EditCoordinate> promotionEditCoordinateReference = new AtomicReference<>();

    public PromoteComponentAction(Manifold manifoldForDisplay) {
        super(manifoldForDisplay);

        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Source path",
                sourcePathProperty, TermAux.DEVELOPMENT_PATH, TermAux.MASTER_PATH));

        getPropertySheet().getItems().add(new PropertySheetItemConceptWrapper(manifoldForDisplay, "Promotion path",
                promotionPathProperty, TermAux.MASTER_PATH, TermAux.DEVELOPMENT_PATH));
    }

    @Override
    public void apply(Chronology chronology, Transaction transaction, StampCoordinate stampCoordinate, EditCoordinate editCoordinate) {
        LatestVersion<Version> latestVersion = chronology.getLatestVersion(stampCoordinate);
        if (latestVersion.isAbsent()) {
            LOG.warn("Batch editing requires a latest version to update. None found for: " + chronology);
            // Nothing to do.
            return;
        }
        // See if the latest on the promotion path is different...
        LatestVersion<Version> promotionPathVersion = chronology.getLatestVersion(getPromotionPathStampCoordinate(stampCoordinate));
        if (promotionPathVersion.isPresent()) {
            // need to compare and see if different...
            LOG.info("Test for promotion: \n" + latestVersion.get() + "\n" + promotionPathVersion.get());
        } else {
            // need to promote.
            LOG.info("Promote: " + Get.conceptDescriptionText(latestVersion.get().getNid()) + " " + latestVersion.get());
            Version version = latestVersion.get();
            Version analog = version.makeAnalog(transaction, getPromotionEditCoordinate(editCoordinate));
            Get.commitService().addUncommitted(transaction, analog);
        }
    }

    private StampCoordinate getPromotionPathStampCoordinate(StampCoordinate stampCoordinate) {
        return promotionPathStampCoordinateReference.updateAndGet(promotionPathStampCoordinate -> {
            if (promotionPathStampCoordinate == null) {
                return stampCoordinate.makePathAnalog(promotionPathProperty.get());
            }
            return promotionPathStampCoordinate;
        });
    }

    private EditCoordinate getPromotionEditCoordinate(EditCoordinate editCoordinate) {
        return promotionEditCoordinateReference.updateAndGet(promotionPathEditCoordinate -> {
            if (promotionPathEditCoordinate == null) {
                if (editCoordinate instanceof ObservableEditCoordinate) {
                    promotionPathEditCoordinate = ((ObservableEditCoordinate) editCoordinate).getEditCoordinate().deepClone();
                } else {
                    promotionPathEditCoordinate = editCoordinate.deepClone();
                }
                ((EditCoordinateImpl) promotionPathEditCoordinate).setPathNid(promotionPathProperty.get().getNid());
                return promotionPathEditCoordinate;
            }
            return promotionPathEditCoordinate;
        });
    }
}

