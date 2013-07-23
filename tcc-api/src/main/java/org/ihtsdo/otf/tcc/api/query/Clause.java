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
package org.ihtsdo.otf.tcc.api.query;

import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import java.io.IOException;
import java.util.EnumSet;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 *
 * @author kec
 */
public abstract class Clause {
    
    Query enclosingQuery;
    Clause parent = null;
    protected static final EnumSet<ClauseComputeType> PRE_AND_POST_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION, ClauseComputeType.POST_ITERATION);
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION);
    protected static final EnumSet<ClauseComputeType> PRE_ITERATION_AND_ITERATION = EnumSet.of(ClauseComputeType.PRE_ITERATION, ClauseComputeType.ITERATION);
    protected static final EnumSet<ClauseComputeType> ITERATION = EnumSet.of(ClauseComputeType.ITERATION);

    public Query getEnclosingQuery() {
        return enclosingQuery;
    }

    public Clause getParent() {
        return parent;
    }

    public void setParent(Clause parent) {
        this.parent = parent;
    }

    public Clause(Query enclosingQuery) {
        this.enclosingQuery = enclosingQuery;
        enclosingQuery.getComputeTypes().addAll(getComputePhases());
    }
    
    public Clause[] getChildren() {
        return new Clause[] {};
    }
    
    /**
     * 
     * @return the ClauseComputeType for this clause. 
     */
    
    public abstract EnumSet<ClauseComputeType> getComputePhases();
    
    /**
     * Compute components that meet the where clause criterion without using 
     * iteration. If the set of possibilities cannot be computed without iteration, 
     * the set of incomingPossibleComponents will be returned. 
     * @param incomingPossibleComponents
     * @return 
     */
    public abstract NativeIdSetBI computePossibleComponents(
            NativeIdSetBI incomingPossibleComponents) throws IOException, ValidationException, ContradictionException;
    
    
    /**
     * Collect intermediate results for clauses that require iteration over the 
     * database. This method will only be called if one of the clauses has a 
     * compute type of <code>ClauseComputeType.ITERATION</code>. The clause
     * will cache results, and return the final results during the computeComponents
     * method. 
     * @param conceptVersion
     */
    public abstract void getQueryMatches(ConceptVersionBI conceptVersion) 
            throws IOException, ContradictionException;
    
    
    /**
     * Compute final results based on possible components, 
     *  and any cached query matches. This third pass was necessary to 
     * support the NOT operator. 
     * @param incomingComponents
     * @return
     * @throws IOException 
     */
    public abstract NativeIdSetBI computeComponents(
            NativeIdSetBI incomingComponents) throws IOException, ValidationException, ContradictionException;

}
