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



package sh.isaac.provider.query.clauses;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.provider.query.ClauseComputeType;
import sh.isaac.provider.query.LeafClause;
import sh.isaac.provider.query.Query;
import sh.isaac.provider.query.WhereClause;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ComponentsFromSnomedIds.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ComponentsFromSnomedIds
        extends LeafClause {
   /**
    * Instantiates a new components from snomed ids.
    */
   protected ComponentsFromSnomedIds() {}

   /**
    * Instantiates a new components from snomed ids.
    *
    * @param enclosingQuery the enclosing query
    */
   public ComponentsFromSnomedIds(Query enclosingQuery) {
      super(enclosingQuery);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compute possible components.
    *
    * @param incomingPossibleComponents the incoming possible components
    * @return the nid set
    */
   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    * @return the query matches
    */
   @Override
   public void getQueryMatches(ConceptVersion conceptVersion) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the where clause.
    *
    * @return the where clause
    */
   @Override
   public WhereClause getWhereClause() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }
}

