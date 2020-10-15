package sh.isaac.komet.batch;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.util.UuidStringKey;
import sh.komet.gui.interfaces.ComponentList;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class AllComponentList implements ComponentList {

    public static final String ALL_COMPONENTS = "All components";
    UUID listUuid = UUID.fromString("f7b9b7d1-1ee8-4953-949a-a3a19f79813f");
    UuidStringKey listKey = new UuidStringKey(listUuid, ALL_COMPONENTS);
    ReadOnlyStringWrapper nameProperty = new ReadOnlyStringWrapper(ALL_COMPONENTS);

    @Override
    public int listSize() {
        return (int) Get.identifierService().getNidStream(true).count();
    }

    @Override
    public Stream<Chronology> getComponentStream() {
        return Get.identifiedObjectService().getChronologySteam(true);
    }

    @Override
    public Optional<ObservableList<ObservableChronology>> getOptionalObservableComponentList() {
        return Optional.empty();
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public UuidStringKey getUuidStringKey() {
        return listKey;
    }
}
