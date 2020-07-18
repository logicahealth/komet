package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.api.observable.coordinate.PropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableManifoldCoordinateWithOverride extends ObservableManifoldCoordinateBase {

    public ObservableManifoldCoordinateWithOverride(ObservableManifoldCoordinateBase manifoldCoordinate) {
        super(manifoldCoordinate);
        manifoldCoordinate.baseCoordinateProperty().addListener(this::overriddenBaseChanged);
        manifoldCoordinate.listening.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.addListeners();
            } else {
                this.removeListeners();
            }

        });
    }

    @Override
    public void setExceptOverrides(ManifoldCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            VertexSort vertexSort = updatedCoordinate.getVertexSort();
            if (vertexSortProperty().isOverridden()) {
                vertexSort = getVertexSort();
            }
            StampFilterImmutable edgeStampFilter = updatedCoordinate.getEdgeStampFilter();
            if (getEdgeStampFilter().hasOverrides()) {
                ObservableStampFilter filter = getEdgeStampFilter();
                filter.setExceptOverrides(edgeStampFilter);
                edgeStampFilter = filter.toStampFilterImmutable();
            }
            LanguageCoordinateImmutable languageCoordinate = updatedCoordinate.getLanguageCoordinate();
            if (getLanguageCoordinate().hasOverrides()) {
                ObservableLanguageCoordinate coordinate = getLanguageCoordinate();
                coordinate.setExceptOverrides(languageCoordinate);
                languageCoordinate = coordinate.toLanguageCoordinateImmutable();
            }
            StampFilterImmutable languageStampFilter = updatedCoordinate.getLanguageStampFilter();
            if (getLanguageStampFilter().hasOverrides()) {
                ObservableStampFilter filter = getLanguageStampFilter();
                filter.setExceptOverrides(languageStampFilter);
                languageStampFilter = filter.toStampFilterImmutable();
            }
            StampFilterImmutable vertexStampFilter = updatedCoordinate.getVertexStampFilter();
            if (getVertexStampFilter().hasOverrides()) {
                ObservableStampFilter filter = getVertexStampFilter();
                filter.setExceptOverrides(vertexStampFilter);
                vertexStampFilter = filter.toStampFilterImmutable();
            }
            NavigationCoordinateImmutable navigationCoordinate = updatedCoordinate.getNavigationCoordinate();
            if (getNavigationCoordinate().hasOverrides()) {
                navigationCoordinate = getNavigationCoordinate().toNavigationCoordinateImmutable();
            }
            LogicCoordinateImmutable logicCoordinate = updatedCoordinate.getLogicCoordinate();
            if (getLogicCoordinate().hasOverrides()) {
                ObservableLogicCoordinate coordinate = getLogicCoordinate();
                coordinate.setExceptOverrides(logicCoordinate);
                logicCoordinate = coordinate.toLogicCoordinateImmutable();
            }
            this.setValue(ManifoldCoordinateImmutable.make(edgeStampFilter,
                    languageCoordinate,
                    languageStampFilter,
                    vertexSort,
                    vertexStampFilter,
                    navigationCoordinate,
                    logicCoordinate));
        } else {
            this.setValue(updatedCoordinate);
        }
    }

    private void overriddenBaseChanged(ObservableValue<? extends ManifoldCoordinateImmutable> observableValue,
                                       ManifoldCoordinateImmutable oldValue,
                                       ManifoldCoordinateImmutable newValue) {
        if (this.hasOverrides()) {
            setExceptOverrides(newValue);
        } else {
            setValue(newValue);
        }

    }


    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinate(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinateImpl observableManifoldCoordinate = (ObservableManifoldCoordinateImpl) manifoldCoordinate;
        return new ObservableLogicCoordinateWithOverride(observableManifoldCoordinate.getLogicCoordinate());
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
    public ObjectPropertyWithOverride<VertexSort> vertexSortProperty() {
        return (ObjectPropertyWithOverride) super.vertexSortProperty();
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
