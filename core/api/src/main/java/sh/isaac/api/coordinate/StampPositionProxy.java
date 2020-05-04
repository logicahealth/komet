package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.ImmutableSet;

public interface StampPositionProxy extends StampPosition {
    StampPosition getStampPosition();

    @Override
    default long getTime() {
        return getStampPosition().getTime();
    }

    @Override
    default int getPathForPositionNid() {
        return getStampPosition().getPathForPositionNid();
    }

    @Override
    default StampPositionImmutable toStampPositionImmutable() {
        return getStampPosition().toStampPositionImmutable();
    }
}
