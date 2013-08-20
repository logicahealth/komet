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
import java.util.EnumSet;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Acts as root node for the construction of queries. Sorts results from one of
 * more child clauses, which can be instances of a ParentClause or
 * <code>LeafClause</code>. Every
 * <code>Query</code> must contain a ParentClause as the root node.
 *
 * @author kec
 */
public abstract class ParentClause extends Clause {

    /**
     * Array of instances of
     * <code>Clause</code> that are children of the ParentClause in the tree
     * used to compute the constructed
     * <code>Query</code>.
     */
    private Clause[] children;

    @Override
    public Clause[] getChildren() {
        return children;
    }

    /**
     * Constructor from a Query and child clauses.
     *
     * @param enclosingQuery
     * @param children
     */
    public ParentClause(Query enclosingQuery, Clause... children) {
        super(enclosingQuery);
        this.children = children;
        for (Clause child : children) {
            child.setParent(this);
        }
    }

    @Override
    public final EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_AND_POST_ITERATION;
    }

    @Override
    public final void getQueryMatches(ConceptVersionBI conceptVersion) throws IOException, ContradictionException {
        for (Clause c : children) {
            c.getQueryMatches(conceptVersion);
        }
    }
}
