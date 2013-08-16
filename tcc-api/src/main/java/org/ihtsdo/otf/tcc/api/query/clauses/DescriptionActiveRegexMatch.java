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
import java.util.EnumSet;
import org.ihtsdo.otf.tcc.api.query.ClauseComputeType;
import org.ihtsdo.otf.tcc.api.query.LeafClause;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Where;

/**
 * Calculates the active descriptions that contain the input Java Regular
 * Expression.
 *
 * @author dylangrald
 */
public class DescriptionActiveRegexMatch extends LeafClause {

    /**
     * The Java Regular Expression used in the search.
     */
    String regex;
    String regexKey;
    /**
     * Results cache used to optimize getQueryMatches method.
     */
    NativeIdSetBI cache = new ConcurrentBitSet();

    public DescriptionActiveRegexMatch(Query enclosingQuery, String regexKey) {
        super(enclosingQuery);
        this.regexKey = regexKey;
        this.regex = (String) enclosingQuery.getLetDeclarations().get(regexKey);
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return ITERATION;
    }

    @Override
    public NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) {
        this.cache = incomingPossibleComponents;
        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) throws IOException, ContradictionException {
        for (DescriptionChronicleBI dc : conceptVersion.getDescriptionsActive()) {
            if (cache.contains(dc.getNid())) {
                for (DescriptionVersionBI dv : dc.getVersions()) {
                    if (dv.getText().matches(regex)) {
                        addToResultsCache((dv.getNid()));
                    }
                }
            }
        }
    }

    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.DESCRIPTION_REGEX_MATCH);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        whereClause.getLetKeys().add(regexKey);
        return whereClause;
    }
}
