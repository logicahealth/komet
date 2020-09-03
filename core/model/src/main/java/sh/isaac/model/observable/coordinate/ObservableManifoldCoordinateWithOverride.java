package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.*;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;

public class ObservableManifoldCoordinateWithOverride extends ObservableManifoldCoordinateBase {

    public ObservableManifoldCoordinateWithOverride(ObservableManifoldCoordinateBase manifoldCoordinate) {
        super(manifoldCoordinate);
        if (manifoldCoordinate instanceof ObservableManifoldCoordinateWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
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
    public ManifoldCoordinateImmutable makeCoordinateAnalog(long classifyTimeInEpochMillis) {
        return getValue().makeCoordinateAnalog(classifyTimeInEpochMillis);
    }

    @Override
    public void setExceptOverrides(ManifoldCoordinateImmutable updatedCoordinate) {
        if (hasOverrides()) {
            VertexSort vertexSort = updatedCoordinate.getVertexSort();
            if (vertexSortProperty().isOverridden()) {
                vertexSort = getVertexSort();
            }
            StatusSet vertexStatusSet = updatedCoordinate.getVertexStatusSet();
            if (vertexStatusSetProperty().isOverridden()) {
                vertexStatusSet = getVertexStatusSet();
            }

            StampFilterImmutable edgeStampFilter = updatedCoordinate.getViewStampFilter();
            if (getViewStampFilter().hasOverrides()) {
                ObservableStampFilter filter = getViewStampFilter();
                filter.setExceptOverrides(edgeStampFilter);
                edgeStampFilter = filter.toStampFilterImmutable();
            }
            LanguageCoordinateImmutable languageCoordinate = updatedCoordinate.getLanguageCoordinate();
            if (getLanguageCoordinate().hasOverrides()) {
                ObservableLanguageCoordinate coordinate = getLanguageCoordinate();
                coordinate.setExceptOverrides(languageCoordinate);
                languageCoordinate = coordinate.toLanguageCoordinateImmutable();
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
                    vertexSort,
                    vertexStatusSet,
                    navigationCoordinate,
                    logicCoordinate, Activity.DEVELOPING, Coordinates.Edit.Default()));
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
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObservableLogicCoordinateWithOverride(observableManifoldCoordinate.getLogicCoordinate());
    }

    @Override
    protected ObservableNavigationCoordinateBase makeNavigationCoordinateProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObservableNavigationCoordinateWithOverride(observableManifoldCoordinate.getNavigationCoordinate());
    }

    @Override
    protected ObjectPropertyWithOverride<StatusSet> makeVertexStatusSetProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObjectPropertyWithOverride(observableManifoldCoordinate.vertexStatusSetProperty(), this);
    }

    @Override
    protected ObservableStampFilterBase makeEdgeStampFilterProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObservableStampFilterWithOverride(observableManifoldCoordinate.getViewStampFilter());
    }
    @Override
    protected SimpleObjectProperty<VertexSort> makeVertexSortProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObjectPropertyWithOverride<>(observableManifoldCoordinate.vertexSortProperty(), this);
    }

    @Override
    protected SimpleObjectProperty<Activity> makeActivityProperty(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObjectPropertyWithOverride<>(observableManifoldCoordinate.activityProperty(), this);
    }

    @Override
    public ObjectPropertyWithOverride<Activity> activityProperty() {
        return (ObjectPropertyWithOverride) super.activityProperty();
    }

    @Override
    protected ObservableLanguageCoordinateBase makeLanguageCoordinate(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObservableLanguageCoordinateWithOverride(observableManifoldCoordinate.getLanguageCoordinate());
    }

    @Override
    protected ObservableEditCoordinateBase makeEditCoordinate(ManifoldCoordinate manifoldCoordinate) {
        ObservableManifoldCoordinate observableManifoldCoordinate = (ObservableManifoldCoordinate) manifoldCoordinate;
        return new ObservableEditCoordinateWithOverride(observableManifoldCoordinate.getEditCoordinate());
    }


    @Override
    public ObjectPropertyWithOverride<VertexSort> vertexSortProperty() {
        return (ObjectPropertyWithOverride) super.vertexSortProperty();
    }

    @Override
    public ObjectPropertyWithOverride<StatusSet> vertexStatusSetProperty() {
        return (ObjectPropertyWithOverride) super.vertexStatusSetProperty();
    }

    @Override
    public ManifoldCoordinateImmutable getOriginalValue() {
        return ManifoldCoordinateImmutable.make(
                getViewStampFilter().getOriginalValue(),
                getLanguageCoordinate().getOriginalValue(),
                vertexSortProperty().getOriginalValue(),
                vertexStatusSetProperty().getOriginalValue(),
                getNavigationCoordinate().getOriginalValue(),
                getLogicCoordinate().getOriginalValue(),
                activityProperty().getOriginalValue(),
                getEditCoordinate().getOriginalValue());
    }

    @Override
    protected ManifoldCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends ManifoldCoordinateImmutable> observable, ManifoldCoordinateImmutable oldValue, ManifoldCoordinateImmutable newValue) {
        this.navigationCoordinateObservable.setExceptOverrides(newValue.toNavigationCoordinateImmutable());
        this.languageCoordinateObservable.setExceptOverrides(newValue.getLanguageCoordinate().toLanguageCoordinateImmutable());
        this.edgeStampFilterObservable.setExceptOverrides(newValue.getViewStampFilter().toStampFilterImmutable());
        this.editCoordinateObservable.setExceptOverrides(newValue.getEditCoordinate().toEditCoordinateImmutable());
        this.logicCoordinateObservable.setExceptOverrides(newValue.getLogicCoordinate().toLogicCoordinateImmutable());

        if (!this.vertexStatusSetProperty().isOverridden()) {
            this.vertexStatusSetObservable.setValue(newValue.getVertexStatusSet());
        }
        if (!this.vertexSortProperty().isOverridden()) {
            this.vertexSortProperty.setValue(newValue.getVertexSort());
        }
        if (!this.activityProperty().isOverridden()) {
            this.activityProperty.setValue(newValue.getCurrentActivity());
        }
        this.activityProperty.setValue(newValue.getCurrentActivity());
        return ManifoldCoordinateImmutable.make(edgeStampFilterObservable.getValue(),
                this.languageCoordinateObservable.getValue(),
                this.vertexSortProperty().get(),
                this.vertexStatusSetProperty().get(),
                this.navigationCoordinateObservable.getValue(),
                this.logicCoordinateObservable.getValue(),
                this.activityProperty().getValue(),
                this.editCoordinateObservable.getValue());
    }

}
