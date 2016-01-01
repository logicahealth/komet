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
package gov.vha.isaac.ochre.query.provider;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.io.IOException;
import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Returns components that are in the incoming For set and not in the set
 * returned from the computation of the clauses that are descendents of the
 * <code>Not</code> clause.
 *
 * @author kec
 */
@XmlRootElement()
public class Not extends ParentClause {

    @XmlTransient
    NidSet forSet;
    @XmlTransient
    NidSet notSet;

    public Not(Query enclosingQuery, Clause child) {
        super(enclosingQuery, child);
    }
    /**
     * Default no arg constructor for Jaxb.
     */
    protected Not() {
        super();
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        this.notSet = new NidSet();
        for (Clause c : getChildren()) {
            for (ClauseComputeType cp : c.getComputePhases()) {
                switch (cp) {
                    case PRE_ITERATION:
                        notSet.or(c.computePossibleComponents(incomingPossibleComponents));
                        break;
                    case ITERATION:
                        c.computePossibleComponents(incomingPossibleComponents);
                        break;
                    case POST_ITERATION:
                        c.computePossibleComponents(incomingPossibleComponents);
                        break;
                }
            }
        }
        return incomingPossibleComponents;
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.NOT);
        for (Clause clause : getChildren()) {
            whereClause.getChildren().add(clause.getWhereClause());
        }
        return whereClause;
    }

    @Override
    public NidSet computeComponents(NidSet incomingComponents) {
        this.forSet = enclosingQuery.getForSet();
        assert forSet != null;
        ConceptSequenceSet activeSet = new ConceptSequenceSet();
        
        Get.conceptService().getConceptChronologyStream(ConceptSequenceSet.of(incomingComponents)).forEach((ConceptChronology cc) -> {
            Optional<ConceptVersion> latestVersion = cc.getLatestVersion(ConceptVersion.class, getEnclosingQuery().getStampCoordinate());
            if (latestVersion.isPresent()) {
                activeSet.add(cc.getNid());
            }
        });
        getChildren().stream().forEach((c) -> {
            notSet.or(c.computeComponents(incomingComponents));
        });
        forSet = NidSet.of(activeSet);
        
        forSet.andNot(notSet);
        return forSet;
    }
}
