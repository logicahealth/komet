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



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- interfaces -------------------------------------------------------------

/**
 * Observable update rules:
 * 1. A Chronology update will be sent to the ObservableChronology with the
 same nid, to update the contents of the ObservableChronology, if it is
 in memory.

 2. A SemanticChronology update will be sent to the ObservableConceptChronology
 of it's assemblage if it is in memory. If it is in memory, and the ObservableList
 of the assemblage's semanticChronology is is memory, then the observable Semantic list will
 be properly updated.

 3. A SemanticChronology update will be sent to the ObservableChronology of the referenced
 component if it is in memory, and will update the Semantic for component ObservableList
 if it is instantiated.

 4. A SemanticChronology update will be sent to the taxonomy manager. The taxonomy
 manager will check for updated to the stated and inferred taxonomy.

 5. A SemanticChronology update will be sent to the LogicService. The logic service
 will determine if changes impact incremental or fully classified data, and will update
 the logic status accordingly.
 *
 *
 *
 * @author kec
 */
public interface ChronologyChangeListener {

   final static Logger log = LogManager.getLogger(ChronologyChangeListener.class);
   /**
    * Don't do work on or block the calling thread.
    * @param cc a ConceptChronology that has changed, but has not been committed.
    */
   void handleChange(ConceptChronology cc);

   /**
    * Don't do work on or block the calling thread.
    * @param sc a SemanticChronology that has changed, but has not been committed.
    */
   void handleChange(SemanticChronology sc);

   /**
    * Don't do work on or block the calling thread.
    * @param commitRecord a record of a successful commit.
    */
   void handleCommit(CommitRecord commitRecord);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the listener uuid.
    *
    * @return a unique UUID for this listener.
    */
   UUID getListenerUuid();
   
   /**
    * Disable the listener
    */
   default void disable() {
       log.warn("disable() method not implemented by {}", this.getClass().getSimpleName());
   }

   /**
    * Enable the listener
    */
   default void enable() {
       log.warn("enable() method not implemented by {}", this.getClass().getSimpleName());
   }
}

