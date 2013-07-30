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

import java.util.EnumSet;
import org.ihtsdo.otf.tcc.api.query.ClauseComputeType;
import org.ihtsdo.otf.tcc.api.query.LeafClause;
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.query.Clause;
import org.ihtsdo.otf.tcc.api.query.Where;

/**
 *
 * @author kec
 */
public class DescriptionLuceneMatch extends LeafClause {

    String luceneMatch;
    String luceneMatchKey;

    public DescriptionLuceneMatch(Query enclosingQuery, String luceneMatchKey) {
        super(enclosingQuery);
        this.luceneMatchKey = luceneMatchKey;
        this.luceneMatch = (String) enclosingQuery.getLetDeclarations().get(luceneMatchKey);
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public final NativeIdSetBI computePossibleComponents(NativeIdSetBI incomingPossibleComponents) {
        // get a list from lucene
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) {
        // Nothing to do...
    }
    @Override
    public Where.WhereClause getWhereClause() {
        Where.WhereClause whereClause = new Where.WhereClause();
        whereClause.setSemantic(Where.ClauseSemantic.DESCRIPTION_LUCENE_MATCH);
        for(Clause clause : getChildren()){
            whereClause.getChildren().add(clause.getWhereClause());
        }
        whereClause.getLetKeys().add(luceneMatchKey);
        return whereClause;
    }
}
