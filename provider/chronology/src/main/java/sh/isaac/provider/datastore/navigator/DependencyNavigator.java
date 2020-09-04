package sh.isaac.provider.datastore.navigator;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.navigation.Navigator;
import sh.isaac.api.tree.EdgeImpl;

import java.util.List;

public class DependencyNavigator implements Navigator {

    private final SingleAssemblageSnapshot<Nid1_Long2_Version> navigationAssemblage;
    private final int[] treeAssemblageNidAsArray;
    private final ManifoldCoordinate manifoldCoordinate;

    public DependencyNavigator(SingleAssemblageSnapshot<Nid1_Long2_Version> navigationAssemblage, ManifoldCoordinate manifoldCoordinate) {
        this.navigationAssemblage = navigationAssemblage;
        this.treeAssemblageNidAsArray = new int[] {navigationAssemblage.getAssemblageNid() };
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public int[] getChildNids(int parentNid) {
        List<LatestVersion<Nid1_Long2_Version>> children = navigationAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        NidSet childrenNids = new NidSet();
        for (LatestVersion<Nid1_Long2_Version> childSemantic: children) {
            childSemantic.ifPresent((semantic) -> {
                if (Get.concept(semantic.getNid1()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
                    childrenNids.add(semantic.getNid1());
                }
            });
        }
        return childrenNids.asArray();
    }

    @Override
    public int[] getParentNids(int childNid) {
        NidSet parentNids = new NidSet();
        List<SearchResult> matches = Get.indexSemanticService().queryNidReference(childNid, treeAssemblageNidAsArray, null, null, null, null, null, Long.MIN_VALUE);
        for (SearchResult match: matches) {
            int semanticNid = match.getNid();
            navigationAssemblage.getLatestSemanticVersion(semanticNid).ifPresent((t) -> {
                if (Get.concept(t.getReferencedComponentNid()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
                    parentNids.add(t.getReferencedComponentNid());
                }
            });
        }
        return parentNids.asArray();
    }

    @Override
    public int[] getRootNids() {
        return new int[] {TermAux.PRIMORDIAL_MODULE.getNid()};
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        List<LatestVersion<Nid1_Long2_Version>> children = navigationAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<Nid1_Long2_Version> childSemantic: children) {
            if (childSemantic.isPresent()) {
                if (childSemantic.get().getNid1() == childNid &&
                        Get.concept(childSemantic.get().getNid1()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
                    return true;
                }
            }
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
            links.add(new EdgeImpl(this.treeAssemblageNidAsArray[0], parentNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getChildLinks(int childConceptNid) {
        int[] childNids = getChildNids(childConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
        for (int childNid: childNids) {
            links.add(new EdgeImpl(this.treeAssemblageNidAsArray[0], childNid));
        }
        return links.toImmutable();
    }

}
