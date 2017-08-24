package sh.komet.gui.control;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertySheetItemConceptWrapper implements ConceptSpecification, PropertySheet.Item {

    private Manifold manifold;
    private final int conceptSequence;
    private final String name;
    private final SimpleObjectProperty<PropertySheetItemConceptWrapper> observableWrapper;

    public PropertySheetItemConceptWrapper(Manifold manifold, int conceptSequence, String name) {
        this.manifold = manifold;
        this.conceptSequence = conceptSequence;
        this.name = name;
        this.observableWrapper = new SimpleObjectProperty<>(this);
    }

    @Override
    public String getFullySpecifiedConceptDescriptionText() {
        return this.manifold.getFullySpecifiedDescriptionText(this.conceptSequence);
    }

    @Override
    public Optional<String> getPreferedConceptDescriptionText() {
        return Optional.of(manifold.getPreferredDescriptionText(this.conceptSequence));
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
        return null;
    }

    @Override
    public Object getValue() {
        return this.observableWrapper.get();
    }

    @Override
    public void setValue(Object value) {
        this.observableWrapper.setValue((PropertySheetItemConceptWrapper) value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(this.observableWrapper);
    }

    @Override
    public String toString() {
        return getPreferedConceptDescriptionText().get();
    }
}
