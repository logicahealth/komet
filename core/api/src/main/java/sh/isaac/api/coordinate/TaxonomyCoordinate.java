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



package sh.isaac.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- interfaces -------------------------------------------------------------

/**
 *
 * @author kec
 */
public interface TaxonomyCoordinate
        extends TimeBasedAnalogMaker<TaxonomyCoordinate>, StateBasedAnalogMaker<TaxonomyCoordinate> {
   /**
    *
    * @param taxonomyType the {@code PremiseType} for the analog
    * @return a new taxonomyCoordinate with the specified taxonomy type.
    */
   TaxonomyCoordinate makeAnalog(PremiseType taxonomyType);

   //~--- get methods ---------------------------------------------------------

   /**
    * Convenience method, buffers concept sequence in a cache-sensitive manner.
    * @return the concept sequence that defines the is-a relationship type.
    */
   int getIsaConceptSequence();

   /**
    *
    * @return a LanguageCoordinate that specifies how to manage the retrieval and display of language.
    * and dialect information.
    */
   LanguageCoordinate getLanguageCoordinate();

   /**
    *
    * @return a LogicCoordinate that specifies how to manage the retrieval and display of logic information.
    */
   LogicCoordinate getLogicCoordinate();

   /**
    *
    * @return a StampCoordinate that specifies the retrieval and display of
    * object chronicle versions by indicating the current position on a path, and allowed modules.
    */
   StampCoordinate getStampCoordinate();

   /**
    *
    * @return PremiseType.STATED if taxonomy operations should be based on stated definitions, or
    * PremiseType.INFERRED if taxonomy operations should be based on inferred definitions.
    */
   PremiseType getTaxonomyType();

   /**
    *
    * @return a UUID that uniquely identifies this taxonomy coordinate.
    */
   UUID getUuid();
}

