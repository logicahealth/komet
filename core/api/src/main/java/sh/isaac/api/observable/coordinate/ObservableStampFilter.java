package sh.isaac.api.observable.coordinate;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.StampFilterProxy;

public interface ObservableStampFilter extends StampFilterProxy, StampFilterTemplateProperties, ObservableCoordinate<StampFilterImmutable>  {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                pathConceptProperty(),
                timeProperty(),
                moduleSpecificationsProperty(),
                excludedModuleSpecificationsProperty(),
                allowedStatusProperty(),
                modulePriorityOrderProperty()
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[]{

        };
    }

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

}
