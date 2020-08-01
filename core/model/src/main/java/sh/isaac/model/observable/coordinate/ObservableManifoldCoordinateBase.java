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

    protected final ObservableLanguageCoordinateBase languageCoordinateObservable;

    protected final SimpleObjectProperty<Activity> activityProperty;

    protected final ObservableEditCoordinateBase editCoordinateObservable;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<NavigationCoordinateImmutable> navigationChanged = this::navigationChanged;
    private final ChangeListener<StampFilterImmutable> edgeStampFilterListener = this::edgeFilterChanged;
    private final ChangeListener<StampFilterImmutable> vertexStampFilterListener = this::vertexFilterChanged;
    private final ChangeListener<LanguageCoordinateImmutable> languageCoordinateListener = this::languageCoordinateChanged;
    private final ChangeListener<VertexSort> vertexSortChangeListener = this::vertexSortChanged;
    private final ChangeListener<LogicCoordinateImmutable> logicCoordinateListener = this::logicCoordinateChanged;
    private final ChangeListener<Activity> activityChangeListener = this::activityChanged;
    private final ChangeListener<EditCoordinateImmutable> editCoordinateListener = this::editCoordinateChanged;


    //~--- constructors --------------------------------------------------------
    public ObservableManifoldCoordinateBase(ManifoldCoordinate manifoldCoordinate, String name) {
        super(manifoldCoordinate.toManifoldCoordinateImmutable(), name);
        this.navigationCoordinateObservable = makeNavigationCoordinateProperty(manifoldCoordinate);
        this.languageCoordinateObservable = makeLanguageCoordinate(manifoldCoordinate);
        this.vertexSortProperty = makeVertexSortProperty(manifoldCoordinate);
        this.edgeStampFilterObservable = makeEdgeStampFilterProperty(manifoldCoordinate);
        this.vertexStampFilterObservable = makeVertexStampFilterProperty(manifoldCoordinate);
        this.logicCoordinateObservable = makeLogicCoordinate(manifoldCoordinate);
        this.activityProperty = makeActivityProperty(manifoldCoordinate);
        this.editCoordinateObservable = makeEditCoordinate(manifoldCoordinate);
        addListeners();
    }

    protected abstract ObservableEditCoordinateBase makeEditCoordinate(ManifoldCoordinate manifoldCoordinate);

    protected abstract SimpleObjectProperty<Activity> makeActivityProperty(ManifoldCoordinate manifoldCoordinate);

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
        getViewStampFilter().pathConceptProperty().set(pathConcept);
        getVertexStampFilter().pathConceptProperty().set(pathConcept);
        ManifoldCoordinateImmutable manifoldCoordinateImmutable = ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate());
        this.addListeners();
        this.setValue(manifoldCoordinateImmutable);
    }

    private void editCoordinateChanged(ObservableValue<? extends EditCoordinateImmutable> observableValue, EditCoordinateImmutable oldEditCoordinate, EditCoordinateImmutable newEditCoordinate) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                newEditCoordinate));
    }

    private void activityChanged(ObservableValue<? extends Activity> observableValue, Activity oldActivity, Activity newActivity) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                newActivity,
                getEditCoordinate()));
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends ManifoldCoordinateImmutable> observable, ManifoldCoordinateImmutable oldValue, ManifoldCoordinateImmutable newValue) {
        this.navigationCoordinateObservable.baseCoordinateProperty().setValue(newValue.toNavigationCoordinateImmutable());
        this.languageCoordinateObservable.setValue(newValue.getLanguageCoordinate().toLanguageCoordinateImmutable());
        this.edgeStampFilterObservable.setValue(newValue.getViewStampFilter().toStampFilterImmutable());
        this.vertexStampFilterObservable.setValue(newValue.getVertexStampFilter().toStampFilterImmutable());
        this.logicCoordinateObservable.setValue(newValue.getLogicCoordinate().toLogicCoordinateImmutable());
        this.vertexSortProperty.setValue(newValue.getVertexSort());
        this.activityProperty.setValue(newValue.getCurrentActivity());
        this.editCoordinateObservable.setValue(newValue.getEditCoordinate().toEditCoordinateImmutable());
    }

    @Override
    protected void addListeners() {
        this.navigationCoordinateObservable.baseCoordinateProperty().addListener(this.navigationChanged);
        this.languageCoordinateObservable.baseCoordinateProperty().addListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().addListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.baseCoordinateProperty().addListener(this.vertexStampFilterListener);
        this.logicCoordinateObservable.baseCoordinateProperty().addListener(this.logicCoordinateListener);
        this.vertexSortProperty.addListener(this.vertexSortChangeListener);
        this.activityProperty.addListener(this.activityChangeListener);
        this.editCoordinateObservable.baseCoordinateProperty().addListener(this.editCoordinateListener);
        listening.set(true);
    }

    @Override
    protected void removeListeners() {
        this.navigationCoordinateObservable.baseCoordinateProperty().removeListener(this.navigationChanged);
        this.languageCoordinateObservable.baseCoordinateProperty().removeListener(this.languageCoordinateListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().removeListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.baseCoordinateProperty().removeListener(this.vertexStampFilterListener);
        this.logicCoordinateObservable.baseCoordinateProperty().removeListener(this.logicCoordinateListener);
        this.vertexSortProperty.removeListener(this.vertexSortChangeListener);
        this.activityProperty.removeListener(this.activityChangeListener);
        this.editCoordinateObservable.baseCoordinateProperty().removeListener(this.editCoordinateListener);
        listening.set(false);
    }

    protected abstract ObservableNavigationCoordinateBase makeNavigationCoordinateProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeVertexStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableStampFilterBase makeEdgeStampFilterProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract SimpleObjectProperty<VertexSort> makeVertexSortProperty(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableLanguageCoordinateBase makeLanguageCoordinate(ManifoldCoordinate manifoldCoordinate);

    protected abstract ObservableLogicCoordinateBase makeLogicCoordinate(ManifoldCoordinate manifoldCoordinate);

    //~--- methods -------------------------------------------------------------

    private void vertexFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                     StampFilterImmutable oldValue,
                                     StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                newValue,
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate()));
    }

    private void vertexSortChanged(ObservableValue<? extends VertexSort> observable,
                                   VertexSort oldValue,
                                   VertexSort newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                newValue,
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate()));
    }


    private void edgeFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                   StampFilterImmutable oldValue,
                                   StampFilterImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                newValue,
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate()));
    }

    private void languageCoordinateChanged(ObservableValue<? extends LanguageCoordinateImmutable> observable,
                                           LanguageCoordinateImmutable oldValue,
                                           LanguageCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                newValue,
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate()));
    }

    private void navigationChanged(ObservableValue<? extends NavigationCoordinateImmutable> observable,
                                   NavigationCoordinateImmutable oldValue,
                                   NavigationCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                newValue,
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getCurrentActivity(),
                getEditCoordinate()));
    }

    private void logicCoordinateChanged(ObservableValue<? extends LogicCoordinateImmutable> observable,
                                        LogicCoordinateImmutable oldValue,
                                        LogicCoordinateImmutable newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(
                getViewStampFilter().toStampFilterImmutable(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getNavigationCoordinate().toNavigationCoordinateImmutable(),
                newValue,
                getCurrentActivity(),
                getEditCoordinate()));
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
    public ObservableStampFilter getVertexStampFilter() {
        return this.vertexStampFilterObservable;
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this.getValue();
    }

    @Override
    public ObservableStampFilter getViewStampFilter() {
        return this.edgeStampFilterObservable;
    }

    @Override
    public ObjectProperty<StampFilterImmutable> vertexStampFilterProperty() {
        return vertexStampFilterObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<EditCoordinateImmutable> editCoordinateProperty() {
        return editCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<StampFilterImmutable> edgeStampFilterProperty() {
        return edgeStampFilterObservable.baseCoordinateProperty();
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

    @Override
    public ObjectProperty<Activity> activityProperty() {
        return this.activityProperty;
    }

    @Override
    public ObservableEditCoordinateBase getEditCoordinate() {
        return this.editCoordinateObservable;
    }

    @Override
    public Activity getCurrentActivity() {
        return this.activityProperty.get();
    }

    @Override
    public PremiseSet getPremiseTypes() {
        return getValue().getPremiseTypes();
    }

    @Override
    public ManifoldCoordinate makeCoordinateAnalog(long classifyTimeInEpochMillis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExceptOverrides(ManifoldCoordinateImmutable updatedCoordinate) {

    }
}