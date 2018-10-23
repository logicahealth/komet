/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.query;

import java.util.HashMap;
import java.util.Map;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import static sh.isaac.api.query.Clause.getParentClauses;

/**
 *
 * @author kec
 */
public class Join 
        extends ParentClause {

    /**
     * Default no arg constructor for Jaxb.
     */
    public Join() {
        super();
    }

    /**
     * Instantiates a new or.
     *
     * @param enclosingQuery the enclosing query
     * @param clauses the clauses
     */
    public Join(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compute components.
     *
     * @param incomingComponents the incoming components
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> incomingComponents) {
        final NidSet results = new NidSet();

        getChildren().stream().forEach((clause) -> {
            results.or(clause.computeComponents(incomingComponents).get(clause.getAssemblageForIteration()));
            setAssemblageForIteration(clause.getAssemblageForIteration());
        });
        HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingComponents);
        resultsMap.put(this.getAssemblageForIteration(), results);
        return resultsMap;
    }

    /**
     * Compute possible components.
     *
     * @param searchSpace the search space
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> searchSpace) {
        final NidSet results = new NidSet();

        getChildren().stream().forEach((clause) -> {
            results.or(clause.computePossibleComponents(searchSpace).get(clause.getAssemblageForIteration()));
            setAssemblageForIteration(clause.getAssemblageForIteration());
        });
        HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(searchSpace);
        resultsMap.put(this.getAssemblageForIteration(), results);
        return resultsMap;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.JOIN;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.JOIN);
        getChildren().stream().forEach((clause) -> {
            whereClause.getChildren()
                    .add(clause.getWhereClause());
        });
        return whereClause;
    }

    @Override
    public ConceptSpecification getClauseConcept() {
        return TermAux.JOIN_QUERY_CLAUSE;
    }

    @Override
    public Clause[] getAllowedSubstutitionClauses() {
        return getParentClauses();
    }

    @Override
    public Clause[] getAllowedChildClauses() {
        return getAllClauses();
    }

    @Override
    public Clause[] getAllowedSiblingClauses() {
        return getAllClauses();
    }
}
