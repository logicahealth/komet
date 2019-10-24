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


//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

import java.time.Instant;
import java.util.Optional;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface LogicService.
 *
 * @author kec
 */
@Contract
public interface LogicService {
   /**
    * Gets the classifier service.
    *
    * @param stampCoordinate the stamp coordinate
    * @param logicCoordinate the logic coordinate
    * @param editCoordinate the edit coordinate
    * @return the classifier service
    */
   ClassifierService getClassifierService(StampCoordinate stampCoordinate,
         LogicCoordinate logicCoordinate,
         EditCoordinate editCoordinate);
   
   default ClassifierService getClassifierService(ManifoldCoordinate coordinate,
         EditCoordinate editCoordinate) {
      return getClassifierService(coordinate, coordinate, editCoordinate);
   }

   /**
    * Gets the logical expression.
    *
    * @param conceptId the concept id
    * @param logicAssemblageId the logic assemblage id
    * @param stampCoordinate the stamp coordinate
    * @return the logical expression
    */
   LatestVersion<? extends LogicalExpression> getLogicalExpression(int conceptId,
         int logicAssemblageId,
         StampCoordinate stampCoordinate);

   /**
    *
    * @return an array of instants at which one or more classifications where committed.
    */
   Instant[] getClassificationInstants();

   /**
    *
    * @return an array of ClassifierResults that where committed at any given instant.  It is possible that
    * multiple ClassifierResults are committed at a particular instant, such as the simultaneous release of
    * a variety of editions.
    */
   Optional<ClassifierResults[]> getClassificationResultsForInstant(Instant instant);

   void addClassifierResults(ClassifierResults classifierResults);

}

