package sh.isaac.provider.datastore.navigator;

import org.eclipse.collections.api.collection.ImmutableCollection;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.navigation.Navigator;

import java.util.UUID;

public class PremiseNavigator implements Navigator, RefreshListener {

    UUID listenerUuid = UUID.randomUUID();

    private final ManifoldCoordinateImmutable manifoldCoordinate;

    TaxonomySnapshot definingTaxonomySnapshot;

    public PremiseNavigator(ManifoldCoordinate manifoldCoordinate, int navigatorNid) {
        this.manifoldCoordinate = manifoldCoordinate.toManifoldCoordinateImmutable();
        if (navigatorNid == this.manifoldCoordinate.getLogicCoordinate().getStatedAssemblageNid()) {
            if (this.manifoldCoordinate.getPremiseType() == PremiseType.STATED) {
                this.definingTaxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
            } else {
                throw new IllegalStateException("Premise type inconsistent with navigatorNid" + this.manifoldCoordinate);
            }
        } else if (navigatorNid == manifoldCoordinate.getLogicCoordinate().getInferredAssemblageNid()) {
            if (this.manifoldCoordinate.getPremiseType() == PremiseType.INFERRED) {
                this.definingTaxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
            } else {
                throw new IllegalStateException("Premise type inconsistent with navigatorNid" + this.manifoldCoordinate);
            }
        } else {
            throw new IllegalStateException("NavigatorNid " + navigatorNid + " is neither stated nor inferred. " + manifoldCoordinate);
        }
        Get.taxonomyService().addTaxonomyRefreshListener(this);
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        return definingTaxonomySnapshot.isLeaf(conceptNid);
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        return definingTaxonomySnapshot.isChildOf(childNid, parentNid);
    }

    @Override
    public boolean isDescendentOf(int descendantNid, int ancestorNid) {
        return definingTaxonomySnapshot.isDescendentOf(descendantNid, ancestorNid);
    }

    @Override
    public int[] getRootNids() {
        return definingTaxonomySnapshot.getRootNids();
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return this.manifoldCoordinate;
    }

    @Override
    public UUID getListenerUuid() {
        return listenerUuid;
    }

    @Override
    public void refresh() {
        this.definingTaxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
    }

    @Override
    public int[] getParentNids(int childNid) {
        return this.definingTaxonomySnapshot.getTaxonomyParentConceptNids(childNid);
    }

    @Override
    public int[] getChildNids(int parentNid) {
        return this.definingTaxonomySnapshot.getTaxonomyChildConceptNids(parentNid);
    }

    @Override
    public ImmutableCollection<Edge> getParentLinks(int childNid) {
        return this.definingTaxonomySnapshot.getTaxonomyParentLinks(childNid);
    }

    @Override
    public ImmutableCollection<Edge> getChildLinks(int parentNid) {
        return this.definingTaxonomySnapshot.getTaxonomyChildLinks(parentNid);
    }
}