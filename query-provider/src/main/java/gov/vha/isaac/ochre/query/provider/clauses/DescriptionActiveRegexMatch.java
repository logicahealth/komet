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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.query.provider.clauses;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.WhereClause;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Calculates the active descriptions that match the specified Java Regular
 * Expression.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionActiveRegexMatch extends DescriptionRegexMatch {

    public DescriptionActiveRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey) {
        super(enclosingQuery, regexKey, viewCoordinateKey);
    }
    protected DescriptionActiveRegexMatch() {
    }
    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        String regex = (String) enclosingQuery.getLetDeclarations().get(regexKey);
        
        ConceptChronology<? extends ConceptVersion> conceptChronology = conceptVersion.getChronology();
        
        conceptChronology.getConceptDescriptionList().stream().forEach((dc) -> {
            dc.getVersionList().stream().filter((dv) -> (dv.getText()
                    .matches(regex) && dv.getState() == State.ACTIVE))
                    .forEach((dv) -> {
                addToResultsCache((dv.getNid()));
            });
        });
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.DESCRIPTION_ACTIVE_REGEX_MATCH);
        whereClause.getLetKeys().add(regexKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;
    }
}
