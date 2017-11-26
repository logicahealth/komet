package sh.komet.gui.search;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;

public class QueryClauseParameter<T> {

    private T parameter;
    private boolean isEmpty = true;

    public T getParameter() {
        return parameter;
    }

    public void setParameter(T parameter) {
        this.parameter = parameter;
    }

    public boolean isEmpty(){
        return parameter == null;
    }

    @Override
    public String toString() {
        return this.parameter instanceof ConceptChronology?
                ((ConceptChronology)this.parameter).getFullySpecifiedConceptDescriptionText()
                : this.parameter.toString();
    }
}
