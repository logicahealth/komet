package sh.isaac.api.query.clauses;

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.*;
import sh.isaac.api.query.properties.ConceptClause;
import sh.isaac.api.query.properties.ManifoldClause;
import sh.isaac.api.query.properties.TaxonomyDistanceClause;
import sh.isaac.api.query.properties.UndirectedTaxonomyClause;

import java.util.EnumSet;
import java.util.Map;

public class TaxonomyRelatedConcepts
        extends LeafClause
        implements ManifoldClause, ConceptClause, TaxonomyDistanceClause, UndirectedTaxonomyClause {

    /**
     * The parent concept spec key.
     */
    LetItemKey referenceConceptSpecKey;

    /**
     * the manifold coordinate key.
     */
    LetItemKey manifoldCoordinateKey;

    LetItemKey taxonomyDistanceKey;

    LetItemKey undirectedTaxonomyKey;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new refset contains concept.
     */
    public TaxonomyRelatedConcepts() {
    }

    /**
     * Instantiates a new refset contains concept.
     *
     * @param enclosingQuery          the enclosing query
     * @param referenceConceptSpecKey the concept spec key
     * @param manifoldCoordinateKey   the manifold coordinate key
     */
    public TaxonomyRelatedConcepts(Query enclosingQuery,
                                   LetItemKey referenceConceptSpecKey,
                                   LetItemKey manifoldCoordinateKey,
                                   LetItemKey taxonomyDistanceKey) {
        super(enclosingQuery);
        this.referenceConceptSpecKey = referenceConceptSpecKey;
        this.manifoldCoordinateKey = manifoldCoordinateKey;
        this.taxonomyDistanceKey = taxonomyDistanceKey;
    }

    //~--- methods -------------------------------------------------------------


    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the compute phases.
     *
     * @return the compute phases
     */
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    public LetItemKey getConceptSpecKey() {
        return referenceConceptSpecKey;
    }

    public void setConceptSpecKey(LetItemKey referenceConceptSpecKey) {
        this.referenceConceptSpecKey = referenceConceptSpecKey;
    }

    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LetItemKey getManifoldCoordinateKey() {
        return manifoldCoordinateKey;
    }

    @Override
    public void setManifoldCoordinateKey(LetItemKey manifoldCoordinateKey) {
        this.manifoldCoordinateKey = manifoldCoordinateKey;
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.CONCEPT_HAS_TAXONOMY_DISTANCE_FROM;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public final WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(getClauseSemantic());
        whereClause.getLetKeys()
                .add(this.referenceConceptSpecKey);
        whereClause.getLetKeys()
                .add(this.manifoldCoordinateKey);
        return whereClause;
    }

    public LetItemKey getTaxonomyDistanceKey() {
        return taxonomyDistanceKey;
    }

    public void setTaxonomyDistanceKey(LetItemKey taxonomyDistanceKey) {
        this.taxonomyDistanceKey = taxonomyDistanceKey;
    }

    @Override
    public LetItemKey getUndirectedTaxonomyKey() {
        return undirectedTaxonomyKey;
    }

    @Override
    public void setUndirectedTaxonomyKey(LetItemKey undirectedTaxonomyKey) {
        this.undirectedTaxonomyKey = undirectedTaxonomyKey;
    }
}