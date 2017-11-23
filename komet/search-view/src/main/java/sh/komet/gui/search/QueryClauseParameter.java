package sh.komet.gui.search;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;

public class QueryClauseParameter<T> {

    private T parameter;

    public QueryClauseParameter(T parameter) {
        this.parameter = parameter;
    }

    public T getParameter() {
        return parameter;
    }

    public void setParameter(T parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return this.parameter instanceof ConceptChronology?
                ((ConceptChronology)this.parameter).getFullySpecifiedConceptDescriptionText()
                : this.parameter.toString();
    }
}
