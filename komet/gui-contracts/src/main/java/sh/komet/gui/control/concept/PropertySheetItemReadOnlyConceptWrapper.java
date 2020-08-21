package sh.komet.gui.control.concept;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.util.FxGet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertySheetItemReadOnlyConceptWrapper implements ConceptSpecification, PropertySheet.Item {

    private final ManifoldCoordinate manifoldCoordinate;
    private final String name;
    private final SimpleObjectProperty<ConceptSpecification> conceptProperty;

    private ConceptSpecification propertySpecification = null;

    public PropertySheetItemReadOnlyConceptWrapper(ManifoldCoordinate manifoldCoordinate,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty) {
        this(manifoldCoordinate, manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(conceptProperty.getName())), conceptProperty);
    }

    public PropertySheetItemReadOnlyConceptWrapper(ManifoldCoordinate manifoldCoordinate, String name,
                                           ObjectProperty<? extends ConceptSpecification> conceptProperty) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.name = name;
        this.conceptProperty = (SimpleObjectProperty<ConceptSpecification>) conceptProperty;

    }

    @Override
    public String getFullyQualifiedName() {
        return this.manifoldCoordinate.getFullyQualifiedDescriptionText(conceptProperty.get());
    }

    @Override
    public Optional<String> getRegularName() {
        return Optional.of(manifoldCoordinate.getPreferredDescriptionText(conceptProperty.get()));
    }

    @Override
    public List<UUID> getUuidList() {
        return new ConceptProxy(conceptProperty.getName()).getUuidList();
    }

    @Override
    public Class<?> getType() {
        return ConceptSpecification.class;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "Select the proper concept value for the version you wish to create. ";
    }

    @Override
    public ConceptSpecification getValue() {
        return this.conceptProperty.get();
    }

    public boolean isEditable() {
        return false;
    }
    @Override
    public void setValue(Object value) {
        try {
            throw new IllegalStateException("Cannot set value on read-only concept property. ");
        } catch (RuntimeException ex) {
            FxGet.statusMessageService().reportStatus(ex.getMessage());
        }
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.conceptProperty);
    }

    public ConceptSpecification getSpecification() {
        if (this.propertySpecification != null) {
            return this.propertySpecification;
        }
        return new ConceptProxy(this.conceptProperty.getName());
    }

    public void setSpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification = propertySpecification;
    }

    @Override
    public String toString() {
        return "Property sheet item for "
                + manifoldCoordinate.getPreferredDescriptionText(new ConceptProxy(getSpecification().toExternalString()));
    }
}
