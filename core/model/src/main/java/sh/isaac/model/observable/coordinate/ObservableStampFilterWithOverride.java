package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.LongPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

import java.util.Set;

public class ObservableStampFilterWithOverride extends ObservableStampFilterBase {

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter) {
        this(stampFilter, stampFilter.getName());
    }

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter, String coordinateName) {
        super(stampFilter, coordinateName);
        if (stampFilter instanceof ObservableStampFilterWithOverride) {
            throw new IllegalStateException("Cannot override an overridden Coordinate. ");
        }
    }

    @Override
    public LongPropertyWithOverride timeProperty() {
        return (LongPropertyWithOverride) super.timeProperty();
    }

    @Override
    public ObjectPropertyWithOverride<ConceptSpecification> pathConceptProperty() {
        return (ObjectPropertyWithOverride) super.pathConceptProperty();
    }

    @Override
    public SetPropertyWithOverride<ConceptSpecification> moduleSpecificationsProperty() {
        return (SetPropertyWithOverride) super.moduleSpecificationsProperty();
    }

    @Override
    public SetPropertyWithOverride<ConceptSpecification> excludedModuleSpecificationsProperty() {
        return (SetPropertyWithOverride) super.excludedModuleSpecificationsProperty();
    }

    @Override
    public ListPropertyWithOverride<ConceptSpecification> modulePriorityOrderProperty() {
        return (ListPropertyWithOverride) super.modulePriorityOrderProperty();
    }

    @Override
    public ObjectPropertyWithOverride<StatusSet> allowedStatusProperty() {
        return (ObjectPropertyWithOverride) super.allowedStatusProperty();
    }

    @Override
    public void setExceptOverrides(StampFilterImmutable updatedCoordinate) {
        if (this.hasOverrides()) {
            long time = updatedCoordinate.getTime();
            if (timeProperty().isOverridden()) {
                time = getTime();
            }
            int pathConceptNid = updatedCoordinate.getPathNidForFilter();
            if (pathConceptProperty().isOverridden()) {
                pathConceptNid = pathConceptProperty().get().getNid();
            }
            ImmutableIntSet moduleSpecificationNids = updatedCoordinate.getModuleNids();
            if (moduleSpecificationsProperty().isOverridden()) {
                moduleSpecificationNids = getModuleNids();
            }
            ImmutableIntSet moduleExclusionNids = updatedCoordinate.getExcludedModuleNids();
            if (excludedModuleSpecificationsProperty().isOverridden()) {
                moduleExclusionNids = getExcludedModuleNids();
            }
            ImmutableIntList modulePriorityOrder = updatedCoordinate.getModulePriorityOrder();
            if (modulePriorityOrderProperty().isOverridden()) {
                modulePriorityOrder = getModulePriorityOrder();
            }
            StatusSet statusSet = updatedCoordinate.getAllowedStates();
            if (allowedStatusProperty().isOverridden()) {
                statusSet = getAllowedStates();
            }
            setValue(StampFilterImmutable.make(statusSet,
                    StampPositionImmutable.make(time, pathConceptNid),
                    moduleSpecificationNids,
                    moduleExclusionNids,
                    modulePriorityOrder));

        } else {
            setValue(updatedCoordinate);
        }
    }

    @Override
    protected ListPropertyWithOverride<ConceptSpecification> makeModulePriorityOrderProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new ListPropertyWithOverride<>(observableStampFilter.modulePriorityOrderProperty(), this);
    }

    @Override
    protected ObjectPropertyWithOverride makeAllowedStatusProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new ObjectPropertyWithOverride<>(observableStampFilter.allowedStatusProperty(), this);
    }

    @Override
    protected SetPropertyWithOverride<ConceptSpecification> makeExcludedModuleSpecificationsProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.excludedModuleSpecificationsProperty(), this);
    }

    @Override
    protected SetPropertyWithOverride<ConceptSpecification> makeModuleSpecificationsProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.moduleSpecificationsProperty(), this);
    }

    @Override
    protected LongPropertyWithOverride makeTimeProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new LongPropertyWithOverride(observableStampFilter.timeProperty(), this);
    }

    @Override
    protected ObjectPropertyWithOverride<ConceptSpecification> makePathConceptProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new ObjectPropertyWithOverride<>(observableStampFilter.pathConceptProperty(), this);
    }

    @Override
    public StampFilterImmutable getOriginalValue() {
        return StampFilterImmutable.make(allowedStatusProperty().getOriginalValue(),
                StampPositionImmutable.make(timeProperty().getOriginalValue().longValue(),
                        pathConceptProperty().getOriginalValue()),
                IntSets.immutable.of(moduleSpecificationsProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()),
                IntSets.immutable.of(excludedModuleSpecificationsProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(modulePriorityOrderProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()));
    }


    @Override
    protected StampFilterImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampFilterImmutable> observable, StampFilterImmutable oldValue, StampFilterImmutable newValue) {
        if (!this.pathConceptProperty().isOverridden()) {
            this.pathConceptProperty().setValue(newValue.getPathConceptForFilter());
        }

        if (!this.timeProperty().isOverridden()) {
            this.timeProperty().set(newValue.getStampPosition().getTime());
        }

        if (!this.modulePriorityOrderProperty().isOverridden()) {
            this.modulePriorityOrderProperty().setAll(newValue.getModulePriorityOrder().collect(nid -> Get.conceptSpecification(nid)).castToList());
        }

        if (!this.allowedStatusProperty().isOverridden()) {
            if (newValue.getAllowedStates() != this.allowedStatusProperty().get()) {
                this.allowedStatusProperty().setValue(newValue.getAllowedStates());
            }
        }

        if (!this.excludedModuleSpecificationsProperty().isOverridden()) {
            Set<ConceptSpecification> excludedModuleSet = newValue.getExcludedModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet();
            if (!excludedModuleSet.equals(this.excludedModuleSpecificationsProperty().get())) {
                this.excludedModuleSpecificationsProperty().setAll(excludedModuleSet);
            }
        }
        if (!this.moduleSpecificationsProperty().isOverridden()) {
            Set<ConceptSpecification> moduleSet = newValue.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet();
            if (!moduleSet.equals(this.moduleSpecificationsProperty().get())) {
                this.moduleSpecificationsProperty().retainAll(moduleSet);
                this.moduleSpecificationsProperty().addAll(moduleSet);
            }
        }
        return StampFilterImmutable.make(allowedStatusProperty().get(),
                StampPositionImmutable.make(timeProperty().get(),
                        pathConceptProperty().get().getNid()),
                IntSets.immutable.of(moduleSpecificationsProperty().stream().mapToInt(value -> value.getNid()).toArray()),
                IntSets.immutable.of(excludedModuleSpecificationsProperty().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(modulePriorityOrderProperty().getOriginalValue().stream().mapToInt(value -> value.getNid()).toArray()));
    }

}
