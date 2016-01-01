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
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Allows the user to define a restriction on the destination set of a
 * relationship query. Also allows the user to specify subsumption on the
 * destination restriction and relType.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class RelRestriction extends LeafClause {

    @XmlElement
    String relTypeKey;
    @XmlElement
    String destinationSpecKey;
    @XmlElement
    String viewCoordinateKey;
    @XmlElement
    String destinationSubsumptionKey;
    @XmlElement
    String relTypeSubsumptionKey;

    ConceptSequenceSet destinationSet;
    ConceptSequenceSet relTypeSet;

    public RelRestriction(Query enclosingQuery, String relTypeKey, String destinationSpecKey,
            String viewCoordinateKey, String destinationSubsumptionKey, String relTypeSubsumptionKey) {
        super(enclosingQuery);
        this.destinationSpecKey = destinationSpecKey;
        this.relTypeKey = relTypeKey;
        this.viewCoordinateKey = viewCoordinateKey;
        this.relTypeSubsumptionKey = relTypeSubsumptionKey;
        this.destinationSubsumptionKey = destinationSubsumptionKey;

    }

    protected RelRestriction() {
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.REL_RESTRICTION);
        whereClause.getLetKeys().add(relTypeKey);
        whereClause.getLetKeys().add(destinationSpecKey);
        whereClause.getLetKeys().add(viewCoordinateKey);
        whereClause.getLetKeys().add(destinationSubsumptionKey);
        whereClause.getLetKeys().add(relTypeSubsumptionKey);
        System.out.println("Where clause size: " + whereClause.getLetKeys().size());
        return whereClause;

    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION_AND_ITERATION;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        System.out.println("Let declerations: " + enclosingQuery.getLetDeclarations());
        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
        ConceptSpecification destinationSpec = (ConceptSpecification) enclosingQuery.getLetDeclarations().get(destinationSpecKey);
        ConceptSpecification relType = (ConceptSpecification) enclosingQuery.getLetDeclarations().get(relTypeKey);
        Boolean relTypeSubsumption = (Boolean) enclosingQuery.getLetDeclarations().get(relTypeSubsumptionKey);
        Boolean destinationSubsumption = (Boolean) enclosingQuery.getLetDeclarations().get(destinationSubsumptionKey);

        //The default is to set relTypeSubsumption and destinationSubsumption to true.
        if (relTypeSubsumption == null) {
            relTypeSubsumption = true;
        }
        if (destinationSubsumption == null) {
            destinationSubsumption = true;
        }

        relTypeSet = new ConceptSequenceSet();
        relTypeSet.add(relType.getConceptSequence());
        if (relTypeSubsumption) {
            relTypeSet.or(Get.taxonomyService().getKindOfSequenceSet(relType.getConceptSequence(), taxonomyCoordinate));
        }

        destinationSet = new ConceptSequenceSet();
        destinationSet.add(destinationSpec.getConceptSequence());
        if (destinationSubsumption) {
            destinationSet.or(Get.taxonomyService().getKindOfSequenceSet(destinationSpec.getConceptSequence(), taxonomyCoordinate));
        }

        return incomingPossibleComponents;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
        Get.taxonomyService().getAllRelationshipDestinationSequencesOfType(
                conceptVersion.getChronology().getConceptSequence(), relTypeSet, taxonomyCoordinate)
                .forEach((destinationSequence) -> {
                    if (destinationSet.contains(destinationSequence)) {
                        getResultsCache().add(conceptVersion.getChronology().getNid());
                    }
                });
    }
}
