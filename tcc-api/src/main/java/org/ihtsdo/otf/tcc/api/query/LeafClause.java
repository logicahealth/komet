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
package org.ihtsdo.otf.tcc.api.query;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;

/**
 * A query clause is a leaf in the tree of clauses of a query. A LeafClause
 * cannot have any child clauses. Implementations of LeafClause should cache
 * their results in the resultsCache, which is then passed on to the clauses
 * that enclose the LeafClause.
 *
 * @author kec
 */
public abstract class LeafClause extends Clause {

    /**
     * The
     * <code>NativeIdSetBI</code> of components that match the criterion
     * specified in the LeafClause.
     */
    ConcurrentBitSet resultsCache = new ConcurrentBitSet();

    public LeafClause(Query enclosingQuery) {
        super(enclosingQuery);
    }

    /**
     *
     * @return <code>NativeIdSetBI</code> of components in the resultsCache,
     * the <code>NativeIdSetBI</code> of components that match the criterion
     * specified in the LeafClause.
     */
    public NativeIdSetBI getResultsCache() {
        return resultsCache;
    }

    /**
     * Setter for the results cache.
     * @param cache
     */
    public void setResultsCache(ConcurrentBitSet cache) {
        this.resultsCache = cache;
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
    public final NativeIdSetBI computeComponents(NativeIdSetBI incomingComponents) throws IOException {
        resultsCache.and(incomingComponents);
        return resultsCache;
    }
}
