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



package sh.isaac.api.classifier;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.logic.LogicalExpression;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface ClassifierService {
   /**
    * Will perform a full or incremental classification as necessary.
    * @return A task that can be used to block, if the caller wishes to wait
    * for the results.
    */
   Task<ClassifierResults> classify();

   //~--- get methods ---------------------------------------------------------

   /**
    * NOTE: this method call may cause a full or incremental classification if
    * changes have been made to the axioms since the last classify. If the expression
    * does not exist, and therfore this method adds the axioms for it,
    * a classification will be performed.
    * @param expression Expression to identify the concept identifier for.
    * @param editCoordinate edit coordinate in case the expression represents a
    * new concept, and thus needs to be added, and classified
    * @return  A task that can be used to block, if the caller wishes to wait
    * for the results.
    */
   Task<Integer> getConceptSequenceForExpression(LogicalExpression expression, EditCoordinate editCoordinate);
}

