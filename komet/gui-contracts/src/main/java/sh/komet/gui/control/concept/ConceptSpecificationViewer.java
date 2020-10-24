package sh.komet.gui.control.concept;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;

public class ConceptSpecificationViewer implements PropertyEditor<ConceptSpecification> {

    private final SimpleObjectProperty<ConceptSpecification> conceptSpecificationValue;
    private final Label conceptLabel = new Label();
    private final ManifoldCoordinate manifoldCoordinate;

    public ConceptSpecificationViewer(PropertySheetItemReadOnlyConceptWrapper wrapper, ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.conceptSpecificationValue = (SimpleObjectProperty<ConceptSpecification>) wrapper.getObservableValue().get();
        this.conceptSpecificationValue.addListener(this::setLabelText);
        if (wrapper.getValue() != null) {
            this.conceptLabel.setText(manifoldCoordinate.getPreferredDescriptionText(wrapper.getValue()));
        } else {
            this.conceptLabel.setText("Empty");
        }
    }
    @Override
    public Node getEditor() {
        return conceptLabel;
    }

    @Override
    public ConceptSpecification getValue() {
        return this.conceptSpecificationValue.get();
    }

    @Override
    public void setValue(ConceptSpecification value) {
        this.conceptSpecificationValue.set(value);
    }

    private void setLabelText(ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) {
        this.conceptLabel.setText(manifoldCoordinate.getPreferredDescriptionText(conceptSpecificationValue.get()));
    }
}
