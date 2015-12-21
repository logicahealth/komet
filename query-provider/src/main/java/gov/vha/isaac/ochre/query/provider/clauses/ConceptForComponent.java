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
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.NidSet;
import java.util.EnumSet;
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
 * Computes the set of enclosing concepts for the set of components that
 * are returned from the child clause.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ConceptForComponent extends ParentClause {

    public ConceptForComponent(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);
    }
    protected ConceptForComponent() {
    }
    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleConceptNids) {
        NidSet incomingPossibleComponentNids = Get.identifierService().getComponentNidsForConceptNids(ConceptSequenceSet.of(incomingPossibleConceptNids));

        NidSet outgoingPossibleConceptNids = new NidSet();
        for (Clause childClause : getChildren()) {
            NidSet childPossibleComponentNids = childClause.computePossibleComponents(incomingPossibleComponentNids);
            ConceptSequenceSet conceptSet = Get.identifierService().getConceptSequenceSetForComponentNidSet(childPossibleComponentNids);
            outgoingPossibleConceptNids.or(NidSet.of(conceptSet));
        }
        return outgoingPossibleConceptNids;
    }
    
    @Override
    public EnumSet<ClauseComputeType> getComputePhases(){
        return POST_ITERATION;
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.CONCEPT_FOR_COMPONENT);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        NidSet incomingPossibleComponentNids = Get.identifierService().getComponentNidsForConceptNids(ConceptSequenceSet.of(incomingComponents));
        NidSet outgoingPossibleConceptNids = new NidSet();
        for (Clause childClause : getChildren()) {
            NidSet childPossibleComponentNids = childClause.computeComponents(incomingPossibleComponentNids);
            outgoingPossibleConceptNids.or(NidSet.of(Get.identifierService().getConceptSequenceSetForComponentNidSet(childPossibleComponentNids)));
        }
        return outgoingPossibleConceptNids;
    }
}
