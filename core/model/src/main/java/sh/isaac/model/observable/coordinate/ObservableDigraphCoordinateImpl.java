package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
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
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

public class ObservableDigraphCoordinateImpl
        extends ObservableCoordinateImpl<DigraphCoordinateImmutable>
        implements DigraphCoordinateProxy, ObservableDigraphCoordinate {

    private final SimpleEqualityBasedSetProperty<ConceptSpecification> digraphIdentifierConceptsProperty;

    private final ObjectProperty<StampFilterImmutable> vertexStampFilterProperty;
    private final ObservableStampFilterImpl vertexStampFilterObservable;

    private final ObjectProperty<StampFilterImmutable> edgeStampFilterProperty;
    private final ObservableStampFilterImpl edgeStampFilterObservable;

    private final ObjectProperty<StampFilterImmutable> languageStampFilterProperty;
    private final ObservableStampFilterImpl languageStampFilterObservable;

    private final ObjectProperty<PremiseType> premiseTypeProperty;

    private final ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty;
    private final ObservableLanguageCoordinateImpl languageCoordinateObservable;

    private final ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty;
    private final ObservableLogicCoordinateImpl logicCoordinateObservable;

    public ObservableDigraphCoordinateImpl(DigraphCoordinateImmutable immutableCoordinate) {
        super(immutableCoordinate);
        this.languageCoordinateObservable = new ObservableLanguageCoordinateImpl(immutableCoordinate.getLanguageCoordinate());
        this.languageCoordinateProperty = this.languageCoordinateObservable.baseCoordinateProperty();


        this.logicCoordinateObservable = new ObservableLogicCoordinateImpl(immutableCoordinate.getLogicCoordinate());
        this.logicCoordinateProperty = this.logicCoordinateObservable.baseCoordinateProperty();

        this.premiseTypeProperty = new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(),
                immutableCoordinate.getPremiseType());

        this.edgeStampFilterObservable = ObservableStampFilterImpl.make(immutableCoordinate.getEdgeStampFilter());
        this.edgeStampFilterProperty = this.edgeStampFilterObservable.baseCoordinateProperty();

        this.vertexStampFilterObservable = ObservableStampFilterImpl.make(immutableCoordinate.getVertexStampFilter());
        this.vertexStampFilterProperty = this.vertexStampFilterObservable.baseCoordinateProperty();

        this.languageStampFilterObservable = ObservableStampFilterImpl.make(immutableCoordinate.getLanguageStampFilter());
        this.languageStampFilterProperty = this.languageStampFilterObservable.baseCoordinateProperty();

        this.digraphIdentifierConceptsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.DIGRAPH_SPECIFICATION_SET.toExternalString(),
                FXCollections.observableSet(immutableCoordinate.getDigraphIdentifierConceptNids()
                        .collect(nid -> Get.conceptSpecification(nid)).castToSet()));

        addListeners();
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends DigraphCoordinateImmutable> observable, DigraphCoordinateImmutable oldValue, DigraphCoordinateImmutable newValue) {
        this.languageCoordinateObservable.setValue(newValue.getLanguageCoordinate());
        this.logicCoordinateObservable.setValue(newValue.getLogicCoordinate());
        this.premiseTypeProperty.setValue(newValue.getPremiseType());
        this.edgeStampFilterObservable.setValue(newValue.getEdgeStampFilter());
        this.vertexStampFilterObservable.setValue(newValue.getVertexStampFilter());
        this.languageStampFilterObservable.setValue(newValue.getLanguageStampFilter());
        this.digraphIdentifierConceptsProperty.setAll(newValue.getDigraphIdentifierConceptNids()
                .collect(nid -> Get.conceptSpecification(nid)).castToSet());
    }

    @Override
    protected void addListeners() {
        this.languageCoordinateProperty.addListener(this::languageCoordinateChanged);
        this.logicCoordinateProperty.addListener(this::logicCoordinateChanged);
        this.premiseTypeProperty.addListener(this::premiseTypeChanged);
        this.edgeStampFilterProperty.addListener(this::edgeFilterChanged);
        this.vertexStampFilterProperty.addListener(this::vertexFilterChanged);
        this.languageStampFilterProperty.addListener(this::languageFilterChanged);
        this.digraphIdentifierConceptsProperty.addListener(this::digraphSetChanged);
    }

    @Override
    protected void removeListeners() {
        this.languageCoordinateProperty.removeListener(this::languageCoordinateChanged);
        this.logicCoordinateProperty.removeListener(this::logicCoordinateChanged);
        this.premiseTypeProperty.removeListener(this::premiseTypeChanged);
        this.edgeStampFilterProperty.removeListener(this::edgeFilterChanged);
        this.vertexStampFilterProperty.removeListener(this::vertexFilterChanged);
        this.languageStampFilterProperty.removeListener(this::languageFilterChanged);
        this.digraphIdentifierConceptsProperty.removeListener(this::digraphSetChanged);
    }


    private void digraphSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        this.setValue(DigraphCoordinateImmutable.make(newValue,
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
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        this.setValue(DigraphCoordinateImmutable.make(getVertexStampFilter().toStampFilterImmutable(),
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
        return vertexStampFilterProperty;
    }

    @Override
    public ObjectProperty<StampFilterImmutable> edgeStampFilterProperty() {
        return edgeStampFilterProperty;
    }

    @Override
    public ObjectProperty<StampFilterImmutable> languageStampFilterProperty() {
        return languageStampFilterProperty;
    }

    @Override
    public ObjectProperty<PremiseType> premiseTypeProperty() {
        return premiseTypeProperty;
    }

    @Override
    public ObjectProperty<LanguageCoordinateImmutable> languageCoordinateProperty() {
        return languageCoordinateProperty;
    }

    @Override
    public ObjectProperty<LogicCoordinateImmutable> logicCoordinateProperty() {
        return logicCoordinateProperty;
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




}
