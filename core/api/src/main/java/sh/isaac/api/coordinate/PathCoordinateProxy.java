package sh.isaac.api.coordinate;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.Collection;
import java.util.UUID;

public interface PathCoordinateProxy extends PathCoordinate {
    /**
     * Gets the path coordinate.
     *
     * @return a PathCoordinate that specifies the retrieval and display of
     * object chronicle versions by indicating the current position on a path, and allowed modules.
     */
    PathCoordinate getPathCoordinate();

    @Override
    default ImmutableIntSet getModuleNids() {
        return getPathCoordinate().getModuleNids();
    }

    @Override
    default PathCoordinate makeModuleAnalog(Collection<ConceptSpecification> modules) {
        return getPathCoordinate().makeModuleAnalog(modules);
    }

    @Override
    default PathCoordinate makePathAnalog(ConceptSpecification pathForPosition) {
        return getPathCoordinate().makePathAnalog(pathForPosition);
    }

    @Override
    default ConceptSpecification getPathConceptForCoordinate() {
        return this.getPathCoordinate().getPathConceptForCoordinate();
    }

    @Override
    default int getPathNidForCoordinate() {
        return this.getPathCoordinate().getPathNidForCoordinate();
    }

    @Override
    default UUID getPathCoordinateUuid() {
        return this.getPathCoordinate().getPathCoordinateUuid();
    }

    @Override
    default ImmutableSet<ConceptSpecification> getModuleSpecifications() {
        return this.getPathCoordinate().getModuleSpecifications();
    }

}
