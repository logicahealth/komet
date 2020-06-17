package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableDigraphCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

public abstract class ObservableDigraphCoordinateBase
        extends ObservableCoordinateImpl<DigraphCoordinateImmutable>
        implements DigraphCoordinateProxy, ObservableDigraphCoordinate {

    private final SimpleEqualityBasedSetProperty<ConceptSpecification> digraphIdentifierConceptsProperty;

    private final ObjectProperty<PremiseType> premiseTypeProperty;

    /**
     *
     * The vertexSort property.
     */
    private final SimpleObjectProperty<VertexSort> vertexSortProperty;

    private final ObservableStampFilterBase vertexStampFilterObservable;

    private final ObservableStampFilterBase edgeStampFilterObservable;

    private final ObservableStampFilterBase languageStampFilterObservable;

    private final ObservableLanguageCoordinateBase languageCoordinateObservable;

    private final ObservableLogicCoordinateBase logicCoordinateObservable;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final SetChangeListener<ConceptSpecification> digraphIdentifierConceptSetListener = this::digraphSetChanged;
    private final ChangeListener<PremiseType> premiseTypeListener = this::premiseTypeChanged;
    private final ChangeListener<StampFilterImmutable> edgeStampFilterListener = this::edgeFilterChanged;
    private final ChangeListener<StampFilterImmutable> vertexStampFilterListener = this::vertexFilterChanged;
    private final ChangeListener<StampFilterImmutable> languageStampFilterListener = this::languageFilterChanged;
    private final ChangeListener<LanguageCoordinateImmutable> languageCoordinateListener = this::languageCoordinateChanged;
    private final ChangeListener<LogicCoordinateImmutable> logicCoordinateListener = this::logicCoordinateChanged;

    public ObservableDigraphCoordinateBase(DigraphCoordinate digraphCoordinate, String coordinateName) {
        super(digraphCoordinate.toDigraphImmutable(), coordinateName);
        this.languageCoordinateObservable = makeLanguageCoordinate(digraphCoordinate);

        this.logicCoordinateObservable = makeLogicCoordinate(digraphCoordinate);

        this.premiseTypeProperty = makePremiseTypeProperty(digraphCoordinate);

        this.vertexSortProperty = makeVertexSortProperty(digraphCoordinate);

        this.edgeStampFilterObservable = makeEdgeStampFilterProperty(digraphCoordinate);

        this.vertexStampFilterObservable = makeVertexStampFilterProperty(digraphCoordinate);

        this.languageStampFilterObservable = makeLanguageStampFilterProperty(digraphCoordinate);

        this.digraphIdentifierConceptsProperty = makeDigraphIdentifierConceptsProperty(digraphCoordinate);
        addListeners();
    }

    protected abstract SimpleEqualityBasedSetProperty<ConceptSpecification> makeDigraphIdentifierConceptsProperty(DigraphCoordinate digraphCoordinate);

    protected abstract ObservableStampFilterBase makeLanguageStampFilterProperty(DigraphCoordinate digraphCoordinate);

    protected abstract ObservableStampFilterBase makeVertexStampFilterProperty(DigraphCoordinate digraphCoordinate);

    protected abstract ObservableStampFilterBase makeEdgeStampFilterProperty(DigraphCoordinate digraphCoordinate);

    protected abstract SimpleObjectProperty<VertexSort> makeVertexSortProperty(DigraphCoordinate digraphCoordinate);

    protected abstract ObjectProperty<PremiseType> makePremiseTypeProperty(DigraphCoordinate digraphCoordinate);

    protected abstract ObservableLogicCoordinateBase makeLogicCoordinate(DigraphCoordinate digraphCoordinate);

    protected abstract ObservableLanguageCoordinateBase makeLanguageCoordinate(DigraphCoordinate digraphCoordinate);

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends DigraphCoordinateImmutable> observable, DigraphCoordinateImmutable oldValue, DigraphCoordinateImmutable newValue) {
        this.languageCoordinateObservable.setValue(newValue.getLanguageCoordinate());
        this.logicCoordinateObservable.setValue(newValue.getLogicCoordinate());
        this.premiseTypeProperty.setValue(newValue.getPremiseType());
        this.edgeStampFilterObservable.setValue(newValue.getEdgeStampFilter());
        this.vertexStampFilterObservable.setValue(newValue.getVertexStampFilter());
        this.languageStampFilterObservable.setValue(newValue.getLanguageStampFilter());
        this.digraphIdentifierConceptsProperty.setAll(newValue.getDigraphIdentifierConceptNids()
                .collect(nid -> Get.conceptSpecification(nid)).toSet());
    }

    @Override
    protected void addListeners() {
        this.languageCoordinateObservable.baseCoordinateProperty().addListener(this.languageCoordinateListener);
        this.logicCoordinateObservable.baseCoordinateProperty().addListener(this.logicCoordinateListener);
        this.premiseTypeProperty.addListener(this.premiseTypeListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().addListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.addListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().addListener(this.languageStampFilterListener);
        this.digraphIdentifierConceptsProperty.addListener(this.digraphIdentifierConceptSetListener);
    }

    @Override
    protected void removeListeners() {
        this.languageCoordinateObservable.baseCoordinateProperty().removeListener(this.languageCoordinateListener);
        this.logicCoordinateObservable.baseCoordinateProperty().removeListener(this.logicCoordinateListener);
        this.premiseTypeProperty.removeListener(this.premiseTypeListener);
        this.edgeStampFilterObservable.baseCoordinateProperty().removeListener(this.edgeStampFilterListener);
        this.vertexStampFilterObservable.removeListener(this.vertexStampFilterListener);
        this.languageStampFilterObservable.baseCoordinateProperty().removeListener(this.languageStampFilterListener);
        this.digraphIdentifierConceptsProperty.removeListener(this.digraphIdentifierConceptSetListener);
    }


    private void digraphSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getPremiseType(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    private void languageFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                       StampFilterImmutable oldValue,
                                       StampFilterImmutable newValue) {
        this.languageStampFilterObservable.setValue(newValue);
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getEdgeStampFilter().toStampFilterImmutable(),
                newValue,
                getPremiseType(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getDigraphIdentifierConceptNids()));
    }
    private void vertexFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                     StampFilterImmutable oldValue,
                                     StampFilterImmutable newValue) {
        this.vertexStampFilterObservable.setValue(newValue);
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                newValue,
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getPremiseType(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getDigraphIdentifierConceptNids()));
    }
    private void edgeFilterChanged(ObservableValue<? extends StampFilterImmutable> observable,
                                   StampFilterImmutable oldValue,
                                   StampFilterImmutable newValue) {
        this.edgeStampFilterObservable.setValue(newValue);
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                newValue,
                getLanguageStampFilter().toStampFilterImmutable(),
                getPremiseType(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getDigraphIdentifierConceptNids()));
    }


    private void premiseTypeChanged(ObservableValue<? extends PremiseType> observable,
                                    PremiseType oldValue,
                                    PremiseType newValue) {
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                newValue,
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getDigraphIdentifierConceptNids()));
    }

    private void logicCoordinateChanged(ObservableValue<? extends LogicCoordinateImmutable> observable,
                                        LogicCoordinateImmutable oldValue,
                                        LogicCoordinateImmutable newValue) {
        this.logicCoordinateObservable.setValue(newValue);
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getPremiseType(),
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                newValue,
                getDigraphIdentifierConceptNids()));
    }



    private void languageCoordinateChanged(ObservableValue<? extends LanguageCoordinateImmutable> observable,
                                           LanguageCoordinateImmutable oldValue,
                                           LanguageCoordinateImmutable newValue) {
        this.languageCoordinateObservable.setValue(newValue);
        this.setValue(DigraphCoordinateImmutable.make(getVertexSort(),
                getVertexStampFilter().toStampFilterImmutable(),
                getEdgeStampFilter().toStampFilterImmutable(),
                getLanguageStampFilter().toStampFilterImmutable(),
                getPremiseType(),
                newValue,
                getLogicCoordinate().toLogicCoordinateImmutable(),
                getDigraphIdentifierConceptNids()));
    }

    @Override
    public DigraphCoordinateImmutable getDigraph() {
        return getValue();
    }

    @Override
    public SetProperty<ConceptSpecification> digraphIdentifierConceptsProperty() {
        return digraphIdentifierConceptsProperty;
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
    public ObjectProperty<PremiseType> premiseTypeProperty() {
        return premiseTypeProperty;
    }

    @Override
    public ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty() {
        return languageCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty() {
        return logicCoordinateObservable.baseCoordinateProperty();
    }

    @Override
    public ObservableStampFilter getVertexStampFilter() {
        return this.vertexStampFilterObservable;
    }

    @Override
    public ObservableStampFilter getEdgeStampFilter() {
        return this.edgeStampFilterObservable;
    }

    @Override
    public ObservableStampFilter getLanguageStampFilter() {
        return this.languageStampFilterObservable;
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return this.languageCoordinateObservable;
    }

    @Override
    public ObservableLogicCoordinate getLogicCoordinate() {
        return this.logicCoordinateObservable;
    }

    @Override
    public ObjectProperty<VertexSort> vertexSortProperty() {
        return this.vertexSortProperty;
    }

    @Override
    public VertexSort getVertexSort() {
        return this.vertexSortProperty.get();
    }
}