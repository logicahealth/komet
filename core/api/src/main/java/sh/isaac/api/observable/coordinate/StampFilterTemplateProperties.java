package sh.isaac.api.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import sh.isaac.api.Status;
import sh.isaac.api.component.concept.ConceptSpecification;

interface StampFilterTemplateProperties {

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
     *
     * @return the specified modules property
     */
    SetProperty<ConceptSpecification> excludedModuleSpecificationsProperty();

    /**
     * Module preference list property.
     *
     * @return the object property
     */
    ListProperty<ConceptSpecification> modulePriorityOrderProperty();
}
