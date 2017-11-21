package sh.komet.gui.search;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;

public class QueryClauseParameter {

    private Object parameterObject;

    public QueryClauseParameter(Object parameterObject) {
        this.parameterObject = parameterObject;
    }

    public String getParamterString(){
        return this.parameterObject instanceof ConceptChronology?
                ((ConceptChronology)this.parameterObject).getFullySpecifiedConceptDescriptionText()
                : this.parameterObject.toString();
    }

    public Chronology getConceptChronology(){
        return (Chronology) this.parameterObject;
    }

    public ConceptSpecification getConceptSpecification(){
        return (ConceptSpecification) this.parameterObject;
    }

    @Override
    public String toString() {
        return this.parameterObject instanceof ConceptChronology?
                ((ConceptChronology)this.parameterObject).getFullySpecifiedConceptDescriptionText()
                : this.parameterObject.toString();
    }
}
