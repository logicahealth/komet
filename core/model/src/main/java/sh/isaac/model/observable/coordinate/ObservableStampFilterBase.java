package sh.isaac.model.observable.coordinate;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public abstract class ObservableStampFilterBase extends ObservableCoordinateImpl<StampFilterImmutable> implements ObservableStampFilter {

    private final LongProperty timeProperty;

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    private final ObjectProperty<ConceptSpecification> pathConceptProperty;

    private final SetProperty<ConceptSpecification> moduleSpecificationsProperty;

    private final SetProperty<ConceptSpecification> excludedModuleSpecificationsProperty;

    private final ObjectProperty<StatusSet> allowedStatusProperty;

    private final ListProperty<ConceptSpecification> modulePriorityOrderProperty;

    /**
     * Note that if you don't declare a listener as final in this way, and just use method references, or
     * a direct lambda expression, you will not be able to remove the listener, since each method reference will create
     * a new object, and they won't compare equal using object identity.
     * https://stackoverflow.com/questions/42146360/how-do-i-remove-lambda-expressions-method-handles-that-are-used-as-listeners
     */
    private final ChangeListener<ConceptSpecification> pathConceptListener = this::pathConceptChanged;
    private final ChangeListener<Number> timeListener = this::timeChanged;
    private final ChangeListener<StatusSet> statusSetListener = this::statusSetChanged;
    private final ListChangeListener<ConceptSpecification> modulePreferenceOrderListener = this::modulePreferenceOrderChanged;
    private final SetChangeListener<ConceptSpecification> excludedModuleSetListener = this::excludedModuleSetChanged;
    private final SetChangeListener<ConceptSpecification> moduleSetListener = this::moduleSetChanged;

    protected ObservableStampFilterBase(StampFilter stampFilter, String coordinateName) {
        super(stampFilter.toStampFilterImmutable(), coordinateName);
        this.pathConceptProperty = makePathConceptProperty(stampFilter);
        this.timeProperty = makeTimeProperty(stampFilter);
        this.moduleSpecificationsProperty = makeModuleSpecificationsProperty(stampFilter);
        this.excludedModuleSpecificationsProperty = makeExcludedModuleSpecificationsProperty(stampFilter);
        this.allowedStatusProperty = makeAllowedStatusProperty(stampFilter);
        this.modulePriorityOrderProperty = makeModulePriorityOrderProperty(stampFilter);
        addListeners();
    }

    protected abstract ListProperty<ConceptSpecification> makeModulePriorityOrderProperty(StampFilter stampFilter);

    protected abstract ObjectProperty<StatusSet> makeAllowedStatusProperty(StampFilter stampFilter);

    protected abstract SetProperty<ConceptSpecification> makeExcludedModuleSpecificationsProperty(StampFilter stampFilter);

    protected abstract SetProperty<ConceptSpecification> makeModuleSpecificationsProperty(StampFilter stampFilter);

    protected abstract LongProperty makeTimeProperty(StampFilter stampFilter);

    protected abstract ObjectProperty<ConceptSpecification> makePathConceptProperty(StampFilter stampFilter);

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this.pathConceptListener);
        this.timeProperty.addListener(this.timeListener);
        this.allowedStatusProperty.addListener(this.statusSetListener);
        this.modulePriorityOrderProperty.addListener(this.modulePreferenceOrderListener);
        this.excludedModuleSpecificationsProperty.addListener(this.excludedModuleSetListener);
        this.moduleSpecificationsProperty.addListener(this.moduleSetListener);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this.pathConceptListener);
        this.timeProperty.removeListener(this.timeListener);
        this.allowedStatusProperty.removeListener(this.statusSetListener);
        this.modulePriorityOrderProperty.removeListener(this.modulePreferenceOrderListener);
        this.excludedModuleSpecificationsProperty.removeListener(this.excludedModuleSetListener);
        this.moduleSpecificationsProperty.removeListener(this.moduleSetListener);
    }

    private void timeChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newTime) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(newTime.longValue(), getPathNidForFilter()),
                getModuleNids(),
                getModulePriorityOrder()));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                StampPositionImmutable.make(timeProperty.longValue(), newPathConcept.getNid()),
                getModuleNids(),
                getModulePriorityOrder()));
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

    /**
     *
     * @return the specified modules property
     */
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
    public ObjectProperty<StatusSet> allowedStatusProperty() {
        return this.allowedStatusProperty;
    }

    @Override
    public StampFilter getStampFilter() {
        return getValue();
    }

    @Override
    public StampFilterTemplate getStampFilterTemplate() {
        return this.getValue().toStampFilterTemplateImmutable();
    }

    @Override
    public StampFilterTemplateImmutable toStampFilterTemplateImmutable() {
        return this.getValue().toStampFilterTemplateImmutable();
    }

    private void moduleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                getStampPosition(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray()),
                getExcludedModuleNids(),
                getModulePriorityOrder()));
    }

    private void excludedModuleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                getStampPosition(),
                getModuleNids(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray()),
                getModulePriorityOrder()));
    }

    private void modulePreferenceOrderChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(StampFilterImmutable.make(getAllowedStates(),
                getStampPosition(),
                getModuleNids(),
                getExcludedModuleNids(),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    private void statusSetChanged(ObservableValue<? extends StatusSet> observableStatusSet,
                                  StatusSet oldStatusSet,
                                  StatusSet newStatusSet) {
        this.setValue(StampFilterImmutable.make(newStatusSet,
                getStampPosition(),
                getModuleNids(),
                getExcludedModuleNids(),
                getModulePriorityOrder()));
    }

    @Override
    public ImmutableIntSet getExcludedModuleNids() {
        return getValue().getExcludedModuleNids();
    }

    @Override
    public ObservableStampFilter makeModuleAnalog(Collection<ConceptSpecification> modules, boolean add) {
        return ObservableStampFilterImpl.make(getValue().makeModuleAnalog(modules, add));
    }

    @Override
    public ObservableStampFilter makePathAnalog(ConceptSpecification pathForPosition) {
        return ObservableStampFilterImpl.make(getValue().makePathAnalog(pathForPosition));
    }

}