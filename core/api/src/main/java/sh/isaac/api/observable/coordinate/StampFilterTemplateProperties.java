package sh.isaac.api.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StatusSet;

interface StampFilterTemplateProperties {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                allowedStatusProperty(),
                moduleSpecificationsProperty(),
                excludedModuleSpecificationsProperty(),
                modulePriorityOrderProperty()
        };
    }

    default ObservableCoordinate<?>[] getEmbeddedCoordinates() {
        return new ObservableCoordinate<?>[]{

        };
    }

    /**
     *
     * @return a set of allowed status values to filter computation results.
     */
    ObjectProperty<StatusSet> allowedStatusProperty();

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
