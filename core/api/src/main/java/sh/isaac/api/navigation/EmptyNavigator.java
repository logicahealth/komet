package sh.isaac.api.navigation;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import sh.isaac.api.Edge;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;

import java.util.ArrayList;

public class EmptyNavigator implements Navigator {
        final ArrayList<Navigator> navigators = new ArrayList<>();
        final ArrayList<Navigator> reverseNavigators = new ArrayList<>();
        final ArrayList<ConceptSpecification> roots = new ArrayList<>();
        final ManifoldCoordinateImmutable manifoldCoordinate;

    public EmptyNavigator(ManifoldCoordinate manifoldCoordinate) {
            if (manifoldCoordinate == null) {
                throw new NullPointerException("manifoldCoordinate cannot be null. ");
            }
            this.manifoldCoordinate = manifoldCoordinate.toManifoldCoordinateImmutable();
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
            return new int[0];
        }


        @Override
        public int[] getParentNids(int childNid) {
            return new int[0];
        }


        @Override
        public int[] getRootNids() {
             return new int[] {TermAux.UNINITIALIZED_COMPONENT_ID.getNid()};
        }

        @Override
        public boolean isChildOf(int childNid, int parentNid) {
             return false;
        }

        @Override
        public boolean isLeaf(int conceptNid) {
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
            return Lists.immutable.empty();
        }

        @Override
        public ImmutableCollection<Edge> getChildLinks(int childConceptNid) {
            return Lists.immutable.empty();
        }

        public void reset() {
            this.navigators.clear();
            this.reverseNavigators.clear();
            this.roots.clear();
        }
}
