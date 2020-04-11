package sh.isaac.api.coordinate;

public interface EditCoordinateProxy extends EditCoordinate {

    EditCoordinate getEditCoordinate();

    @Override
    default int getAuthorNid() {
        return getEditCoordinate().getAuthorNid();
    }

    @Override
    default int getModuleNid() {
        return getEditCoordinate().getModuleNid();
    }

    @Override
    default int getPathNid() {
        return getEditCoordinate().getPathNid();
    }
}
