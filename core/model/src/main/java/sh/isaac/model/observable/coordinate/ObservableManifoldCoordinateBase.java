package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.*;

public abstract class ObservableManifoldCoordinateBase
        extends ObservableCoordinateImpl<ManifoldCoordinateImmutable>
        implements ObservableManifoldCoordinate {

    private final ObservableNavigationCoordinateBase observableNavigationCoordinate;
    /**
     *
     * The vertexSort property.
     */
    private final SimpleObjectProperty<VertexSort> vertexSortProperty;

    private final ObservableStampFilterBase vertexStampFilterObservable;

    private final ObservableStampFilterBase edgeStampFilterObservable;

    private final ObservableStampFilterBase languageStampFilterObservable;

    private final ObservableLanguageCoordinateBase languageCoordinateObservable;
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<NavigationCoordinateImmutable> digraphListener = this::digraphChanged;
    private final ChangeListener<StampFilterImmutable> edgeStampFilterListener = this::edgeFilterChanged;
    private final ChangeListener<StampFilterImmutable> vertexStampFilterListener = this::vertexFilterChanged;
    private final ChangeListener<StampFilterImmutable> languageStampFilterListener = this::languageFilterChanged;
    private final ChangeListener<LanguageCoordinateImmutable> languageCoordinateListener = this::languageCoordinateChanged;
    private final ChangeListener<VertexSort> vertexSortChangeListener = this::vertexSortChanged;

    //~--- constructors --------------------------------------------------------
    public ObservableManifoldCoordinateBase(ManifoldCoordinate manifoldCoordinate, String name) {
        super(manifoldCoordinate.toManifoldCoordinateImmutable(), name);
        this.observableNavigationCoordinate = makeNavigationCoordinateProperty(manifoldCoordinate);
        this.languageCoordinateObservable = makeLanguageCoordinate(manifoldCoordinate);
        this.vertexSortProperty = makeVertexSortProperty(manifoldCoordinate);
        this.edgeStampFilterObservable = makeEdgeStampFilterProperty(manifoldCoordinate);
        this.vertexStampFilterObservable = makeVertexStampFilterProperty(manifoldCoordinate);
        this.languageStampFilterObservable = makeLanguageStampFilterProperty(manifoldCoordinate);
        addListeners();
    }

    /**
     * Instantiates a new observable taxonomy coordinate impl.
     *
     * @param manifoldCoordinate the taxonomy coordinate
     */
    public ObservableManifoldCoordinateBase(ManifoldCoordinate manifoldCoordinate) {
        this(manifoldCoordinate, "Manifold");
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends ManifoldCoordinateImmutable> observable, ManifoldCoordinateImmutable oldValue, ManifoldCoordinateImmutable newValue) {
        this.observableNavigationCoordinate.baseCoordinateProperty().setValue(newValue.toNavigationCoordinateImmutable());
        this.languageCoordinateObservable.setValue(newValue.getLanguageCoordinate().toLanguageCoordinateImmutable());
        this.edgeStampFilterObservable.setValue(newValue.getEdgeStampFilter().toStampFilterImmutable());
        this.vertexStampFilterObservable.setValue(newValue.getVertexStampFilter().toStampFilterImmutable());
        this.languageStampFilterObservable.setValue(newValue.getLanguageStampFilter().toStampFilterImmutable());
    }

    @Override
    protected void addListeners() {
        this.observableNavigationCoordinate.baseCoordinateProperty().addListener(this.digraphListener);
        this.languageCoordinateObservable.baseCoordinateProperty().addListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().addListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.addListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().addListener(this.languageStampFilterListener);
        this.vertexSortProperty.addListener(this.vertexSortChangeListener);
    }

    @Override
    protected void removeListeners() {
        this.observableNavigationCoordinate.baseCoordinateProperty().removeListener(this.digraphListener);
        this.languageCoordinateObservable.baseCoordinateProperty().addListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().addListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.addListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().addListener(this.languageStampFilterListener);
        this.vertexSortProperty.removeListener(this.vertexSortChangeListener);
    }

    protected abstract ObservableNavigationCoordinateBase makeNavigationCoordinateProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeLanguageStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeVertexStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeEdgeStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract SimpleObjectProperty<VertexSort> makeVertexSortProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableLanguageCoordinateBase makeLanguageCoordinate(ManifoldCoordinate manifoldCoordinate);

    //~--- methods -------------------------------------------------------------


    private void languageFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                       StampFilterImmutable oldValue,
                                       StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                newValue,
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable()));
    }

    private void vertexFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                     StampFilterImmutable oldValue,
                                     StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                newValue,
                getNavigationCoordinate().toNavigationCoordinateImmutable()));
    }

    private void vertexSortChanged(ObservableValue<? extends VertexSort> observable,
                                   VertexSort oldValue,
                                   VertexSort newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexStampFilter().toStampFilterImmutable(),
                newValue,
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable()));
    }


    private void edgeFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                   StampFilterImmutable oldValue,
                                   StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                newValue,
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable()));
    }

    private void languageCoordinateChanged(ObservableValue<? extends LanguageCoordinateImmutable> observable,
                                           LanguageCoordinateImmutable oldValue,
                                           LanguageCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                newValue,
                getVertexStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable()));
    }

    private void digraphChanged(ObservableValue<? extends NavigationCoordinateImmutable> observable,
                                NavigationCoordinateImmutable oldValue,
                                NavigationCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                newValue));
    }

    @Override
    public ObjectProperty<NavigationCoordinateImmutable> navigationCoordinateImmutableProperty() {
        return observableNavigationCoordinate.baseCoordinateProperty();
    }

    @Override
    public ObservableNavigationCoordinate getNavigationCoordinate() {
        return this.observableNavigationCoordinate;
    }

    @Override
    public ObservableLogicCoordinate getLogicCoordinate() {
        return this.observableNavigationCoordinate.getLogicCoordinate();
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return this.languageCoordinateObservable;
    }

    @Override
    public ObservableStampFilter getLanguageStampFilter() {
        return this.languageStampFilterObservable;
    }

    @Override
    public ObservableStampFilter getVertexStampFilter() {
        return this.vertexStampFilterObservable;
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this.getValue();
    }

    @Override
    public ObservableStampFilter getEdgeStampFilter() {
        return this.edgeStampFilterObservable;
    }

    @Override
    public ObjectProperty<StampFilterImmutable> vertexStampFilterProperty() {
        return vertexStampFilterObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<StampFilterImmutable> edgeStampFilterProperty() {
        return edgeStampFilterObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<StampFilterImmutable> languageStampFilterProperty() {
        return languageStampFilterObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<VertexSort> vertexSortProperty() {
        return this.vertexSortProperty;
    }

    @Override
    public VertexSort getVertexSort() {
        return this.vertexSortProperty.get();
    }

    @Override
    public ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty() {
        return this.languageCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public TaxonomySnapshot getNavigationSnapshot() {
        return getValue().getNavigationSnapshot();
    }

}