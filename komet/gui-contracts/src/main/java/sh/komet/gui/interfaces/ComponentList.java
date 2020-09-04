package sh.komet.gui.interfaces;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.util.UuidStringKey;

import java.util.Optional;
import java.util.stream.Stream;

public interface ComponentList extends Comparable<ComponentList> {

    Stream<Chronology> getComponentStream();

    int listSize();

    Optional<ObservableList<ObservableChronology>> getOptionalObservableComponentList();

    StringProperty nameProperty();

    UuidStringKey getUuidStringKey();

    @Override
    default int compareTo(ComponentList o) {
        return this.getUuidStringKey().compareTo(o.getUuidStringKey());
    }
}
