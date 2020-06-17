package sh.isaac.api.observable.coordinate;

import javafx.beans.property.Property;

public interface PropertyWithOverride<T> extends Property<T> {
    boolean isOverridden();
    void removeOverride();
}
