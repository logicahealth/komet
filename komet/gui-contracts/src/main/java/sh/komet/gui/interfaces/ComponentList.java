package sh.komet.gui.interfaces;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.observable.ObservableChronology;

import java.util.UUID;

public interface ComponentList extends Comparable<ComponentList> {
    ObservableList<ObservableChronology> getComponents();
    StringProperty nameProperty();
    UUID getListId();

    @Override
    default int compareTo(ComponentList o) {
        return this.getListId().compareTo(o.getListId());
    }
}
