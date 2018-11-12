/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.api.query.clauses;

//~--- JDK imports ------------------------------------------------------------
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.query.LetItemKey;

//~--- classes ----------------------------------------------------------------
/**
 * Returns descriptions matching the input string using Lucene.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionLuceneMatch
        extends LeafClause {

    public static final LetItemKey DEFAULT_QUERY_STRING_KEY 
            = new LetItemKey("Default query string key", 
                    UUID.fromString("dd11c59a-9afd-4a15-a4a9-32fb414f3299"));
    
    /**
     * the manifold coordinate key.
     */

    private LetItemKey queryStringKey = DEFAULT_QUERY_STRING_KEY;
    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new description lucene match.
     */
    public DescriptionLuceneMatch() {
    }

    /**
     * Instantiates a new description lucene match.
     *
     * @param enclosingQuery the enclosing query
     * @param queryStringKey the lucene match key
     */
    public DescriptionLuceneMatch(Query enclosingQuery, LetItemKey queryStringKey) {
        super(enclosingQuery);
        this.queryStringKey = queryStringKey;
    }
    /**
     * Instantiates a new description lucene match using the DEFAULT_QUERY_STRING_KEY.
     *
     * @param enclosingQuery the enclosing query
     */
    public DescriptionLuceneMatch(Query enclosingQuery) {
        super(enclosingQuery);
    }


    //~--- methods -------------------------------------------------------------
    @Override
    public void resetResults() {
        // no cached data in task. 
    }

    /**
     * Compute possible components.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    @Override
    public final Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        final NidSet nids = new NidSet();
        IndexDescriptionQueryService descriptionIndexer = LookupService.get().getService(IndexDescriptionQueryService.class);

        if (descriptionIndexer == null) {
            throw new IllegalStateException("No description indexer found on classpath");
        }

        final List<SearchResult> queryResults = descriptionIndexer.query((String) this.enclosingQuery.getLetDeclarations().get(queryStringKey), 1000);

        NidSet incomingPossibleSet = incomingPossibleComponents.get(this.getAssemblageForIteration());
        queryResults.stream().forEach((s) -> {
            if (incomingPossibleSet.contains(s.getNid())) {
                nids.add(s.getNid());
            }

        });

      getResultsCache().or(nids);
      HashMap<ConceptSpecification, NidSet> resultsMap = new HashMap<>(incomingPossibleComponents);
      resultsMap.put(this.getAssemblageForIteration(), getResultsCache());
      return resultsMap;
    }

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

    /**
     * Gets the query matches.
     *
     * @param conceptVersion the concept version
     */
    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        getResultsCache();
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.DESCRIPTION_LUCENE_MATCH;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.DESCRIPTION_LUCENE_MATCH);
        whereClause.getLetKeys()
                .add(this.queryStringKey);
        return whereClause;
    }

    @Override
    public ConceptSpecification getClauseConcept() {
        return TermAux.DESCRIPTION_LUCENE_MATCH_QUERY_CLAUSE;
    }
    public void setQueryStringKey(LetItemKey queryStringKey) {
        this.queryStringKey = queryStringKey;
    }

    @XmlElement
    public LetItemKey getQueryStringKey() {
        return this.queryStringKey;
    }    
    
    public String getQueryText() {
        return getLetItem(queryStringKey);
    }
    
}
