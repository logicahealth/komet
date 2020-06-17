package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.observable.coordinate.ObservableStampFilter;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.LongPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

import java.util.Collection;

public class ObservableStampFilterWithOverride extends ObservableStampFilterBase {

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter) {
        super(stampFilter, "Stamp filter with override");
    }

    public ObservableStampFilterWithOverride(ObservableStampFilter stampFilter, String coordinateName) {
        super(stampFilter, coordinateName);
    }

    @Override
    protected ListProperty<ConceptSpecification> makeModulePriorityOrderProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new ListPropertyWithOverride<>(observableStampFilter.modulePriorityOrderProperty(), this);
    }

    @Override
    protected SetProperty<Status> makeAllowedStatusProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.allowedStatusProperty(), this);
    }

    @Override
    protected SetProperty<ConceptSpecification> makeExcludedModuleSpecificationsProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.excludedModuleSpecificationsProperty(), this);
    }

    @Override
    protected SetProperty<ConceptSpecification> makeModuleSpecificationsProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new SetPropertyWithOverride<>(observableStampFilter.moduleSpecificationsProperty(), this);
    }

    @Override
    protected LongProperty makeTimeProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new LongPropertyWithOverride(observableStampFilter.timeProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampFilter stampFilter) {
        ObservableStampFilter observableStampFilter = (ObservableStampFilter) stampFilter;
        return new ObjectPropertyWithOverride<>(observableStampFilter.pathConceptProperty(), this);
    }
}
