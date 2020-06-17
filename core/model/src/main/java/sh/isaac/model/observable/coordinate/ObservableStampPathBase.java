package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPath;

public abstract class ObservableStampPathBase
        extends ObservableCoordinateImpl<StampPathImmutable>
        implements ObservableStampPath {

    /** The path concept property. */
    private final ObjectProperty<ConceptSpecification> pathConceptProperty;

    private final SetProperty<StampPositionImmutable> pathOriginsProperty;

    private final ListProperty<StampPositionImmutable> pathOriginsAsListProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptSpecification> pathConceptChangedListener = this::pathConceptChanged;
    private final SetChangeListener<StampPositionImmutable> pathOriginsSetChanged = this::pathOriginsSetChanged;
    private final ListChangeListener<StampPositionImmutable> pathOriginsListChangedListener = this::pathOriginsListChanged;

    //~--- constructors --------------------------------------------------------

    protected ObservableStampPathBase(StampPath stampPath, String coordinateName) {
        super(stampPath.toStampPathImmutable(), coordinateName);
        this.pathConceptProperty = makePathConceptProperty(stampPath);

        this.pathOriginsProperty = makePathOriginsProperty(stampPath);

        this.pathOriginsAsListProperty = makePathOriginsAsListProperty(stampPath);

        addListeners();
    }

    protected abstract ListProperty<StampPositionImmutable> makePathOriginsAsListProperty(StampPath stampPath);

    protected abstract SetProperty<StampPositionImmutable> makePathOriginsProperty(StampPath stampPath);

    protected abstract ObjectProperty<ConceptSpecification> makePathConceptProperty(StampPath stampPath);

    @Override
    protected final void addListeners() {
        this.pathConceptProperty.addListener(this.pathConceptChangedListener);
        this.pathOriginsProperty.addListener(this.pathOriginsSetChanged);
        this.pathOriginsAsListProperty.addListener(this.pathOriginsListChangedListener);
    }

    @Override
    protected final void removeListeners() {
        this.pathConceptProperty.removeListener(this.pathConceptChangedListener);
        this.pathOriginsProperty.removeListener(this.pathOriginsSetChanged);
        this.pathOriginsAsListProperty.removeListener(this.pathOriginsListChangedListener);
    }

    @Override
    protected final void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPathImmutable> observable, StampPathImmutable oldValue, StampPathImmutable newValue) {
        this.pathConceptProperty.setValue(Get.conceptSpecification(newValue.getPathConceptNid()));
        this.pathOriginsProperty.setValue(FXCollections.observableSet(newValue.getPathOrigins().toSet()));
        this.pathOriginsAsListProperty.setValue(FXCollections.observableList(newValue.getPathOrigins().toList()));
    }

    private void pathOriginsSetChanged(SetChangeListener.Change<? extends StampPositionImmutable> c) {
        this.setValue(StampPathImmutable.make(getPathConcept().getNid(),
                Sets.immutable.withAll(c.getSet())));
    }

    private void pathOriginsListChanged(ListChangeListener.Change<? extends StampPositionImmutable> c) {
        this.setValue(StampPathImmutable.make(getPathConcept().getNid(),
                Sets.immutable.withAll(c.getList())));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(StampPathImmutable.make(newPathConcept.getNid(),
                getPathOrigins()));
    }

    @Override
    public final ObjectProperty<ConceptSpecification> pathConceptProperty() {
        return this.pathConceptProperty;
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ObservableStampPathImpl{" + this.getValue().toString() + '}';
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this.getValue().equals(obj);
    }

    @Override
    public final SetProperty<StampPositionImmutable> pathOriginsProperty() {
        return this.pathOriginsProperty;
    }

    @Override
    public final ListProperty<StampPositionImmutable> pathOriginsAsListPropertyProperty() {
        return this.pathOriginsAsListProperty;
    }

    @Override
    public final StampPath getStampPath() {
        return this.getValue();
    }

    @Override
    public final int getPathConceptNid() {
        return this.getValue().getPathConceptNid();
    }

    @Override
    public final ImmutableSet<StampPositionImmutable> getPathOrigins() {
        return this.getValue().getPathOrigins();
    }

    @Override
    public final StampPathImmutable toStampPathImmutable() {
        return getValue();
    }
}

