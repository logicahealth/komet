/*
 * Copyright 2013 International Health Terminology Standards Development 
 * Organisation.
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
package gov.vha.isaac.ochre.query.provider;

import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Statements that are used to retrieve desired components in a
 * <code>Query</code>. Clauses in a Query are organized into a tree and computed
 * using a depth-first search. The root node in a Query must be an instance of
 * a
 * <code>ParentClause</code>.
 *
 * @author kec
 */
@XmlRootElement(name = "CLAUSE")
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class Clause {

    /**
     * The instance of
     * <code>Query</code> that contains the specifications.
     */
    protected Query enclosingQuery;
    Clause parent = null;
    protected static final EnumSet<ClauseComputeType> PRE_AND_POST_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION, ClauseComputeType.POST_ITERATION);
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION);
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION_AND_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION, ClauseComputeType.ITERATION);
    protected static final EnumSet<ClauseComputeType> ITERATION = EnumSet.of(ClauseComputeType.ITERATION);
    protected static final EnumSet<ClauseComputeType> POST_ITERATION = EnumSet.of(ClauseComputeType.POST_ITERATION);

    /**
     * Getter for the
     * <code>Query</code> that contains the specifications used to retrieve
     * components that satisfy criterion.
     *
     * @return the enclosing <code>Query</code>
     */
    public Query getEnclosingQuery() {
        return enclosingQuery;
    }

    public void setParent(Clause parent) {
        this.parent = parent;
    }

    public Clause(Query enclosingQuery) {
        this.enclosingQuery = enclosingQuery;
        enclosingQuery.getComputePhases().addAll(getComputePhases());
    }

    /**
     * Default no arg constructor for Jaxb.
     */
    protected Clause() {
        super();
    }


    /**
     * Getter for the
     * <code>Where</code> clause, which is the syntax of the query.
     *
     * @return the where clause of the query
     */
    public abstract WhereClause getWhereClause();

    public abstract List<Clause> getChildren();

    /**
     * Getter for the iteration types required to compute the clause.
     *
     * @return an <code>EnumSet</code> of <code>ClauseComputeType</code>
     * elements for the clause.
     */
    public abstract EnumSet<ClauseComputeType> getComputePhases();

    /**
     * Compute components that meet the where clause criterion without using
     * iteration. If the set of possibilities cannot be computed without
     * iteration, the set of incomingPossibleComponents will be returned.
     *
     * @param incomingPossibleComponents
     * @return
     */
    public abstract NidSet computePossibleComponents(
            NidSet incomingPossibleComponents);

    /**
     * Collect intermediate results for clauses that require iteration over the
     * database. This method will only be called if one of the clauses has a
     * compute type of
     * <code>ClauseComputeType.ITERATION</code>. The clause will cache results,
     * and return the final results during the computeComponents method.
     *
     * @param conceptVersion
     */
    public abstract void getQueryMatches(ConceptVersion conceptVersion);

    /**
     * Compute final results based on possible components, and any cached query
     * matches. This third pass was necessary to support the NOT operator.
     *
     * @param incomingComponents
     * @return
     */
    public abstract NidSet computeComponents(
            NidSet incomingComponents);
}
