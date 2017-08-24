package sh.komet.gui.control;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;

public class PropertySheetItemConceptWrapper implements ConceptSpecification, PropertySheet.Item {

    private final Manifold manifoldForModification;
    private final Manifold manifoldForDisplay;
    private final int conceptSequence;
    private final String name;
    private final SimpleObjectProperty<ConceptForControlWrapper> observableWrapper;
    private final IntegerProperty conceptSequenceProperty;

    public PropertySheetItemConceptWrapper(Manifold manifoldForModification, Manifold manifoldForDisplay, int conceptSequence, String name, 
            IntegerProperty conceptSequenceProperty) {
        this.manifoldForModification = manifoldForModification;
        this.manifoldForDisplay = manifoldForDisplay;
        this.conceptSequence = conceptSequence;
        this.name = name;
        this.observableWrapper = new SimpleObjectProperty<>(new ConceptForControlWrapper(manifoldForDisplay, conceptSequence));
        this.conceptSequenceProperty = conceptSequenceProperty;
    }

    @Override
    public String getFullySpecifiedConceptDescriptionText() {
        return this.manifoldForDisplay.getFullySpecifiedDescriptionText(this.conceptSequence);
    }

    @Override
    public Optional<String> getPreferedConceptDescriptionText() {
        return Optional.of(manifoldForDisplay.getPreferredDescriptionText(this.conceptSequence));
    }

    @Override
    public List<UUID> getUuidList() {
        return null;
    }

    @Override
    public Class<?> getType() {
        return null;
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
        return "Tooltip for the property sheet item we are editing. ";
    }

    @Override
    public ConceptForControlWrapper getValue() {
        return this.observableWrapper.get();
    }

    @Override
    public void setValue(Object value) {
        this.observableWrapper.setValue((ConceptForControlWrapper) value);
        this.conceptSequenceProperty.setValue(((ConceptForControlWrapper) value).getConceptSequence());
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.conceptSequenceProperty);
    }

    @Override
    public String toString() {
        return "Property sheet item we are editing...";
    }
}
