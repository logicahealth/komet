package sh.isaac.api.tree;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampBranchImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;

public class TaxonomySnapshotFromPathOrigins  implements TaxonomySnapshot {

    private final ManifoldCoordinate manifoldCoordinate;

    public TaxonomySnapshotFromPathOrigins(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public int[] getTaxonomyChildConceptNids(int parentNid) {
        ImmutableSet<StampBranchImmutable> branches = Get.versionManagmentPathService().getBranches(parentNid);
        MutableIntList results = IntLists.mutable.empty();
        branches.forEach(stampBranchImmutable -> {
            results.add(stampBranchImmutable.getPathOfBranchNid());
        });
        return results.toArray();
    }

    @Override
    public int[] getTaxonomyParentConceptNids(int childNid) {
        ImmutableSet<StampPositionImmutable> origins = Get.versionManagmentPathService().getOrigins(childNid);
        MutableIntList results = IntLists.mutable.empty();
        origins.forEach(stampPositionImmutable -> {
            results.add(stampPositionImmutable.getPathForPositionNid());
        });
        return results.toArray();
    }

    @Override
    public int[] getRootNids() {
        return new int[] {};
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        ImmutableSet<StampBranchImmutable> branches = Get.versionManagmentPathService().getBranches(parentNid);
        MutableIntSet results = IntSets.mutable.empty();
        branches.forEach(stampBranchImmutable -> {
            results.add(stampBranchImmutable.getPathOfBranchNid());
        });
        return results.contains(childNid);
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        return getTaxonomyChildConceptNids(conceptNid).length == 0;
    }

    @Override
    public boolean isKindOf(int childConceptNid, int parentConceptNid) {
        throw new UnsupportedOperationException("Not supported by assemblage.");
    }

    @Override
    public ImmutableIntSet getKindOfConcept(int rootConceptNid) {
        throw new UnsupportedOperationException("Not supported by assemblage.");
    }
    @Override
    public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree getTaxonomyTree() {
        throw new UnsupportedOperationException("Not supported by assemblage.");
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    @Override
    public ImmutableCollection<Edge> getTaxonomyParentLinks(int parentConceptNid) {
        int[] parentNids = getTaxonomyParentConceptNids(parentConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(parentNids.length);
        for (int parentNid: parentNids) {
            links.add(new EdgeImpl(TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid(), parentNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getTaxonomyChildLinks(int childConceptNid) {
        int[] childNids = getTaxonomyChildConceptNids(childConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
        for (int childNid: childNids) {
            links.add(new EdgeImpl(TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid(), childNid));
        }
        return links.toImmutable();
    }
}
