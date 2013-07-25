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
package org.ihtsdo.otf.tcc.api.query.clauses;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.ParentClause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 *
 * @author kec
 */
public class ConceptForComponent extends ParentClause {

    public ConceptForComponent(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleConcepNids) throws IOException, ValidationException, ContradictionException {
        NativeIdSetBI incomingPossibleComponentNids = Ts.get().getComponentNidsForConceptNids(incomingPossibleConcepNids);

        NativeIdSetBI outgoingPossibleConceptNids = new ConcurrentBitSet();
        for (Clause childClause : getChildren()) {
            NativeIdSetBI childPossibleComponentNids = childClause.computePossibleComponents(incomingPossibleComponentNids);
            outgoingPossibleConceptNids.or(Ts.get().getConceptNidsForComponentNids(childPossibleComponentNids));
        }
        return outgoingPossibleConceptNids;
    }
}
