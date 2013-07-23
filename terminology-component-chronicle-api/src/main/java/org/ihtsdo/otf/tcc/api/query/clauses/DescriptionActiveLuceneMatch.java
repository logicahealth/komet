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
import org.ihtsdo.otf.tcc.api.query.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 *
 * @author dylangrald
 */
public class DescriptionActiveLuceneMatch extends DescriptionLuceneMatch {

    public DescriptionActiveLuceneMatch(Query enclosingQuery, String luceneMatch) {
        super(enclosingQuery, luceneMatch);
        this.luceneMatch = luceneMatch;
    }
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION_AND_ITERATION;
    }


    @Override
    public void getQueryMatches(ConceptVersionBI conceptVersion) {
        // see if the descriptions are active...
        // remove any from the cached set that are not active
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
