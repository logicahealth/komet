/*

 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.Collections;
import java.util.List;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A leaf in the computation tree of clauses of a query. A LeafClause
 * cannot have any child clauses. Implementations of LeafClause should cache
 * their results in the resultsCache, which is then passed on to the clauses
 * that enclose the LeafClause.
 *
 * @author kec
 */
@XmlRootElement(name = "leaf")
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class LeafClause extends Clause {

    /**
     * The
     * <code>NidSet</code> of components that match the criterion
     * specified in the LeafClause.
     */
    NidSet resultsCache = new NidSet();

    public LeafClause(Query enclosingQuery) {
        super(enclosingQuery);
    }

    protected LeafClause() {
        super();
    }

    /**
     *
     * @return <code>NativeIdSetBI</code> of components in the resultsCache,
     * which is the components that match the criterion
     * specified in the LeafClause.
     */
    public NidSet getResultsCache() {
        return resultsCache;
    }

    public List<Clause> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Sets the specified nid as a member of the results cache set.
     *
     * @param nid
     */
    public void addToResultsCache(int nid) {
        this.resultsCache.add(nid);
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        resultsCache.and(incomingComponents);
        return resultsCache;
    }
}
