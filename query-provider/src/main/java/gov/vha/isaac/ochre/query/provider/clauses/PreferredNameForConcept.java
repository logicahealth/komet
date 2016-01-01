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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import java.util.Optional;
import gov.vha.isaac.ochre.query.provider.Clause;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.ParentClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Retrieves the preferred names for a result set of concepts.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class PreferredNameForConcept extends ParentClause {

    public PreferredNameForConcept(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);

    }
    protected PreferredNameForConcept() {
    }
    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.PREFERRED_NAME_FOR_CONCEPT);
        getChildren().stream().forEach((clause) -> {
            whereClause.getChildren().add(clause.getWhereClause());
        });
        return whereClause;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleConcepts) {
        return incomingPossibleConcepts;
    }
    
    @Override
    public EnumSet<ClauseComputeType> getComputePhases(){
        return POST_ITERATION;
    }

    @Override
    public NidSet computeComponents(NidSet incomingConcepts) {
        LanguageCoordinate languageCoordinate = getEnclosingQuery().getLanguageCoordinate();
        StampCoordinate stampCoordinate = getEnclosingQuery().getStampCoordinate();
        NidSet outgoingPreferredNids = new NidSet();
        getChildren().stream().map((childClause) -> childClause.computePossibleComponents(incomingConcepts)).map((childPossibleComponentNids) -> ConceptSequenceSet.of(childPossibleComponentNids)).forEach((conceptSequenceSet) -> {
            Get.conceptService().getConceptChronologyStream(conceptSequenceSet)
                    .forEach((conceptChronology) -> {
                        Optional<LatestVersion<DescriptionSememe<?>>> desc = 
                                conceptChronology.getPreferredDescription(languageCoordinate, stampCoordinate);
                        if (desc.isPresent()) {
                            outgoingPreferredNids.add(desc.get().value().getNid());
                        }
                    });
        });
        return outgoingPreferredNids;
    }
}
