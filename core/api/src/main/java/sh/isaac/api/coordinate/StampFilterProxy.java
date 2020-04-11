package sh.isaac.api.coordinate;

import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

public interface StampFilterProxy extends StampFilter {

    StampFilter getStampFilter();

    @Override
    default int getPathNidForFilter() {
        return getStampFilter().getPathNidForFilter();
    }

    @Override
    default StatusSet getAllowedStates() {
        return getStampFilter().getAllowedStates();
    }

    @Override
    default ImmutableIntSet getModuleNids() {
        return getStampFilter().getModuleNids();
    }

    @Override
    default ImmutableIntList getModulePreferenceOrder() {
        return getStampFilter().getModulePreferenceOrder();
    }

    @Override
    default StampPosition getStampPosition() {
        return getStampFilter().getStampPosition();
    }

    @Override
    default RelativePositionCalculator getRelativePositionCalculator() {
        return getStampFilter().getRelativePositionCalculator();
    }

    @Override
    default StampFilter makeCoordinateAnalog(StatusSet statusSet) {
        return getStampFilter().makeCoordinateAnalog(statusSet);
    }

    @Override
    default StampFilter makeCoordinateAnalog(long stampPositionTime) {
        return getStampFilter().makeCoordinateAnalog(stampPositionTime);
    }
}
