/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api.logic;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.classifier.ClassifierService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import java.util.Optional;
import java.util.stream.Stream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface LogicService {

    Optional<LatestVersion<? extends LogicalExpression>> getLogicalExpression(int conceptId, int logicAssemblageId, 
            StampCoordinate<?> stampCoordinate);
    
    ClassifierService getClassifierService(
            StampCoordinate<?> stampCoordinate,
            LogicCoordinate logicCoordinate,
            EditCoordinate editCoordinate);

    Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>>
            getRelationshipAdaptorsOriginatingWithConcept(ConceptChronology<?> conceptChronology);
    Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>>
            getRelationshipAdaptorsOriginatingWithConcept(ConceptChronology<?> conceptChronology,
                    LogicCoordinate logicCoordinate);
    Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>>
            getRelationshipAdaptorsWithConceptAsDestination(ConceptChronology<?> conceptChronology);
    Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>>
            getRelationshipAdaptorsWithConceptAsDestination(ConceptChronology<?> conceptChronology,
                    LogicCoordinate logicCoordinate);
}
