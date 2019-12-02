package sh.komet.gui.provider.classification;

import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.Optional;

public class StringWithOptionalConceptSpec {
    final String label;
    final ConceptSpecification conceptSpecification;

    public StringWithOptionalConceptSpec(String label, ConceptSpecification conceptSpecification) {
        this.label = label;
        this.conceptSpecification = conceptSpecification;
    }
    public StringWithOptionalConceptSpec(String label) {
        this.label = label;
        this.conceptSpecification = null;
    }

    public String getLabel() {
        return label;
    }

    public Optional<ConceptSpecification> getOptionalConceptSpecification() {
        return Optional.ofNullable(conceptSpecification);
    }

    @Override
    public String toString() {
        return label;
    }
}
