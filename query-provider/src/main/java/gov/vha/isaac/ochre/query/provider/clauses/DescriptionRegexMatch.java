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
package gov.vha.isaac.ochre.query.provider.clauses;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.WhereClause;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Calculates descriptions that match the specified Java Regular Expression.
 * Very slow when iterating over a large
 * {@link org.ihtsdo.otf.query.implementation.ForCollection} set.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionRegexMatch extends LeafClause {

    NidSet cache = new NidSet();
    @XmlElement
    String regexKey;
    @XmlElement
    String viewCoordinateKey;

    public DescriptionRegexMatch(Query enclosingQuery, String regexKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.viewCoordinateKey = viewCoordinateKey;
        this.regexKey = regexKey;
    }
    protected DescriptionRegexMatch() {
    }
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return ITERATION;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        this.cache = incomingPossibleComponents;
        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        String regex = (String) enclosingQuery.getLetDeclarations().get(regexKey);
        ConceptChronology<? extends ConceptVersion> conceptChronology = conceptVersion.getChronology();
         
        conceptChronology.getConceptDescriptionList().forEach((description)->{
            if (cache.contains(description.getNid())) {
                description.getVersionList().forEach((dv) -> {
                    if (dv.getText().matches(regex)) {
                        addToResultsCache((dv.getNid()));
                    }
                });
            }
        });
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.DESCRIPTION_REGEX_MATCH);
        whereClause.getLetKeys().add(regexKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        return whereClause;
    }
}
