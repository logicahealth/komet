package sh.isaac.api.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilterProxy;

public interface ObservableStampFilter extends StampFilterProxy {

    /**
     *
     * @return property that identifies the time for this filter.
     */
    LongProperty timeProperty();

    /**
     *
     * @return the property that identifies the path concept for this path coordinate
     */
    ObjectProperty<ConceptSpecification> pathConceptProperty();

    /**
     *
     * @return a set of allowed status values to filter computation results.
     */
    SetProperty<Status> allowedStatusProperty();


    /**
     *
     * @return the specified modules property
     */
    SetProperty<ConceptSpecification> moduleSpecificationsProperty();

    /**
     * Module preference list property.
     *
     * @return the object property
     */
    ListProperty<ConceptSpecification> modulePreferenceListForVersionsProperty();

}
