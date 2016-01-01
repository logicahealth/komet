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
import java.util.Optional;
import gov.vha.isaac.ochre.query.provider.Clause;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.ParentClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Retrieves the fully specified names for a result set of concepts.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class FullySpecifiedNameForConcept extends ParentClause {

    public FullySpecifiedNameForConcept(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);
    }
    protected FullySpecifiedNameForConcept() {
    }
    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.FULLY_SPECIFIED_NAME_FOR_CONCEPT);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        return incomingPossibleComponents;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        LanguageCoordinate languageCoordinate = getEnclosingQuery().getLanguageCoordinate();
        StampCoordinate stampCoordinate = getEnclosingQuery().getStampCoordinate();
        NidSet outgoingFullySpecifiedNids = new NidSet();
        for (Clause childClause : getChildren()) {
            NidSet childPossibleComponentNids = childClause.computePossibleComponents(incomingComponents);
            ConceptSequenceSet conceptSequenceSet = ConceptSequenceSet.of(childPossibleComponentNids);
            Get.conceptService().getConceptChronologyStream(conceptSequenceSet)
                    .forEach((conceptChronology) -> {
                        Optional<LatestVersion<DescriptionSememe<?>>> desc = 
                                conceptChronology.getFullySpecifiedDescription(languageCoordinate, stampCoordinate);
                        if (desc.isPresent()) {
                            outgoingFullySpecifiedNids.add(desc.get().value().getNid());
                        }
                    });
        }
        return outgoingFullySpecifiedNids;
    }
}
