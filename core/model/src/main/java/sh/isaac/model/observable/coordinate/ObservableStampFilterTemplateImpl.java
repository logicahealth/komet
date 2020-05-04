package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilterTemplate;
import sh.isaac.api.coordinate.StampFilterTemplateImmutable;
import sh.isaac.api.coordinate.StatusSet;
import sh.isaac.api.observable.coordinate.ObservableStampFilterTemplate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

public class ObservableStampFilterTemplateImpl extends ObservableCoordinateImpl<StampFilterTemplateImmutable> implements ObservableStampFilterTemplate {

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
     *
     * @return the excluded modules property
     */
    private final SimpleEqualityBasedSetProperty<ConceptSpecification> excludedModuleSpecificationsProperty;

    /**
     * Module preference list property.
     *
     * @return the object property
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> modulePriorityOrderProperty;



    private ObservableStampFilterTemplateImpl(StampFilterTemplateImmutable stampFilterTemplateImmutable) {
        super(stampFilterTemplateImmutable);

        this.allowedStatusProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilterTemplateImmutable.getAllowedStates().toEnumSet()));

        this.modulePriorityOrderProperty = new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(stampFilterTemplateImmutable.getModulePriorityOrder().collect(nid -> Get.conceptSpecification(nid)).castToList()));

        this.excludedModuleSpecificationsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_EXCLUSION_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilterTemplateImmutable.getExcludedModuleNids().collect(nid -> Get.conceptSpecification(nid)).toSet()));

        this.moduleSpecificationsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilterTemplateImmutable.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).toSet()));


        addListeners();

    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampFilterTemplateImmutable> observable, StampFilterTemplateImmutable oldValue, StampFilterTemplateImmutable newValue) {
        this.allowedStatusProperty.setAll(newValue.getAllowedStates().toEnumSet());
        this.modulePriorityOrderProperty.setAll(newValue.getModulePriorityOrder().collect(nid -> Get.conceptSpecification(nid)).castToList());
        this.excludedModuleSpecificationsProperty.setAll(newValue.getExcludedModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet());
        this.moduleSpecificationsProperty.setAll(newValue.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet());
    }

    @Override
    protected void addListeners() {
        this.allowedStatusProperty.addListener(this::statusSetChanged);
        this.modulePriorityOrderProperty.addListener(this::modulePreferenceOrderChanged);
        this.excludedModuleSpecificationsProperty.addListener(this::excludedModuleSetChanged);
        this.moduleSpecificationsProperty.addListener(this::moduleSetChanged);
    }

    @Override
    protected void removeListeners() {
        this.allowedStatusProperty.removeListener(this::statusSetChanged);
        this.modulePriorityOrderProperty.removeListener(this::modulePreferenceOrderChanged);
        this.excludedModuleSpecificationsProperty.removeListener(this::excludedModuleSetChanged);
        this.moduleSpecificationsProperty.removeListener(this::moduleSetChanged);
    }

    public static ObservableStampFilterTemplateImpl make(StampFilterTemplate stampFilterTemplate) {
        return new ObservableStampFilterTemplateImpl(stampFilterTemplate.toStampFilterTemplateImmutable());
    }

    private void moduleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterTemplateImmutable.make(getAllowedStates(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray()),
                getExcludedModuleNids(),
                getModulePriorityOrder()));
    }

    private void excludedModuleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterTemplateImmutable.make(getAllowedStates(),
                getModuleNids(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray()),
                getModulePriorityOrder()));
    }

    private void modulePreferenceOrderChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterTemplateImmutable.make(getAllowedStates(),
                getModuleNids(),
                getExcludedModuleNids(),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    private void statusSetChanged(SetChangeListener.Change<? extends Status> c) {
        this.setValue(StampFilterTemplateImmutable.make(StatusSet.of(c.getSet()),
                getModuleNids(),
                getExcludedModuleNids(),
                getModulePriorityOrder()));
    }

    @Override
    public SetProperty<ConceptSpecification> moduleSpecificationsProperty() {
        return this.moduleSpecificationsProperty;
    }

    @Override
    public ImmutableIntSet getExcludedModuleNids() {
        return getValue().getExcludedModuleNids();
    }

    @Override
    public SetProperty<ConceptSpecification> excludedModuleSpecificationsProperty() {
        return this.excludedModuleSpecificationsProperty;
    }

    @Override
    public ListProperty<ConceptSpecification> modulePriorityOrderProperty() {
        return this.modulePriorityOrderProperty;
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
    public StampFilterTemplateImmutable getStampFilterTemplate() {
        return this.getValue();
    }

    @Override
    public StampFilterTemplateImmutable toStampFilterTemplateImmutable() {
        return this.getValue();
    }
}
