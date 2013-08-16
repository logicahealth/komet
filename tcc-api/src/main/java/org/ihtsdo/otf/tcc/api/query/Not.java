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
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Clause that computes the relative complement of the result of the child
 * clause with respect to the For set.
 *
 * @author kec
 */
public class Not extends ParentClause {

    NativeIdSetBI forSet;

    public Not(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);
        forSet = enclosingQuery.getForSet();
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) throws IOException, ValidationException, ContradictionException {
        NativeIdSetBI notSet = new ConcurrentBitSet();
        for (Clause c : getChildren()) {
            notSet.or(c.computePossibleComponents(incomingPossibleComponents));
        }
        return notSet;
    }

    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.NOT);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NativeIdSetBI computeComponents(NativeIdSetBI incomingComponents) throws IOException, ValidationException, ContradictionException {
        NativeIdSetBI forSetCopy = new ConcurrentBitSet(forSet);
        NativeIdSetBI notSet = new ConcurrentBitSet();
        for (Clause c : getChildren()) {
            notSet.or(c.computeComponents(incomingComponents));
        }
        forSetCopy.andNot(notSet);
        return forSetCopy;
    }
}
