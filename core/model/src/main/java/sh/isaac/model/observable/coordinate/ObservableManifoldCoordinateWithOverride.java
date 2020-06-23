package sh.isaac.model.observable.coordinate;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.VertexSort;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableManifoldCoordinateWithOverride extends ObservableManifoldCoordinateBase {
    public ObservableManifoldCoordinateWithOverride(ManifoldCoordinate manifoldCoordinate, String name) {
        super(manifoldCoordinate, name);
    }

    public ObservableManifoldCoordinateWithOverride(ManifoldCoordinate manifoldCoordinate) {
        super(manifoldCoordinate);
    }

    @Override
    protected ObservableNavigationCoordinateBase makeNavigationCoordinateProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableNavigationCoordinateWithOverride(observableManifoldCoordinate.getNavigationCoordinate());
    }

    @Override
    protected ObservableStampFilterBase makeLanguageStampFilterProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableStampFilterWithOverride(observableManifoldCoordinate.getLanguageStampFilter());
    }

    @Override
    protected ObservableStampFilterBase makeVertexStampFilterProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableStampFilterWithOverride(observableManifoldCoordinate.getVertexStampFilter());
    }

    @Override
    protected ObservableStampFilterBase makeEdgeStampFilterProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableStampFilterWithOverride(observableManifoldCoordinate.getEdgeStampFilter());
    }

    @Override
    protected SimpleObjectProperty<VertexSort> makeVertexSortProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObjectPropertyWithOverride<>(observableManifoldCoordinate.vertexSortProperty(), this);
    }

    @Override
    protected ObservableLanguageCoordinateBase makeLanguageCoordinate(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableLanguageCoordinateWithOverride(observableManifoldCoordinate.getLanguageCoordinate());
    }
}
