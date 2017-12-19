package sh.komet.gui.search.flowr;

import sh.isaac.api.component.concept.ConceptChronology;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        StringBuilder stringBuilder = new StringBuilder();

        if(this.parameter instanceof ConceptChronology){
            stringBuilder.append(((ConceptChronology)this.parameter).getFullySpecifiedConceptDescriptionText());
        }else if(this.parameter instanceof HashMap) {
            Set<Map.Entry> entries = ((HashMap)this.parameter).entrySet();
            for(Map.Entry entry : entries){
                stringBuilder.append(entry.getKey() + ": " + entry.getValue());
            }
        }else{
            stringBuilder.append(this.parameter.toString());
        }

        return stringBuilder.toString();
    }
}
