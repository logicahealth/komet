package sh.isaac.model.observable.coordinate;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

public class ObservableStampFilterImpl extends ObservableCoordinateImpl<StampFilterImmutable> implements ObservableStampFilter {

    private final LongProperty timeProperty;

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    private final ObjectProperty<ConceptSpecification> pathConceptProperty;

    /**
     *
     * @return a set of allowed status values to filter computation results.
     */
    private final SimpleEqualityBasedSetProperty<Status> allowedStatusProperty;


    /**
     *
     * @return the specified modules property
     */
    private final SimpleEqualityBasedSetProperty<ConceptSpecification> moduleSpecificationsProperty;

    /**
     * Module preference list property.
     *
     * @return the object property
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> modulePreferenceListForVersionsProperty;



    private ObservableStampFilterImpl(StampFilterImmutable stampFilterImmutable) {
        super(stampFilterImmutable);

        this.allowedStatusProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilterImmutable.getAllowedStates().toEnumSet()));

        this.modulePreferenceListForVersionsProperty = new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(stampFilterImmutable.getModulePreferenceOrder().collect(nid -> Get.conceptSpecification(nid)).castToList()));

        this.pathConceptProperty = new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampFilterImmutable.getPathConceptForFilter());

        this.moduleSpecificationsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilterImmutable.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet()));

        this.timeProperty = new SimpleLongProperty(this,
                ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
                stampFilterImmutable.getStampPosition().getTime());

        addListeners();

    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampFilterImmutable> observable, StampFilterImmutable oldValue, StampFilterImmutable newValue) {
        this.allowedStatusProperty.setAll(newValue.getAllowedStates().toEnumSet());
        this.modulePreferenceListForVersionsProperty.setAll(newValue.getModulePreferenceOrder().collect(nid -> Get.conceptSpecification(nid)).castToList());
        this.pathConceptProperty.setValue(newValue.getPathConceptForFilter());
        this.moduleSpecificationsProperty.setAll(newValue.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet());
        this.timeProperty.set(newValue.getStampPosition().getTime());
    }

    @Override
    protected void addListeners() {
        this.allowedStatusProperty.addListener(this::statusSetChanged);
        this.modulePreferenceListForVersionsProperty.addListener(this::modulePreferenceOrderChanged);
        this.pathConceptProperty.addListener(this::pathConceptChanged);
        this.moduleSpecificationsProperty.addListener(this::moduleSetChanged);
        this.timeProperty.addListener(this::timeChanged);
    }

    @Override
    protected void removeListeners() {
        this.allowedStatusProperty.removeListener(this::statusSetChanged);
        this.modulePreferenceListForVersionsProperty.removeListener(this::modulePreferenceOrderChanged);
        this.pathConceptProperty.removeListener(this::pathConceptChanged);
        this.moduleSpecificationsProperty.removeListener(this::moduleSetChanged);
        this.timeProperty.removeListener(this::timeChanged);
    }

    public static ObservableStampFilterImpl make(StampFilter stampFilter) {
        return new ObservableStampFilterImpl(stampFilter.toStampFilterImmutable());
    }

    private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(newTime.longValue(), getPathNidForFilter()),
                getModuleNids(),
                getModulePreferenceOrder()));
    }

    private void moduleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(timeProperty.longValue(), getPathNidForFilter()),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray()),
                getModulePreferenceOrder()));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(timeProperty.longValue(), newPathConcept.getNid()),
                getModuleNids(),
                getModulePreferenceOrder()));
    }

    private void modulePreferenceOrderChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(timeProperty.longValue(), pathConceptProperty.get().getNid()),
                getModuleNids(),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    private void statusSetChanged(SetChangeListener.Change<? extends Status> c) {
        this.setValue(StampFilterImmutable.make(StatusSet.of(c.getSet()),
                StampPositionImmutable.make(timeProperty.longValue(), pathConceptProperty.get().getNid()),
                getModuleNids(),
                getModulePreferenceOrder()));
    }

    @Override
    public LongProperty timeProperty() {
        return this.timeProperty;
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathConceptProperty() {
        return this.pathConceptProperty;
    }

    @Override
    public SetProperty<ConceptSpecification> moduleSpecificationsProperty() {
        return this.moduleSpecificationsProperty;
    }

    @Override
    public ListProperty<ConceptSpecification> modulePreferenceListForVersionsProperty() {
        return this.modulePreferenceListForVersionsProperty;
    }

    /**
     * Allowed states property.
     *
     * @return the set property
     */
    @Override
    public SetProperty<Status> allowedStatusProperty() {
         return this.allowedStatusProperty;
    }

    @Override
    public StampFilter getStampFilter() {
        return getValue();
    }
}
