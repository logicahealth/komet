package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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

public class ObservableDigraphCoordinateImpl extends ObservableDigraphCoordinateBase {

    public ObservableDigraphCoordinateImpl(DigraphCoordinateImmutable immutableCoordinate, String coordinateName) {
        super(immutableCoordinate, coordinateName);
    }

    public ObservableDigraphCoordinateImpl(DigraphCoordinateImmutable immutableCoordinate) {
        super(immutableCoordinate, "Digraph");
    }

    @Override
    protected SimpleEqualityBasedSetProperty<ConceptSpecification> makeDigraphIdentifierConceptsProperty(DigraphCoordinate digraphCoordinate) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.DIGRAPH_SPECIFICATION_SET.toExternalString(),
                FXCollections.observableSet(digraphCoordinate.getDigraphIdentifierConceptNids()
                        .collect(nid -> Get.conceptSpecification(nid)).toSet()));
    }

    @Override
    protected ObservableStampFilterBase makeLanguageStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        return ObservableStampFilterImpl.make(digraphCoordinate.getLanguageStampFilter(), ObservableFields.LANGUAGE_FILTER_FOR_DIGRAPH.toExternalString());
    }

    @Override
    protected ObservableStampFilterBase makeVertexStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        return  ObservableStampFilterImpl.make(digraphCoordinate.getVertexStampFilter(), ObservableFields.VERTEX_FILTER_FOR_DIGRAPH.toExternalString());
    }

    @Override
    protected ObservableStampFilterBase makeEdgeStampFilterProperty(DigraphCoordinate digraphCoordinate) {
        return ObservableStampFilterImpl.make(digraphCoordinate.getEdgeStampFilter(), ObservableFields.EDGE_FILTER_FOR_DIGRAPH.toExternalString());
    }

    @Override
    protected SimpleObjectProperty<VertexSort> makeVertexSortProperty(DigraphCoordinate digraphCoordinate) {
        return new SimpleObjectProperty<>(this,
                ObservableFields.VERTEX_SORT_PROPERTY.toExternalString(),
                digraphCoordinate.getVertexSort());
    }

    @Override
    protected ObjectProperty<PremiseType> makePremiseTypeProperty(DigraphCoordinate digraphCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PREMISE_TYPE_FOR_TAXONOMY_COORDINATE.toExternalString(),
                digraphCoordinate.getPremiseType());
    }

    @Override
    protected ObservableLogicCoordinateBase makeLogicCoordinate(DigraphCoordinate digraphCoordinate) {
        return new ObservableLogicCoordinateImpl(digraphCoordinate.getLogicCoordinate());
    }

    @Override
    protected ObservableLanguageCoordinateBase makeLanguageCoordinate(DigraphCoordinate digraphCoordinate) {
        return new ObservableLanguageCoordinateImpl(digraphCoordinate.getLanguageCoordinate());
    }
}
