package sh.isaac.api.query.clauses;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.WhereClause;

import java.util.EnumSet;

public class AssociatedParameter extends LeafClause {

    private ConceptSpecification associatedSpecParameter;
    private String associatedStringParameter;


    public void setAssociatedSpecParameter(ConceptSpecification associatedSpecParameter) {
        this.associatedSpecParameter = associatedSpecParameter;
    }

    public String getAssociatedStringParameter() {
        return associatedStringParameter;
    }

    public void setAssociatedStringParameter(String associatedStringParameter) {
        this.associatedStringParameter = associatedStringParameter;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        return null;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {

    }

    @Override
    public WhereClause getWhereClause() {
        return null;
    }

    @Override
    public ConceptSpecification getClauseConcept() {
        return TermAux.ASSOCIATED_PARAMETER_QUERY_CLAUSE;
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return null;
    }
}
