package sh.isaac.api.query.properties;

import sh.isaac.api.query.LetItemKey;

public interface TaxonomyDistanceClause {
    LetItemKey getTaxonomyDistanceKey();

    void setTaxonomyDistanceKey(LetItemKey taxonomyDistanceKey);
}
