package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableNavigationCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

public abstract class ObservableNavigationCoordinateBase
        extends ObservableCoordinateImpl<NavigationCoordinateImmutable>
        implements NavigationCoordinateProxy, ObservableNavigationCoordinate {

    private final SimpleEqualityBasedSetProperty<ConceptSpecification> navigatorIdentifierConceptsProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final SetChangeListener<ConceptSpecification> navigatorIdentifierConceptSetListener = this::navigationSetChanged;

    public ObservableNavigationCoordinateBase(NavigationCoordinate navigationCoordinate, String coordinateName) {
        super(navigationCoordinate.toNavigationCoordinateImmutable(), coordinateName);
        this.navigatorIdentifierConceptsProperty = makeNavigatorIdentifierConceptsProperty(navigationCoordinate);
        addListeners();
    }

    protected abstract SimpleEqualityBasedSetProperty<ConceptSpecification> makeNavigatorIdentifierConceptsProperty(NavigationCoordinate navigationCoordinate);


    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends NavigationCoordinateImmutable> observable, NavigationCoordinateImmutable oldValue, NavigationCoordinateImmutable newValue) {
        this.navigatorIdentifierConceptsProperty.setAll(newValue.getNavigationConceptNids()
                .collect(nid -> Get.conceptSpecification(nid)).toSet());
    }

    @Override
    protected void addListeners() {
        this.navigatorIdentifierConceptsProperty.addListener(this.navigatorIdentifierConceptSetListener);
    }

    @Override
    protected void removeListeners() {
        this.navigatorIdentifierConceptsProperty.removeListener(this.navigatorIdentifierConceptSetListener);
    }


    private void navigationSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(NavigationCoordinateImmutable.make(
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    @Override
    public NavigationCoordinateImmutable getDigraph() {
        return getValue();
    }

    @Override
    public SetProperty<ConceptSpecification> navigatorIdentifierConceptsProperty() {
        return navigatorIdentifierConceptsProperty;
    }
}