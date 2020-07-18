package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.*;

public abstract class ObservableManifoldCoordinateBase
        extends ObservableCoordinateImpl<ManifoldCoordinateImmutable>
        implements ObservableManifoldCoordinate {

    protected final SimpleBooleanProperty listening = new SimpleBooleanProperty(this, "Listening for changes", false);

    protected final ObservableNavigationCoordinateBase navigationCoordinateObservable;

    protected final ObservableLogicCoordinateBase logicCoordinateObservable;

    /**
     *
     * The vertexSort property.
     */
    protected final SimpleObjectProperty<VertexSort> vertexSortProperty;

    protected final ObservableStampFilterBase vertexStampFilterObservable;

    protected final ObservableStampFilterBase edgeStampFilterObservable;

    protected final ObservableStampFilterBase languageStampFilterObservable;

    protected final ObservableLanguageCoordinateBase languageCoordinateObservable;
    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<NavigationCoordinateImmutable> navigationChanged = this::navigationChanged;
    private final ChangeListener<StampFilterImmutable> edgeStampFilterListener = this::edgeFilterChanged;
    private final ChangeListener<StampFilterImmutable> vertexStampFilterListener = this::vertexFilterChanged;
    private final ChangeListener<StampFilterImmutable> languageStampFilterListener = this::languageFilterChanged;
    private final ChangeListener<LanguageCoordinateImmutable> languageCoordinateListener = this::languageCoordinateChanged;
    private final ChangeListener<VertexSort> vertexSortChangeListener = this::vertexSortChanged;
    private final ChangeListener<LogicCoordinateImmutable> logicCoordinateListener = this::logicCoordinateChanged;


    //~--- constructors --------------------------------------------------------
    public ObservableManifoldCoordinateBase(ManifoldCoordinate manifoldCoordinate, String name) {
        super(manifoldCoordinate.toManifoldCoordinateImmutable(), name);
        this.navigationCoordinateObservable = makeNavigationCoordinateProperty(manifoldCoordinate);
        this.languageCoordinateObservable = makeLanguageCoordinate(manifoldCoordinate);
        this.vertexSortProperty = makeVertexSortProperty(manifoldCoordinate);
        this.edgeStampFilterObservable = makeEdgeStampFilterProperty(manifoldCoordinate);
        this.vertexStampFilterObservable = makeVertexStampFilterProperty(manifoldCoordinate);
        this.languageStampFilterObservable = makeLanguageStampFilterProperty(manifoldCoordinate);
        this.logicCoordinateObservable = makeLogicCoordinate(manifoldCoordinate);
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
    public void changeManifoldPath(ConceptSpecification pathConcept) {
        this.removeListeners();
        getEdgeStampFilter().pathConceptProperty().set(pathConcept);
        getLanguageStampFilter().pathConceptProperty().set(pathConcept);
        getVertexStampFilter().pathConceptProperty().set(pathConcept);
        ManifoldCoordinateImmutable manifoldCoordinateImmutable = ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable());
        this.addListeners();
        this.setValue(manifoldCoordinateImmutable);
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends ManifoldCoordinateImmutable> observable, ManifoldCoordinateImmutable oldValue, ManifoldCoordinateImmutable newValue) {
        this.navigationCoordinateObservable.baseCoordinateProperty().setValue(newValue.toNavigationCoordinateImmutable());
        this.languageCoordinateObservable.setValue(newValue.getLanguageCoordinate().toLanguageCoordinateImmutable());
        this.edgeStampFilterObservable.setValue(newValue.getEdgeStampFilter().toStampFilterImmutable());
        this.vertexStampFilterObservable.setValue(newValue.getVertexStampFilter().toStampFilterImmutable());
        this.languageStampFilterObservable.setValue(newValue.getLanguageStampFilter().toStampFilterImmutable());
    }

    @Override
    protected void addListeners() {
        this.navigationCoordinateObservable.baseCoordinateProperty().addListener(this.navigationChanged);
        this.languageCoordinateObservable.baseCoordinateProperty().addListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().addListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.baseCoordinateProperty().addListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().addListener(this.languageStampFilterListener);
        this.logicCoordinateObservable.baseCoordinateProperty().addListener(this.logicCoordinateListener);
        this.vertexSortProperty.addListener(this.vertexSortChangeListener);
        listening.set(true);
    }

    @Override
    protected void removeListeners() {
        this.navigationCoordinateObservable.baseCoordinateProperty().removeListener(this.navigationChanged);
        this.languageCoordinateObservable.baseCoordinateProperty().removeListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().removeListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.baseCoordinateProperty().removeListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().removeListener(this.languageStampFilterListener);
        this.logicCoordinateObservable.baseCoordinateProperty().removeListener(this.logicCoordinateListener);
        this.vertexSortProperty.removeListener(this.vertexSortChangeListener);
        listening.set(false);
    }

    protected abstract ObservableNavigationCoordinateBase makeNavigationCoordinateProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeLanguageStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeVertexStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeEdgeStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract SimpleObjectProperty<VertexSort> makeVertexSortProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableLanguageCoordinateBase makeLanguageCoordinate(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableLogicCoordinateBase makeLogicCoordinate(ManifoldCoordinate manifoldCoordinate);

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
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }

    private void vertexFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                     StampFilterImmutable oldValue,
                                     StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                newValue,
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }

    private void vertexSortChanged(ObservableValue<? extends VertexSort> observable,
                                   VertexSort oldValue,
                                   VertexSort newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                newValue,
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }


    private void edgeFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                   StampFilterImmutable oldValue,
                                   StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                newValue,
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }

    private void languageCoordinateChanged(ObservableValue<? extends LanguageCoordinateImmutable> observable,
                                           LanguageCoordinateImmutable oldValue,
                                           LanguageCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                newValue,
                getLanguageStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }

    private void navigationChanged(ObservableValue<? extends NavigationCoordinateImmutable> observable,
                                   NavigationCoordinateImmutable oldValue,
                                   NavigationCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                newValue,
                getLogicCoordinate().toLogicCoordinateImmutable()));
    }

    private void logicCoordinateChanged(ObservableValue<? extends LogicCoordinateImmutable> observable,
                                        LogicCoordinateImmutable oldValue,
                                        LogicCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                newValue));
    }


    @Override
    public ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty() {
        return logicCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public ObservableLogicCoordinate getLogicCoordinate() {
        return this.logicCoordinateObservable;
    }

    @Override
    public ObjectProperty<NavigationCoordinateImmutable> navigationCoordinateImmutableProperty() {
        return navigationCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public ObservableNavigationCoordinate getNavigationCoordinate() {
        return this.navigationCoordinateObservable;
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{\n" +
                getValue().toString() +
                "\n}";
    }
}