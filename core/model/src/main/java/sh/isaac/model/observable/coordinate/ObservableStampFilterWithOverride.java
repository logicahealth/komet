package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.LongPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

import java.util.Collection;

public class ObservableStampFilterWithOverride extends ObservableStampFilterBase {

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter) {
        super(stampFilter, stampFilter.getName());
    }

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter, String coordinateName) {
        super(stampFilter, coordinateName);
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
    public SetPropertyWithOverride<Status> allowedStatusProperty() {
        return (SetPropertyWithOverride) super.allowedStatusProperty();
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
    protected SetPropertyWithOverride<Status> makeAllowedStatusProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.allowedStatusProperty(), this);
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
}
