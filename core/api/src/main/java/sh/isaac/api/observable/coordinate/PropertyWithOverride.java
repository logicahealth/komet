package sh.isaac.api.observable.coordinate;

import javafx.beans.property.Property;
import sh.isaac.api.coordinate.ManifoldCoordinate;

public interface PropertyWithOverride<T> extends Property<T> {
    boolean isOverridden();

    void removeOverride();

    Property<T> overriddenProperty();

    default String getOverrideName(ManifoldCoordinate manifoldCoordinate) {
        String name = manifoldCoordinate.toPreferredConceptString(overriddenProperty().getName());
        if (isOverridden()) {
            return name + " with override";
        }
        return name;
    }
}
