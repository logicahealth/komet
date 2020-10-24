package sh.isaac.provider.datastore.navigator;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.factory.Lists;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.navigation.Navigator;

import java.util.ArrayList;
import java.util.Collection;

public class NavigationAmalgam implements Navigator {
    final ArrayList<Navigator> navigators = new ArrayList<>();
    final ArrayList<Navigator> reverseNavigators = new ArrayList<>();
    final ArrayList<ConceptSpecification> roots = new ArrayList<>();
    final ManifoldCoordinateImmutable manifoldCoordinate;

    public NavigationAmalgam(ManifoldCoordinate manifoldCoordinate) {
        if (manifoldCoordinate == null) {
            throw new NullPointerException("manifoldCoordinate cannot be null. ");
        }
        this.manifoldCoordinate = manifoldCoordinate.toManifoldCoordinateImmutable();
        this.manifoldCoordinate.getNavigationCoordinate().getNavigationConceptNids().forEach(navigatorNid -> {
            LogicCoordinate logicCoordinate = this.manifoldCoordinate.getLogicCoordinate();
            if (navigatorNid == logicCoordinate.getStatedAssemblageNid()) {
                this.navigators.add(new PremiseNavigator(this.manifoldCoordinate, navigatorNid));
                this.roots.add(logicCoordinate.getRoot());
            } else if (navigatorNid == logicCoordinate.getInferredAssemblageNid()) {
                this.navigators.add(new PremiseNavigator(this.manifoldCoordinate, navigatorNid));
                this.roots.add(logicCoordinate.getRoot());
            } else if (navigatorNid == TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid()) {
                PathOriginsNavigator pathOriginsNavigator = new PathOriginsNavigator(this.manifoldCoordinate);
                this.navigators.add(pathOriginsNavigator);
                for (int rootNid: pathOriginsNavigator.getRootNids()) {
                    this.roots.add(Get.concept(rootNid));
                }
            } else if (navigatorNid == TermAux.DEPENDENCY_MANAGEMENT.getNid()) {
                DependencyNavigator dependencyNavigator =
                        new DependencyNavigator(Get.assemblageService().getSingleAssemblageSnapshot(
                                TermAux.DEPENDENCY_MANAGEMENT.getNid(), Nid1_Long2_Version.class, manifoldCoordinate.getVertexStampFilter()
                        ), manifoldCoordinate);
                this.navigators.add(dependencyNavigator);
                for (int rootNid: dependencyNavigator.getRootNids()) {
                    this.roots.add(Get.concept(rootNid));
                }
            } else {
                SingleAssemblageSnapshot< ComponentNidVersion > navigationSnapshot = Get.assemblageService().getSingleAssemblageSnapshot(navigatorNid, ComponentNidVersion.class, this.manifoldCoordinate.getViewStampFilter());
                ComponentNidAssemblageNavigator navigator = new ComponentNidAssemblageNavigator(navigationSnapshot, this.manifoldCoordinate);
                this.navigators.add(navigator);
                for (int rootNid: navigator.getRootNids()) {
                    this.roots.add(Get.concept(rootNid));
                }
            }
        });
    }

    public ArrayList<ConceptSpecification> getRoots() {
        return roots;
    }

    public ArrayList<Navigator> getNavigators() {
        return navigators;
    }

    public ArrayList<Navigator> getReverseNavigators() {
        return reverseNavigators;
    }

    @Override
    public int[] getChildNids(int parentNid) {
        NidSet childNids = new NidSet();
        for (Navigator navigator: navigators) {
            childNids.addAll(navigator.getChildNids(parentNid));
        }
        for (Navigator reverseNavigator: reverseNavigators) {
            childNids.addAll(reverseNavigator.getChildNids(parentNid));
        }
        return childNids.asArray();
    }


    @Override
    public int[] getParentNids(int childNid) {
        NidSet parentNids = new NidSet();
        for (Navigator navigator: navigators) {
            parentNids.addAll(navigator.getParentNids(childNid));
        }
        for (Navigator inverseTree: reverseNavigators) {
            parentNids.addAll(inverseTree.getParentNids(childNid));
        }
        return parentNids.asArray();
    }


    @Override
    public int[] getRootNids() {
        NidSet rootNids = new NidSet();
        for (Navigator navigator: navigators) {
            rootNids.addAll(navigator.getRootNids());
        }
        for (ConceptSpecification root: roots) {
            rootNids.add(root.getNid());
        }
        return rootNids.asArray();
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        for (Navigator navigator: navigators) {
            if (navigator.isChildOf(childNid,  parentNid)) {
                return true;
            }
        }
        for (Navigator reverseNavigator: reverseNavigators) {
            if (reverseNavigator.isChildOf(parentNid, childNid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        for (Navigator navigator: navigators) {
            if (!navigator.isLeaf(conceptNid)) {
                return false;
            };
        }
        for (Navigator reverseNavigator: reverseNavigators) {
            if (!reverseNavigator.isLeaf(conceptNid)) {
                return false;
            };
        }
        return true;
    }

    @Override
    public boolean isDescendentOf(int descendantNid, int ancestorNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return this.manifoldCoordinate;
    }

    @Override
    public ImmutableCollection<Edge> getParentLinks(int parentConceptNid) {
        MutableCollection<Edge> links = Lists.mutable.empty();
        for (Navigator navigator: navigators) {
            links.addAll((Collection<? extends Edge>) navigator.getParentLinks(parentConceptNid));
        }
        for (Navigator reverseNavigator: reverseNavigators) {
            links.addAll((Collection<? extends Edge>) reverseNavigator.getParentLinks(parentConceptNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getChildLinks(int childConceptNid) {
        MutableCollection<Edge> links = Lists.mutable.empty();
        for (Navigator navigator: navigators) {
            links.addAll((Collection<? extends Edge>) navigator.getChildLinks(childConceptNid));
        }
        for (Navigator reverseNavigator: reverseNavigators) {
            links.addAll((Collection<? extends Edge>) reverseNavigator.getChildLinks(childConceptNid));
        }
        return links.toImmutable();
    }

    public void reset() {
        this.navigators.clear();
        this.reverseNavigators.clear();
        this.roots.clear();
    }
}
