/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.logic;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.relationship.RelationshipVersionAdaptor;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
@Contract
public interface LogicService {
   ClassifierService getClassifierService(StampCoordinate stampCoordinate,
         LogicCoordinate logicCoordinate,
         EditCoordinate editCoordinate);

   Optional<LatestVersion<? extends LogicalExpression>> getLogicalExpression(int conceptId,
         int logicAssemblageId,
         StampCoordinate stampCoordinate);

   Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsOriginatingWithConcept(
           ConceptChronology<?> conceptChronology);

   Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsOriginatingWithConcept(
           ConceptChronology<?> conceptChronology,
           LogicCoordinate logicCoordinate);

   Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsWithConceptAsDestination(
           ConceptChronology<?> conceptChronology);

   Stream<? extends SememeChronology<? extends RelationshipVersionAdaptor<?>>> getRelationshipAdaptorsWithConceptAsDestination(
           ConceptChronology<?> conceptChronology,
           LogicCoordinate logicCoordinate);
}

