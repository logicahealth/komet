package sh.isaac.model.observable.coordinate;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

public class ObservableStampFilterImpl extends ObservableStampFilterBase {


    private ObservableStampFilterImpl(StampFilterImmutable stampFilterImmutable, String coordinateName) {
        super(stampFilterImmutable, coordinateName);
    }


    private ObservableStampFilterImpl(StampFilterImmutable stampFilterImmutable) {
        super(stampFilterImmutable, "Stamp filter");
    }

    @Override
    protected ListProperty<ConceptSpecification> makeModulePriorityOrderProperty(StampFilter stampFilter) {
        return new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_PREFERENCE_LIST_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(stampFilter.getModulePriorityOrder().collect(nid -> Get.conceptSpecification(nid)).castToList()));
    }

    @Override
    protected LongProperty makeTimeProperty(StampFilter stampFilter) {
        return new SimpleLongProperty(this,
                ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
                stampFilter.getStampPosition().getTime());
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampFilter stampFilter) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampFilter.getPathConceptForFilter());
    }

    @Override
    protected SetProperty<Status> makeAllowedStatusProperty(StampFilter stampFilter) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.ALLOWED_STATES_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilter.getAllowedStates().toEnumSet()));
    }

    @Override
    protected SetProperty<ConceptSpecification> makeExcludedModuleSpecificationsProperty(StampFilter stampFilter) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_EXCLUSION_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilter.getExcludedModuleNids().collect(nid -> Get.conceptSpecification(nid)).toSet()));
    }

    @Override
    protected SetProperty<ConceptSpecification> makeModuleSpecificationsProperty(StampFilter stampFilter) {
        return new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(stampFilter.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).toSet()));
    }

    public static ObservableStampFilterImpl make(StampFilter stampFilter) {
        return new ObservableStampFilterImpl(stampFilter.toStampFilterImmutable());
    }
    public static ObservableStampFilterImpl make(StampFilter stampFilter, String coordinateName) {
        return new ObservableStampFilterImpl(stampFilter.toStampFilterImmutable(), coordinateName);
    }

}
