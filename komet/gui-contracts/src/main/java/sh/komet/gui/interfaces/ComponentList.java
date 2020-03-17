package sh.komet.gui.interfaces;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;

public interface ComponentList extends Comparable<ComponentList> {
    ObservableList<ObservableChronology> getComponents();
    StringProperty nameProperty();
    UUID getListId();

    default UuidStringKey getUuidStringKey() {
        return new UuidStringKey(getListId(), nameProperty().getValue());
    }

    @Override
    default int compareTo(ComponentList o) {
        return this.getListId().compareTo(o.getListId());
    }
}
