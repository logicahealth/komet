package sh.isaac.komet.batch;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.observable.ObservableChronology;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.util.UuidStringKey;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class ConceptComponentList implements ComponentList {

    public static final String ALL_CONCEPTS = "All concepts";
    UUID listUuid = UUID.fromString("7ca23cd0-696f-11ea-bc55-0242ac130003");
    UuidStringKey listKey = new UuidStringKey(listUuid, ALL_CONCEPTS);
    ReadOnlyStringWrapper nameProperty = new ReadOnlyStringWrapper(ALL_CONCEPTS);

    @Override
    public int listSize() {
        return (int) Get.identifierService().getNidsForAssemblage(TermAux.SOLOR_CONCEPT_ASSEMBLAGE).count();
    }

    @Override
    public Stream<Chronology> getComponentStream() {
        return Get.conceptService().getConceptChronologyStream().map(conceptChronology -> (Chronology) conceptChronology);
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
