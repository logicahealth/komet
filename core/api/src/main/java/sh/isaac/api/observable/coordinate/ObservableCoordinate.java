package sh.isaac.api.observable.coordinate;

import javafx.beans.property.Property;
import sh.isaac.api.coordinate.ImmutableCoordinate;

public interface ObservableCoordinate<T extends ImmutableCoordinate> extends Property<T> {
    /**
     *
     * @return The properties this coordinate defines, not the properties that contained
     * coordinates may define.
     */
    Property<?>[] getBaseProperties();

    /**
     *
     * @return composite coordinates, so that properties of composite coordinates can be
     * recursively identified.
     */
    ObservableCoordinate<?>[] getCompositeCoordinates();
}
