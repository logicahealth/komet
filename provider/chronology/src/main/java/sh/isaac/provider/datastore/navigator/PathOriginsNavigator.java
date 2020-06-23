package sh.isaac.provider.datastore.navigator;

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
import sh.isaac.api.navigation.Navigator;
import sh.isaac.api.tree.EdgeImpl;

public class PathOriginsNavigator implements Navigator {

    private final ManifoldCoordinate manifoldCoordinate;

    public PathOriginsNavigator(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public int[] getChildNids(int parentNid) {
        if (Get.versionManagmentPathService().exists(parentNid)) {
            ImmutableSet<StampBranchImmutable> branches = Get.versionManagmentPathService().getBranches(parentNid);
            MutableIntList results = IntLists.mutable.empty();
            branches.forEach(stampBranchImmutable -> {
                results.add(stampBranchImmutable.getPathOfBranchNid());
            });
            return results.toArray();
        }
        return new int[0];
    }

    @Override
    public int[] getParentNids(int childNid) {
        if (Get.versionManagmentPathService().exists(childNid)) {
            ImmutableSet<StampPositionImmutable> origins = Get.versionManagmentPathService().getOrigins(childNid);
            MutableIntList results = IntLists.mutable.empty();
            origins.forEach(stampPositionImmutable -> {
                results.add(stampPositionImmutable.getPathForPositionNid());
            });
            return results.toArray();
        }
        return new int[0];
    }

    @Override
    public int[] getRootNids() {
        return new int[] {
                TermAux.PRIMORDIAL_PATH.getNid()
        };
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        if (Get.versionManagmentPathService().exists(childNid)) {
            ImmutableSet<StampBranchImmutable> branches = Get.versionManagmentPathService().getBranches(parentNid);
            MutableIntSet results = IntSets.mutable.empty();
            branches.forEach(stampBranchImmutable -> {
                results.add(stampBranchImmutable.getPathOfBranchNid());
            });
            return results.contains(childNid);
        }
        return false;
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        return getChildNids(conceptNid).length == 0;
    }

    @Override
    public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    @Override
    public ImmutableCollection<Edge> getParentLinks(int parentConceptNid) {
        int[] parentNids = getParentNids(parentConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(parentNids.length);
        for (int parentNid: parentNids) {
            links.add(new EdgeImpl(TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid(), parentNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getChildLinks(int childConceptNid) {
        int[] childNids = getChildNids(childConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
        for (int childNid: childNids) {
            links.add(new EdgeImpl(TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid(), childNid));
        }
        return links.toImmutable();
    }
}
