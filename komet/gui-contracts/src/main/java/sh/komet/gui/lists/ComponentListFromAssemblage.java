package sh.komet.gui.lists;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.util.UuidStringKey;
import sh.komet.gui.interfaces.ComponentList;

import java.util.Optional;
import java.util.stream.Stream;

public class ComponentListFromAssemblage implements ComponentList  {

    private final ManifoldCoordinate manifoldCoordinate;
    private final ConceptSpecification assemblageConcept;
    private final SimpleStringProperty nameProperty = new SimpleStringProperty();

    public ComponentListFromAssemblage(ManifoldCoordinate manifoldCoordinate, ConceptSpecification assemblageConcept) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.assemblageConcept = assemblageConcept;
        this.nameProperty.setValue(manifoldCoordinate.getPreferredDescriptionText(assemblageConcept));
    }

    @Override
    public Stream<Chronology> getComponentStream() {
        SingleAssemblageSnapshot<SemanticVersion> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(this.assemblageConcept, SemanticVersion.class, manifoldCoordinate.getViewStampFilter().toStampFilterImmutable());
        return snapshot.getLatestSemanticVersionsFromAssemblage()
                .filter(semanticVersionLatestVersion -> semanticVersionLatestVersion.isPresent()).map(semanticVersionLatestVersion -> semanticVersionLatestVersion.get().getChronology() );
    }

    @Override
    public int listSize() {
        SingleAssemblageSnapshot<SemanticVersion> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(this.assemblageConcept, SemanticVersion.class, manifoldCoordinate.getViewStampFilter().toStampFilterImmutable());
        return (int) snapshot.getLatestSemanticVersionsFromAssemblage().count();
    }

    @Override
    public Optional<ObservableList<ObservableChronology>> getOptionalObservableComponentList() {
        SingleAssemblageSnapshot<SemanticVersion> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(this.assemblageConcept, SemanticVersion.class, manifoldCoordinate.getViewStampFilter().toStampFilterImmutable());
        ObservableList<ObservableChronology> chronologyList = FXCollections.observableArrayList();
        snapshot.getLatestSemanticVersionsFromAssemblage()
                .filter(semanticVersionLatestVersion -> semanticVersionLatestVersion.isPresent()).forEach(semanticVersionLatestVersion ->
        {
            chronologyList.add(Get.observableChronology(semanticVersionLatestVersion.get().getNid()));
        });
        return Optional.of(chronologyList);
    }

    @Override
    public StringProperty nameProperty() {
        return this.nameProperty;
    }

    @Override
    public UuidStringKey getUuidStringKey() {
        return new UuidStringKey(assemblageConcept.getPrimordialUuid(), this.nameProperty.get());
    }
}
