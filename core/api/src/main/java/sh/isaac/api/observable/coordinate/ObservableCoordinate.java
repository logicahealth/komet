package sh.isaac.api.observable.coordinate;

import javafx.beans.property.Property;
import sh.isaac.api.coordinate.ImmutableCoordinate;

public interface ObservableCoordinate<T extends ImmutableCoordinate> extends Property<T> {

}
