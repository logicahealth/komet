package sh.isaac.komet.batch.action;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.Activity;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.komet.gui.control.concept.PropertySheetItemReadOnlyConceptWrapper;

import java.util.concurrent.ConcurrentHashMap;

import static sh.isaac.komet.batch.action.MergePathActionFactory.MERGE_PATH;

public class MergePath extends ActionItem {
    public static final int marshalVersion = 1;
    private enum MergeKeys {
        MERGE_MANIFOLD,
        PROMOTION_VIEW,
        TRANSACTION
    }

    public MergePath() {
    }

    public MergePath(ByteArrayDataBuffer in) {
        // no fields yet;
    }

    @Override
    protected void setupItemForGui(ObservableManifoldCoordinate manifoldForDisplay) {

        PropertySheetItemReadOnlyConceptWrapper sourcePathWrapper = new PropertySheetItemReadOnlyConceptWrapper(manifoldForDisplay, "Source path",
                manifoldForDisplay.getViewStampFilter().pathConceptProperty());

        getPropertySheet().getItems().add(sourcePathWrapper);

        PropertySheetItemReadOnlyConceptWrapper promotionPathWrapper = new PropertySheetItemReadOnlyConceptWrapper(manifoldForDisplay, "Promotion path",
                manifoldForDisplay.getEditCoordinate().promotionPathProperty());
        getPropertySheet().getItems().add(promotionPathWrapper);

    }

    @Override
    protected void setupForApply(ConcurrentHashMap<Enum, Object> cache, Transaction transaction, ManifoldCoordinateImmutable manifoldCoordinate) {
        ObservableManifoldCoordinateImpl mergeManifold = new ObservableManifoldCoordinateImpl(manifoldCoordinate.toManifoldCoordinateImmutable());
        mergeManifold.activityProperty().setValue(Activity.PROMOTING);
        cache.put(MergeKeys.MERGE_MANIFOLD, mergeManifold.getValue());

        mergeManifold.activityProperty().setValue(Activity.VIEWING);
        mergeManifold.setManifoldPath(mergeManifold.getEditCoordinate().getPromotionPath());
        cache.put(MergeKeys.PROMOTION_VIEW, mergeManifold.getValue());

        cache.put(MergeKeys.TRANSACTION, transaction);


    }

    @Override
    protected void apply(Chronology chronology, ConcurrentHashMap<Enum, Object> cache, VersionChangeListener versionChangeListener) {
        ManifoldCoordinateImmutable mergeManifold = (ManifoldCoordinateImmutable) cache.get(MergeKeys.MERGE_MANIFOLD);
        ManifoldCoordinateImmutable promotionManifold = (ManifoldCoordinateImmutable) cache.get(MergeKeys.PROMOTION_VIEW);
        Transaction transaction = (Transaction) cache.get(MergeKeys.TRANSACTION);

        LatestVersion<Version> latestVersion = chronology.getLatestVersion(mergeManifold.getViewStampFilter());
        if (latestVersion.isAbsent()) {
            LOG.warn("Batch editing requires a latest version to update. None found for: " + chronology);
            // Nothing to do.
            return;
        }

        Version version = latestVersion.get();
        if (version.getPathNid() == mergeManifold.getViewStampFilter().getPathNidForFilter()) {
            LOG.info("Found version for merge processing: " + version.toUserString());
            // See if the latest on the promotion path is different...
            LatestVersion<Version> promotionPathVersion = chronology.getLatestVersion(promotionManifold.getViewStampFilter());
            if (promotionPathVersion.isPresent()) {
                // need to compare and see if different...
                LOG.info("Test for promotion: \n" + latestVersion.get() + "\n" + promotionPathVersion.get());
            } else {
                // need to promote.
                LOG.info("Promote: " + Get.conceptDescriptionText(latestVersion.get().getNid()) + " " + latestVersion.get());
                Version analog = version.makeAnalog(transaction, mergeManifold);
                Get.commitService().addUncommitted(transaction, analog);
                versionChangeListener.versionChanged(version, analog);
            }
        }
    }

    @Override
    protected void conclude(ConcurrentHashMap<Enum, Object> cache) {
        // nothing to do...
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
    }

    @Unmarshaler
    public static Object make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new MergePath(in);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    public String getTitle() {
        return MERGE_PATH;
    }

}
