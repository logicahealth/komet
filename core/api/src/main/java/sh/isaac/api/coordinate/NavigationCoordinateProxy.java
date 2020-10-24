package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;

public interface NavigationCoordinateProxy extends NavigationCoordinate {
    NavigationCoordinate getDigraph();

    @Override
    default ImmutableIntSet getNavigationConceptNids() {
        return getDigraph().getNavigationConceptNids();
    }

    @Override
    default NavigationCoordinateImmutable toNavigationCoordinateImmutable() {
        return getDigraph().toNavigationCoordinateImmutable();
    }

}
