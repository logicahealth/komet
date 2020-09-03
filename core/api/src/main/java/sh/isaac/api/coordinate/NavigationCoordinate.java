package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.UUIDUtil;

import java.util.ArrayList;
import java.util.UUID;

/**
 * In mathematics, and more specifically in graph theory, a directed graph (or digraph)
 * is a graph that is made up of a set of vertices connected by edges, where the edges
 * have a direction associated with them.
 *
 * TODO change Graph NODE to Vertex everywhere since node is overloaded with JavaFx Node (which means something else)...
 */
public interface NavigationCoordinate {

    static UUID getNavigationCoordinateUuid(NavigationCoordinate navigationCoordinate) {
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (int nid: navigationCoordinate.getNavigationConceptNids().toArray()) {
            UUIDUtil.addSortedUuids(uuidList, nid);
        }
        return UUID.nameUUIDFromBytes(uuidList.toString().getBytes());
    }


    static ImmutableIntSet defaultNavigationConceptIdentifierNids() {
        return IntSets.immutable.of(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid());
    }

    default UUID getNavigationCoordinateUuid() {
        return getNavigationCoordinateUuid(this);
    }

    //---------------------------

    ImmutableIntSet getNavigationConceptNids();

    default ImmutableSet<ConceptSpecification> getNavigationIdentifierConcepts() {
        return IntSets.immutable.of(getNavigationConceptNids().toArray()).collect(nid -> Get.conceptSpecification(nid));
    }

    NavigationCoordinateImmutable toNavigationCoordinateImmutable();

    default String toUserString() {
        StringBuilder sb = new StringBuilder("Navigators: ");
        for (int nid: getNavigationConceptNids().toArray()) {
            sb.append("\n     ").append(Get.conceptDescriptionText(nid));
        }
        return sb.toString();
    }

}
