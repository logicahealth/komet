package sh.isaac.api.observable.coordinate;

import javafx.beans.property.Property;
import sh.isaac.api.coordinate.StampFilterTemplateImmutable;
import sh.isaac.api.coordinate.StampFilterTemplateProxy;

public interface ObservableStampFilterTemplate
        extends StampFilterTemplateProxy, ObservableCoordinate<StampFilterTemplateImmutable>, StampFilterTemplateProperties {

    default Property<?>[] getBaseProperties() {
        return new Property<?>[] {
                allowedStatusProperty(),
                moduleSpecificationsProperty(),
                excludedModuleSpecificationsProperty(),
                modulePriorityOrderProperty()
        };
    }

    default ObservableCoordinate<?>[] getCompositeCoordinates() {
        return new ObservableCoordinate<?>[]{

        };
    }

}
