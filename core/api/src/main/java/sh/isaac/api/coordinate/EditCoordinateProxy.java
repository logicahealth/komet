package sh.isaac.api.coordinate;

public interface EditCoordinateProxy extends EditCoordinate {

    EditCoordinate getEditCoordinate();

    @Override
    default int getAuthorNidForChanges() {
        return getEditCoordinate().getAuthorNidForChanges();
    }

    @Override
    default int getDefaultModuleNid() {
        return getEditCoordinate().getDefaultModuleNid();
    }

    @Override
    default int getDestinationModuleNid() {
        return getEditCoordinate().getDestinationModuleNid();
    }

    @Override
    default EditCoordinateImmutable toEditCoordinateImmutable() {
        return getEditCoordinate().toEditCoordinateImmutable();
    }

    @Override
    default int getPromotionPathNid() {
        return getEditCoordinate().getPromotionPathNid();
    }
}
